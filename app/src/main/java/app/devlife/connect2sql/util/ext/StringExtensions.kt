package app.devlife.connect2sql.util.ext

fun String.equals(other: String,
                                    ignoreCase: Boolean = false,
                                    ignoreWhitespace: Boolean = false): Boolean {

    fun normalize(s: String?): String? = s
        ?.let { if (ignoreCase)  it.toLowerCase() else it }
        ?.let { if (ignoreWhitespace)  it.replace("\\s".toRegex(), "") else it }

    return normalize(this) == normalize(other)
}
