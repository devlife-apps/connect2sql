package app.devlife.connect2sql.util.ext

import android.view.View

fun <T : View> View.findSiblingById(id: Int): T? {
    return (this.parent as View?)?.findViewById(id)
}

fun View.onClickToggleVisibilityOf(view: View, invisibleState: Int = View.GONE) {
    setOnClickListener {
        view.visibility = when (view.visibility) {
            View.VISIBLE -> invisibleState
            else -> View.VISIBLE
        }
    }
}