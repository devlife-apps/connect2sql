package app.devlife.connect2sql.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import java.util.ArrayList

class FloatLabelPlusLayout : FloatLabelLayout {

    private var inputView: View? = null
    private val imageViews = ArrayList<ImageView>()

    private val onLongClickListener = OnLongClickListener { v ->
        if (!TextUtils.isEmpty(v.contentDescription)) {
            Toast.makeText(context, v.contentDescription, Toast.LENGTH_SHORT).show()
        }

        false
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context,
        attrs,
        defStyle)

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is ImageView) {
            imageViews.add(child)
            child.setOnLongClickListener(onLongClickListener)

            // do not hide icon if edittext is already focused
            if (editText != null && !editText.isFocused) {
                child.setVisibility(View.GONE)
            }
        } else if (child is Switch) {
            inputView = child
        }

        super.addView(child, index, params)
    }

    override fun setEditText(editText: EditText) {
        super.setEditText(editText)
        inputView = editText
    }

    override fun showLabel() {
        super.showLabel()
        if (editText.isFocused) {
            showActions()
        }
    }

    override fun onFocusChange(view: View, focused: Boolean) {
        super.onFocusChange(view, focused)
        if (!focused) {
            hideActions()
        }
    }

    protected fun showActions() {
        for (imageView in imageViews) {
            if (imageView.visibility != View.VISIBLE) {
                imageView.visibility = View.VISIBLE
                imageView.alpha = 0f
                imageView.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION_MS)
                    .setListener(null).start()
            }
        }
    }

    protected fun hideActions() {
        for (imageView in imageViews) {
            imageView.alpha = 1f
            imageView.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_MS)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        imageView.visibility = View.GONE
                    }
                }).start()
        }
    }

    companion object {
        private const val ANIMATION_DURATION_MS: Long = 300
    }
}
