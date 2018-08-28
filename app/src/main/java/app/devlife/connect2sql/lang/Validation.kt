package app.devlife.connect2sql.lang

fun <T> T.ensure(valid: (T) -> Boolean): T {
    return ensureWith(valid, { throw IllegalArgumentException("Argument was invalid!") })
}

fun <T> T.ensureWith(valid: (T) -> Boolean, action: () -> Unit): T {
    if (!valid(this)) action()
    return this
}