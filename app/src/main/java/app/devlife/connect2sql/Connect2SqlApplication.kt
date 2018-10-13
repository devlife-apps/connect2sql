package app.devlife.connect2sql

import android.app.Activity
import android.app.Application
import app.devlife.connect2sql.activity.LaunchActivity
import app.devlife.connect2sql.data.LockManager
import app.devlife.connect2sql.di.AnalyticsModule
import app.devlife.connect2sql.di.ApplicationComponent
import app.devlife.connect2sql.di.ApplicationModule
import app.devlife.connect2sql.di.ConnectionModule
import app.devlife.connect2sql.di.DaggerApplicationComponent
import app.devlife.connect2sql.di.DatabaseModule
import app.devlife.connect2sql.di.PreferencesModule
import app.devlife.connect2sql.di.SecurityModule
import app.devlife.connect2sql.log.EzLogger
import io.fabric.sdk.android.Fabric
import javax.inject.Inject


/**
 *
 */
class Connect2SqlApplication : Application() {

    lateinit var applicationComponent: ApplicationComponent

    @Inject
    lateinit var applicationFocusManager: ApplicationFocusManager
    @Inject
    lateinit var lockManager: LockManager
    @Inject
    lateinit var fabric: Fabric

    private val mOnFocusChangeListener = ApplicationFocusManager.OnFocusChangeListener { focused ->
        if (focused) {
            val lastFocusedActivity = applicationFocusManager.lastFocusedActivity
            if (lastFocusedActivity != null) {
                if (lastFocusedActivity.get() != null) {
                    val activity = lastFocusedActivity.get()
                    EzLogger.d("Last focused activity: $activity")
                    if (activity !is LaunchActivity) {
                        if (!lockManager.isSetLockActivity(activity) &&
                            !lockManager.isUnlockActivity(activity) &&
                            !lockManager.isForgotLockActivity(activity)) {
                            lockManager.startUnlockActivity(activity, 0)
                        } else {
                            EzLogger.d("Activity is a lock specific activity.")
                        }
                    } else {
                        EzLogger.d("Last focused activity was the Launch activity.")
                    }
                } else {
                    EzLogger.d("Last focused activity has gone away.")
                }
            } else {
                EzLogger.d("No reference to last focused activity")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        applicationComponent = DaggerApplicationComponent.builder()
            .analyticsModule(AnalyticsModule(this))
            .applicationModule(ApplicationModule(this))
            .connectionModule(ConnectionModule(this))
            .databaseModule(DatabaseModule(this))
            .preferencesModule(PreferencesModule(this))
            .securityModule(SecurityModule(this))
            .build()
            .also { it.inject(this) }

        applicationFocusManager.addOnFocusChangeListener(mOnFocusChangeListener)

        EzLogger.i("Fabric version: " + fabric.version)
    }
}
