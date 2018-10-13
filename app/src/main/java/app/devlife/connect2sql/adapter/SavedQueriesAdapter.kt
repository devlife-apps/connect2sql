package app.devlife.connect2sql.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import app.devlife.connect2sql.db.model.query.BaseNamedQuery
import app.devlife.connect2sql.db.model.query.BuiltInQuery
import app.devlife.connect2sql.db.model.query.SavedQuery
import com.gitlab.connect2sql.R

class SavedQueriesAdapter(private val context: Context) : BaseExpandableListAdapter() {
    var titleOnly = false
    private val inflator = LayoutInflater.from(context)
    private val builtInText = context.getString(R.string.saved_queries_built_in)
    private val savedText = context.getString(R.string.saved_queries_saved)
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

    fun clearSavedQueries() {
        queries[GROUP_SAVED].clear()
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
            null -> inflator.inflate(R.layout.item_query_list_child, null)
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
            null -> inflator.inflate(R.layout.item_expandable_group, null)
            else -> convertView
        }

        val textView = view.findViewById(R.id.text1) as TextView
        when (groupPosition) {
            GROUP_BUILTIN -> textView.text = builtInText
            else -> textView.text = savedText
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
        const val GROUP_BUILTIN = 0
        const val GROUP_SAVED = 1
    }
}
