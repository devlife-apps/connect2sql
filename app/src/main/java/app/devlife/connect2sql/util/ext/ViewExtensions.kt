package app.devlife.connect2sql.util.ext

import android.view.View

fun <T : View> View.findSiblingById(id: Int): T? {
    return (this.parent as View?)?.findViewById(id)
}