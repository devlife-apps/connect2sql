package app.devlife.connect2sql.util.ext

import android.widget.Spinner

fun Spinner.setSelectionByResource(resourceId: Int) {
    val value = context.getString(resourceId)
    val position = (0 until this.adapter.count).firstOrNull { position ->
        this.adapter.getItem(position).toString() == value
    }

    position?.also { this.setSelection(it) }
}