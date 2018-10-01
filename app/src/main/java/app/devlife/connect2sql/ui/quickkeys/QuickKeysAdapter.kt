package app.devlife.connect2sql.ui.quickkeys

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import com.gitlab.connect2sql.R
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.parcel.Parcelize

class QuickKeysAdapter(val context: Context, sections: MutableList<Section<*, *>>) :
    ExpandableRecyclerViewAdapter<
        QuickKeysAdapter.SectionViewHolder,
        QuickKeysAdapter.QuickKeyViewHolder>(sections) {

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    var onQuickKeyClickedListener: (QuickKey<*>) -> Unit = { }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        return SectionViewHolder(
            layoutInflater.inflate(R.layout.item_expandable_group, parent, false)
        )
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): QuickKeyViewHolder {
        return QuickKeyViewHolder(
            layoutInflater.inflate(R.layout.item_quick_keys_child, parent, false)
        )
    }

    override fun onBindGroupViewHolder(holder: SectionViewHolder,
                                       flatPosition: Int,
                                       group: ExpandableGroup<*>) {
        holder.text.text = group.title
    }

    override fun onBindChildViewHolder(holder: QuickKeyViewHolder,
                                       flatPosition: Int,
                                       group: ExpandableGroup<*>,
                                       childIndex: Int) {
        val quickKey = (group as Section<*, *>).getItems()[childIndex]
        holder.root.setOnClickListener { onQuickKeyClickedListener(quickKey) }
        holder.text.text = quickKey.displayName
    }

    sealed class QuickKey<T> : Parcelable {
        abstract val displayName: String
        abstract val obj: T

        @Parcelize
        data class OfTypeString(
            override val displayName: String,
            override val obj: String) : QuickKey<String>()

        @Parcelize
        data class OfTypeSystemObject(
            override val displayName: String,
            override val obj: DriverAgent.SystemObject) : QuickKey<DriverAgent.SystemObject>()
    }

    class Section<Q : QuickKey<T>, T>(title: String, quickKeys: MutableList<Q>) :
        ExpandableGroup<Q>(title, quickKeys)

    class SectionViewHolder(itemView: View) : GroupViewHolder(itemView) {
        val root = itemView
        val text: TextView = itemView.findViewById(R.id.text1)

        override fun expand() {
            text.isSelected = true
        }

        override fun collapse() {
            text.isSelected = false
        }
    }

    class QuickKeyViewHolder(itemView: View) : ChildViewHolder(itemView) {
        val root = itemView
        val text: TextView = itemView.findViewById(R.id.text1)
    }
}