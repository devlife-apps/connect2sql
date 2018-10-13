package app.devlife.connect2sql.ui.browse

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import com.gitlab.connect2sql.R


class BrowseAdapter(
    private val databasesAdapter: BrowseDatabasesAdapter,
    private val tablesAdapter: BrowseTablesAdapter,
    private val theme: Resources.Theme?
) :
    RecyclerView.Adapter<BrowseAdapter.ViewHolder>() {

    var selectedDatabase: DriverAgent.Database? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedTable: DriverAgent.Table? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_browse_group, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return ChildAdapterMeta.values().size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val resources = viewHolder.root.resources
        viewHolder.root.setOnClickListener {
            viewHolder.recyclerView.visibility = when (viewHolder.recyclerView.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        viewHolder.recyclerView.apply {
            addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {
                    // ignored
                }

                override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
                    // ignored
                }

                override fun onInterceptTouchEvent(v: RecyclerView, event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_MOVE) {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    return false
                }
            })
        }

        viewHolder.apply {
            when (ChildAdapterMeta.fromPosition(position)) {
                ChildAdapterMeta.DATABASES -> {
                    title.setText(R.string.item_browse_databases)
                    subtitle.text = with(selectedDatabase) {
                        return@with when (this) {
                            null -> resources.getString(
                                R.string.item_browse_selected_,
                                resources.getString(R.string.item_browse_none_selected))
                            else -> resources.getString(
                                R.string.item_browse_selected_,
                                this.name)
                        }
                    }
                    icon.setImageDrawable(resources.getDrawable(R.drawable.ic_database, theme))
                    if (recyclerView.adapter == null) {
                        recyclerView.adapter = databasesAdapter
                    }
                }
                ChildAdapterMeta.TABLES -> {
                    title.setText(R.string.item_browse_tables)
                    subtitle.text = with(selectedTable) {
                        return@with when (this) {
                            null ->
                                resources.getString(
                                    R.string.item_browse_selected_,
                                    resources.getString(R.string.item_browse_none_selected))
                            else ->
                                resources.getString(R.string.item_browse_selected_, this.name)
                        }
                    }
                    icon.setImageDrawable(
                        resources.getDrawable(R.drawable.ic_table_type_table, theme))

                    if (recyclerView.adapter == null) {
                        recyclerView.adapter = tablesAdapter
                    }
                }
                else -> TODO()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView
        val icon: ImageView = itemView.findViewById(R.id.item_icon)
        val title: TextView = itemView.findViewById(R.id.item_title)
        val subtitle: TextView = itemView.findViewById(R.id.item_subtitle)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.item_recycler_view)
    }

    enum class ChildAdapterMeta(val position: Int) {
        DATABASES(0),
        TABLES(1);

        companion object {
            fun fromPosition(position: Int): ChildAdapterMeta? {
                return values().firstOrNull { it.position == position }
            }
        }
    }
}