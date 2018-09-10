package app.devlife.connect2sql.util.ext

import android.widget.EditText


val EditText.stringValue: String
    get() = this.text.toString()

val EditText.nonBlankStringValue: String?
    get() {
        val value = this.text?.toString()
        return if (value.isNullOrBlank()) null else value
    }