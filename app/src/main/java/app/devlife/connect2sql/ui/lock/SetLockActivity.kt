package app.devlife.connect2sql.ui.lock

import android.os.Bundle

import javax.inject.Inject

import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.data.LockManager
import me.zhanghai.android.patternlock.PatternUtils
import me.zhanghai.android.patternlock.PatternView
import me.zhanghai.android.patternlock.SetPatternActivity
import org.slf4j.LoggerFactory

/**

 */
class SetLockActivity : SetPatternActivity() {

    val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var lockManager: LockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("[onCreate]: $savedInstanceState")
        super.onCreate(savedInstanceState)
        ApplicationUtils.getApplication(this).applicationComponent.inject(this)
    }

    override fun onSetPattern(pattern: List<PatternView.Cell>) {
        logger.debug("[onCreate]: $pattern")
        val patternSha1 = PatternUtils.patternToSha1String(pattern)
        lockManager.passphrase = patternSha1
    }
}
