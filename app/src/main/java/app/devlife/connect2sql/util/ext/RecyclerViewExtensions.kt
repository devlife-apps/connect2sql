package app.devlife.connect2sql.util.ext

import android.support.v7.widget.RecyclerView
import android.view.MotionEvent

private val parentDisablingOnItemTouchListener = object :
    RecyclerView.OnItemTouchListener {
    override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {
        // ignored
    }

    override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
        // ignored
    }

    override fun onInterceptTouchEvent(v: RecyclerView, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            v.parent.requestDisallowInterceptTouchEvent(true)
        }
        return false
    }
}

/**
 * Disable parent touch interception. Useful for nested RecyclerViews.
 */
fun RecyclerView.disableParentInterception() {
    this.addOnItemTouchListener(parentDisablingOnItemTouchListener)
}