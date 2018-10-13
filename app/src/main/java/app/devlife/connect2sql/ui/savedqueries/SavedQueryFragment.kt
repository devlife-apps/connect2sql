package app.devlife.connect2sql.ui.savedqueries

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.adapter.SavedQueriesAdapter
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.query.BaseNamedQuery
import app.devlife.connect2sql.db.model.query.BaseQuery
import app.devlife.connect2sql.db.model.query.BuiltInQuery
import app.devlife.connect2sql.db.model.query.SavedQuery
import app.devlife.connect2sql.db.provider.ContentUriHelper
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.db.repo.SavedQueryRepository
import app.devlife.connect2sql.fragment.BaseFragment
import app.devlife.connect2sql.lang.ensure
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.viewmodel.SavedQueriesViewModel
import app.devlife.connect2sql.viewmodel.ViewModelFactory
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.activity_saved_queries.listview_saved_queries
import javax.inject.Inject

class SavedQueryFragment : BaseFragment() {


    private val builtinQueryContentUri: Uri = ContentUriHelper.getContentUri(BuiltInQuery.BuiltInQuerySqlModel::class.java)

    private val savedQueriesAdapter: SavedQueriesAdapter by lazy { SavedQueriesAdapter(context!!) }

    private val connectionInfo: ConnectionInfo by lazy {
        val id = arguments?.getLong(EXTRA_CONNECTION_INFO_ID).ensure { t -> t != null && t > 0 }!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    private val savedQueriesViewModel: SavedQueriesViewModel by lazy {
        ViewModelProviders.of(activity!!, viewModelFactory).get(SavedQueriesViewModel::class.java)
    }

    private var contentObserver: ContentObserver? = null

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var savedQueryRepository: SavedQueryRepository
    @Inject
    lateinit var builtInQuerySqlModel: BuiltInQuery.BuiltInQuerySqlModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    var onQueryClickListener: (BaseQuery) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationUtils.getApplication(context).applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_saved_queries, container, false)
    }

    override fun onStart() {
        super.onStart()

        registerForContextMenu(listview_saved_queries)

        listview_saved_queries.setAdapter(savedQueriesAdapter)
        listview_saved_queries.expandGroup(SavedQueriesAdapter.GROUP_SAVED)
        listview_saved_queries.setOnChildClickListener(mOnChildClickListener)
        listview_saved_queries.setOnCreateContextMenuListener(this)

        savedQueriesAdapter.clear()

        savedQueriesViewModel.getSavedQueries(connectionInfo.id)
            .observe(this, Observer { savedQueries ->
                savedQueriesAdapter.clearSavedQueries()
                savedQueries?.forEach { savedQueriesAdapter.addToSavedQueries(it) }
                savedQueriesAdapter.notifyDataSetChanged()
            })

        context?.contentResolver?.query(
            builtinQueryContentUri,
            null,
            "${BuiltInQuery.Column.DRIVER} = ?",
            arrayOf(connectionInfo.driverType.name),
            "${BuiltInQuery.Column.NAME} ASC")
            ?.let { cursor ->
                while (cursor.moveToNext()) {
                    savedQueriesAdapter.addToBuiltInQueries(
                        builtInQuerySqlModel.hydrateObject(cursor))
                }
                cursor.close()
            }

        savedQueriesAdapter.notifyDataSetChanged()

        if (savedQueriesAdapter.getChildrenCount(SavedQueriesAdapter.GROUP_SAVED) == 0) {
            listview_saved_queries.expandGroup(SavedQueriesAdapter.GROUP_BUILTIN)
        }
    }

    override fun onStop() {
        contentObserver?.also { context?.contentResolver?.unregisterContentObserver(it) }
        super.onStop()
    }

    override fun onCreateContextMenu(menu: ContextMenu,
                                     v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as ExpandableListView.ExpandableListContextMenuInfo
        val type = ExpandableListView.getPackedPositionType(info.packedPosition)
        val groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition)
        val childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition)

        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

            val query = savedQueriesAdapter.getChild(groupPosition, childPosition) as BaseNamedQuery

            menu.setHeaderTitle("Query: " + query.name)
            menu.add(0, MENU_OPEN, 0, "Open")
            if (groupPosition == SavedQueriesAdapter.GROUP_SAVED) {
                // only offer delete to saved queries
                menu.add(0, MENU_DELETE, 1, "Delete")
            }
        }
    }

    data class Position(val groupPosition: Int, val childPosition: Int)

    override fun onContextItemSelected(menuItem: MenuItem): Boolean {
        val info = menuItem.menuInfo as ExpandableListView.ExpandableListContextMenuInfo

        val position = when (ExpandableListView.getPackedPositionType(info.packedPosition)) {
            ExpandableListView.PACKED_POSITION_TYPE_CHILD ->
                Position(ExpandableListView.getPackedPositionGroup(info.packedPosition),
                    ExpandableListView.getPackedPositionChild(info.packedPosition))
            else -> Position(0, 0)
        }

        val query = savedQueriesAdapter.getChild(position.groupPosition,
            position.childPosition) as BaseNamedQuery

        when (menuItem.itemId) {
            MENU_OPEN -> {
                mOnChildClickListener.onChildClick(
                    listview_saved_queries,
                    null,
                    position.groupPosition,
                    position.childPosition,
                    0)
                return true
            }
            MENU_DELETE -> {
                val builder = AlertDialog.Builder(context!!)
                    .setTitle("Delete: " + query.name)
                    .setMessage("Are you sure you want to delete this query?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        if (savedQueryRepository.deleteSavedQuery(context, query.id)) {
                            Toast.makeText(context, "Query Deleted!", Toast.LENGTH_SHORT).show()
                            savedQueriesAdapter.removeSavedQuery(query as SavedQuery)
                            savedQueriesAdapter.notifyDataSetChanged()
                        }

                        dialog.dismiss()
                    }

                builder.setNegativeButton("No", null)
                builder.create().show()
                return true
            }
            else -> return super.onContextItemSelected(menuItem)
        }
    }

    private val mOnChildClickListener = ExpandableListView.OnChildClickListener { _, _, groupPosition, childPosition, _ ->
        val queryToLoad = (savedQueriesAdapter.getChild(groupPosition,
            childPosition) as BaseNamedQuery)
        onQueryClickListener.invoke(queryToLoad)
        true
    }

    companion object {

        private const val EXTRA_CONNECTION_INFO_ID = "EXTRA_CONNECTION_INFO_ID"
        private const val MENU_OPEN = 1
        private const val MENU_DELETE = 9

        fun newInstance(connectionInfoId: Long): SavedQueryFragment {
            return SavedQueryFragment().apply {
                arguments = Bundle().apply { putLong(EXTRA_CONNECTION_INFO_ID, connectionInfoId) }
            }
        }
    }
}
