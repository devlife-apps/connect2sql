package app.devlife.connect2sql.adapter

import app.devlife.connect2sql.db.model.query.BaseNamedQuery
import app.devlife.connect2sql.db.model.query.BuiltInQuery
import com.gitlab.connect2sql.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import app.devlife.connect2sql.db.model.query.SavedQuery

class SavedQueriesAdapter(private val context: Context) : BaseExpandableListAdapter() {
    var titleOnly = false
    private val inflator = LayoutInflater.from(context)
    private val queries: List<MutableList<BaseNamedQuery>> = listOf(arrayListOf(), arrayListOf())

    fun addToBuiltInQueries(query: BuiltInQuery) {
        queries[GROUP_BUILTIN].add(query)
    }

    fun addToSavedQueries(query: SavedQuery) {
        queries[GROUP_SAVED].add(query)
    }

    fun removeSavedQuery(query: SavedQuery) {
        queries[GROUP_SAVED].remove(query)
    }

    fun clear() {
        queries[GROUP_BUILTIN].clear()
        queries[GROUP_SAVED].clear()
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return (getGroup(groupPosition) as List<*>)[childPosition]!!
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view: View = when (convertView) {
            null -> {
                val view = inflator.inflate(R.layout.item_query_list_child, null)
                view.setPadding(80, 10, 2, 10)
                view
            }
            else -> convertView
        }

        val query = queries[groupPosition][childPosition]

        (view.findViewById(R.id.lblQueryName) as TextView).text = query.name
        (view.findViewById(R.id.lblQueryText) as TextView).text = query.query
        (view.findViewById(R.id.lblQueryText) as TextView).visibility = if (titleOnly) View.GONE else View.VISIBLE

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return (getGroup(groupPosition) as List<*>).size
    }

    override fun getGroup(groupPosition: Int): Any {
        return queries[groupPosition]
    }

    override fun getGroupCount(): Int {
        return queries.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view: View = when (convertView) {
            null -> {
                val view = inflator.inflate(android.R.layout.simple_expandable_list_item_1, null)
                view.setPadding(80, 15, 2, 15)
                view
            }
            else -> convertView
        }

        val textView = view.findViewById(android.R.id.text1) as TextView
        when (groupPosition) {
            GROUP_BUILTIN -> textView.text = "Built In"
            else -> textView.text = "Saved"
        }

        return view
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    companion object {

        // This is basically the position of each group
        val GROUP_BUILTIN = 0
        val GROUP_SAVED = 1
    }
}
