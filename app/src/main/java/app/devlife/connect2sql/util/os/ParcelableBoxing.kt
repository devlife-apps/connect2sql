package app.devlife.connect2sql.util.os

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StringParcelable(val value: String) : CharSequence, Parcelable {

    override fun get(index: Int): Char = value.get(index)

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        subSequence(startIndex, endIndex)

    override val length: Int
        get() = value.length
}

fun String.toParcelable() = StringParcelable(this)