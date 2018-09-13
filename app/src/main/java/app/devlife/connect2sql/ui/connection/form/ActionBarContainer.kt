package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gitlab.connect2sql.R

class ActionBarContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    FrameLayout(context, attrs, defStyle) {

    val logoView: ImageView
    val titleView: TextView
    private val backgroundView: FrameLayout

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.form_actionbar, this)
        backgroundView = view.findViewById(R.id.form_actionbar_background)
        logoView = view.findViewById(R.id.form_actionbar_logo)
        titleView = view.findViewById(R.id.form_actionbar_title)
    }

    override fun setBackgroundResource(resid: Int) {
        backgroundView.setBackgroundResource(resid)
    }

    override fun setBackground(background: Drawable) {
        this.backgroundView.background = background
    }
}
