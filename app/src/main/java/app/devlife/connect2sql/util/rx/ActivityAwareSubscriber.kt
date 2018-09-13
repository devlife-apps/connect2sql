package app.devlife.connect2sql.util.rx

import android.app.Activity
import app.devlife.connect2sql.log.EzLogger
import rx.Subscriber
import java.lang.ref.WeakReference

class ActivityAwareSubscriber<T>(
    activity: Activity?,
    private val delegate: Subscriber<T>
) : Subscriber<T>() {

    private val activityRef: WeakReference<Activity?> = WeakReference(activity)

    override fun onStart() {
        if (isValid()) delegate.onStart()
        else EzLogger.v("Activity has gone away...")
    }

    override fun onNext(t: T) {
        if (isValid()) delegate.onNext(t)
        else EzLogger.v("Activity has gone away...")
    }

    override fun onError(e: Throwable?) {
        if (isValid()) delegate.onError(e)
        else EzLogger.v("Activity has gone away...")
    }

    override fun onCompleted() {
        if (isValid()) delegate.onCompleted()
        else EzLogger.v("Activity has gone away...")
    }

    private fun isValid(): Boolean {
        return activityRef.get()?.let { activity ->
            !activity.isDestroyed && !activity.isFinishing && activity.window.decorView.isShown
        } ?: false
    }
}