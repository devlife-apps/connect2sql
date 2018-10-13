package app.devlife.connect2sql.ui.widget

import android.content.Context
import android.support.design.bottomappbar.BottomAppBar
import android.util.AttributeSet

class CustomBottomAppBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomAppBar(context, attrs, defStyleAttr) {

    private var onMenuItemClickListener: OnMenuItemClickListener? = null

    init {
        super.setOnMenuItemClickListener { menuItem ->
            onMenuItemClickListener?.onMenuItemClick(menuItem) ?: false
        }
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        this.onMenuItemClickListener = listener
    }

    fun performClickOn(itemId: Int): Boolean {
        (0 until menu.size()).forEach {
            val menuItem = menu.getItem(it)
            if (menuItem.itemId == itemId) {
                return onMenuItemClickListener?.onMenuItemClick(menuItem) ?: false
            }
        }

        return false
    }

    fun clearMenuSelection() {
        (0 until menu.size()).forEach {
            menu.getItem(it).isChecked = false
        }
    }
}