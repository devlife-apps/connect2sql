package app.devlife.connect2sql.util.ext

import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle


fun SpannableStringBuilder.appendMultiple(text: CharSequence,
                                          whats: List<CharacterStyle>,
                                          flags: Int): SpannableStringBuilder {
    val start = length
    append(text)
    whats.forEach {
        setSpan(it, start, length, flags)
    }
    return this
}