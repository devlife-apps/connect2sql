package app.devlife.connect2sql.ui.browse

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import com.gitlab.connect2sql.R

class BrowseDatabasesAdapter : RecyclerView.Adapter<BrowseDatabasesAdapter.ViewHolder>() {

    val databases = mutableListOf<DriverAgent.Database>()
    var onItemClickListener: (DriverAgent.Database) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_browse_child_database, parent, false)
        )
    }

    override fun getItemCount(): Int = databases.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.apply {
            root.setOnClickListener { onItemClickListener.invoke(databases[position]) }
            title.text = databases[position].name
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView
        val title: TextView = itemView.findViewById(R.id.item_title)
    }
}