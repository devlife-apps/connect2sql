package app.devlife.connect2sql.ui.savedqueries

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
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
import app.devlife.connect2sql.prefs.UserPreferences
import app.devlife.connect2sql.prefs.UserPreferences.Option.BooleanOption
import app.devlife.connect2sql.ui.widget.Toast
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.activity_saved_queries.listview_saved_queries
import javax.inject.Inject

class SavedFragment : BaseFragment() {

    private val savedQueryContentUri: Uri = ContentUriHelper.getContentUri(SavedQuery.SavedQuerySqlModel::class.java)
    private val builtinQueryContentUri: Uri = ContentUriHelper.getContentUri(BuiltInQuery.BuiltInQuerySqlModel::class.java)

    private val savedQueriesAdapter: SavedQueriesAdapter by lazy { SavedQueriesAdapter(context!!) }

    private val connectionInfo: ConnectionInfo by lazy {
        val id = arguments?.getLong(EXTRA_CONNECTION_INFO_ID).ensure { t -> t != null && t > 0 }!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var savedQueryRepository: SavedQueryRepository
    @Inject
    lateinit var builtInQuerySqlModel: BuiltInQuery.BuiltInQuerySqlModel
    @Inject
    lateinit var savedQuerySqlModel: SavedQuery.SavedQuerySqlModel
    @Inject
    lateinit var userPreferences: UserPreferences

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
    }

    override fun onResume() {
        super.onResume()

        savedQueriesAdapter.clear()

        val cursor1 = context?.contentResolver?.query(savedQueryContentUri, null,
            SavedQuery.Column.CONNECTION_ID + "=" + connectionInfo.id, null,
            SavedQuery.Column.NAME + " ASC")

        if (cursor1 != null) {
            while (cursor1.moveToNext()) {
                savedQueriesAdapter.addToSavedQueries(savedQuerySqlModel.hydrateObject(cursor1))
            }
            cursor1.close()
        }

        val cursor2 = context?.contentResolver?.query(builtinQueryContentUri, null,
            BuiltInQuery.Column.DRIVER + " = ?",
            arrayOf(connectionInfo.driverType.name),
            BuiltInQuery.Column.NAME + " ASC")

        if (cursor2 != null) {
            while (cursor2.moveToNext()) {
                savedQueriesAdapter.addToBuiltInQueries(builtInQuerySqlModel.hydrateObject(cursor2))
            }
            cursor2.close()
        }

        savedQueriesAdapter.titleOnly = userPreferences.read(OPTION_SAVED_QUERY_NAMES_ONLY, false)
        savedQueriesAdapter.notifyDataSetChanged()

        if (savedQueriesAdapter.getChildrenCount(SavedQueriesAdapter.GROUP_SAVED) == 0) {
            listview_saved_queries.expandGroup(SavedQueriesAdapter.GROUP_BUILTIN)
        }

        userPreferences.registerListener<Boolean>(OPTION_SAVED_QUERY_NAMES_ONLY) { _, value ->
            savedQueriesAdapter.titleOnly = value == true
            savedQueriesAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.query_saved, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        menu?.findItem(R.id.menu_name_only)?.isChecked =
            userPreferences.read(OPTION_SAVED_QUERY_NAMES_ONLY, false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_name_only -> {
                userPreferences.save(BooleanOption(OPTION_SAVED_QUERY_NAMES_ONLY, !item.isChecked))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        private const val OPTION_SAVED_QUERY_NAMES_ONLY = "OPTION_SAVED_QUERY_NAMES_ONLY"
        private const val MENU_OPEN = 1
        private const val MENU_DELETE = 9

        fun newInstance(connectionInfoId: Long): SavedFragment {
            return SavedFragment().apply {
                arguments = Bundle().apply { putLong(EXTRA_CONNECTION_INFO_ID, connectionInfoId) }
            }
        }
    }
}
