package app.devlife.connect2sql.ui.browse

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import com.gitlab.connect2sql.R

class BrowseTablesAdapter : RecyclerView.Adapter<BrowseTablesAdapter.ViewHolder>() {

    val tables = mutableListOf<DriverAgent.Table>()
    var onItemClickListener: (DriverAgent.Table) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_browse_child_table, parent, false)
        )
    }

    override fun getItemCount(): Int = tables.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.apply {
            root.setOnClickListener { onItemClickListener.invoke(tables[position]) }
            title.text = tables[position].name
            subtitle.text = when (tables[position].type) {
                DriverAgent.TableType.TABLE -> subtitle.resources.getText(R.string.table_list_type_table)
                DriverAgent.TableType.VIEW -> subtitle.resources.getText(R.string.table_list_type_view)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView
        val icon: ImageView = itemView.findViewById(R.id.item_icon)
        val title: TextView = itemView.findViewById(R.id.item_title)
        val subtitle: TextView = itemView.findViewById(R.id.item_subtitle)
    }
}