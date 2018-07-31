package me.jromero.connect2sql.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.TaskStackBuilder

import com.crashlytics.android.Crashlytics

import javax.inject.Inject

import me.jromero.connect2sql.ApplicationUtils
import me.jromero.connect2sql.data.LockManager
import me.jromero.connect2sql.db.AppDatabaseHelperV3
import me.jromero.connect2sql.db.model.connection.ConnectionInfoSqlModel
import me.jromero.connect2sql.db.model.query.HistoryQuery
import me.jromero.connect2sql.db.model.query.SavedQuery
import me.jromero.connect2sql.log.EzLogger
import me.jromero.connect2sql.ui.connection.ConnectionInfoDriverChooserActivity

/**

 */
class LaunchActivity : Activity() {

    @Inject
    lateinit var mLockManager: LockManager

    @Inject
    lateinit var mAppDatabaseHelperV3: AppDatabaseHelperV3

    @Inject
    lateinit var mConnectionInfoSqlModel: ConnectionInfoSqlModel

    @Inject
    lateinit var mHistoryQuerySqlModel: HistoryQuery.HistoryQuerySqlModel

    @Inject
    lateinit var mSavedQuerySqlModel: SavedQuery.SavedQuerySqlModel

    @Inject
    lateinit var mCrashlytics: Crashlytics

    private var mPendingResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mPendingResult = savedInstanceState.getBoolean(STATE_PENDING_RESULT, false)
        }

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        EzLogger.v("[onSaveInstanceState] mPendingResult=" + mPendingResult)
        outState.putBoolean(STATE_PENDING_RESULT, mPendingResult)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        EzLogger.v("[onStart]")
        super.onStart()
        if (!mPendingResult) {
            if (mLockManager.isPassphraseSet) {
                startUnlockActivity()
            } else {
                startSetLockActivity()
            }
            mPendingResult = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EzLogger.i("[onActivityResult] requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_CODE_UNLOCK) {
            if (mLockManager.wasUnlockValid(resultCode)) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                finish()
            }
            mPendingResult = false
        } else if (requestCode == REQUEST_CODE_SET_PATTERN) {
            if (mLockManager.wasPatternSet(resultCode)) {
                this@LaunchActivity.let {context ->
                    TaskStackBuilder.create(this@LaunchActivity)
                        .addNextIntent(Intent(this, DashboardActivity::class.java))
                        .addNextIntent(ConnectionInfoDriverChooserActivity.newIntent(this@LaunchActivity))
                        .startActivities()
                }
                finish()
            } else {
                finish()
            }
            mPendingResult = false
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startUnlockActivity() {
        EzLogger.d("[startUnlockActivity]")
        mLockManager.startUnlockActivity(this, REQUEST_CODE_UNLOCK)
    }

    private fun startSetLockActivity() {
        EzLogger.d("[startSetLockActivity]")
        mLockManager.startSetLockActivity(this, REQUEST_CODE_SET_PATTERN)
    }

    companion object {

        private val REQUEST_CODE_SET_PATTERN = 1
        private val REQUEST_CODE_UNLOCK = 2
        private val STATE_PENDING_RESULT = "STATE_PENDING_RESULT"
    }
}
