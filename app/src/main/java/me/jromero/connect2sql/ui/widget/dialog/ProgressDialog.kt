package me.jromero.connect2sql.ui.widget.dialog

import android.content.Context
import android.support.v7.app.AppCompatDialog
import android.widget.TextView
import com.gitlab.connect2sql.R

/**
 *
 */
class ProgressDialog(context: Context, title: String, message: String) : AppCompatDialog(context) {

    init {
        setContentView(R.layout.widget_progressdialog)
        setTitle(title)
        (findViewById(R.id.text1) as? TextView)?.text = message
    }
}