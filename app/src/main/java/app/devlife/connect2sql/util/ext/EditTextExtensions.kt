package app.devlife.connect2sql.util.ext

import android.widget.EditText

val EditText.intValue: Int
    get() = Integer.parseInt(this.text.toString())

val EditText.stringValue: String
    get() = this.text.toString()

val EditText.nonBlankStringValue: String?
    get() {
        val value = this.text?.toString()
        return if (value.isNullOrBlank()) null else value
    }