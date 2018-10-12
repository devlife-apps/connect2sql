package app.devlife.connect2sql.util.ext

import android.content.res.Resources
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP

fun Resources.dp2Px(dp: Float): Float {
    return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, this.displayMetrics)
}