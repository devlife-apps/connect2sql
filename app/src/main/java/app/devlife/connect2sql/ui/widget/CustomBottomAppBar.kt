package app.devlife.connect2sql.ui.widget

import android.content.Context
import android.support.design.bottomappbar.BottomAppBar
import android.support.v7.widget.Toolbar.OnMenuItemClickListener
import android.util.AttributeSet
import android.view.MenuItem

class CustomBottomAppBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomAppBar(context, attrs, defStyleAttr) {

    private var onMenuItemClickListener: OnMenuItemClickListener? = null
    private val delegatingOnMenuItemClickListener: OnMenuItemClickListener = OnMenuItemClickListener { menuItem ->
        onMenuItemClickListener?.onMenuItemClick(menuItem)
            ?.also { handled -> if (handled) selectFunction.invoke(menuItem) }
            ?: false
    }

    var selectFunction: (MenuItem) -> Unit = {}
    var deselectFunction: (MenuItem) -> Unit = {}

    init {
        super.setOnMenuItemClickListener(delegatingOnMenuItemClickListener)
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        this.onMenuItemClickListener = listener
    }

    fun performClickOn(itemId: Int): Boolean {
        (0 until menu.size()).forEach {
            val menuItem = menu.getItem(it)
            if (menuItem.itemId == itemId) {
                return delegatingOnMenuItemClickListener.onMenuItemClick(menuItem)
            }
        }

        return false
    }

    fun clearMenuSelection() {
        (0 until menu.size()).forEach {
            deselectFunction.invoke(menu.getItem(it))
        }
    }
}