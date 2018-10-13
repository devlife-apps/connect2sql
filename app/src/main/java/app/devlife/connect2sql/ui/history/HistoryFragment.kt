package app.devlife.connect2sql.ui.history

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.adapter.QueryHistoryAdapter
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.query.BaseQuery
import app.devlife.connect2sql.db.model.query.HistoryQuery
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.db.repo.HistoryQueryRepository
import app.devlife.connect2sql.fragment.BaseFragment
import app.devlife.connect2sql.lang.ensure
import app.devlife.connect2sql.ui.widget.Toast
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.fragment_query_history.listView1
import javax.inject.Inject

class HistoryFragment : BaseFragment() {

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var historyQueryRepository: HistoryQueryRepository

    var onQueryClickListener: (BaseQuery) -> Unit = {}

    private val connectionInfo: ConnectionInfo by lazy {
        val id = arguments?.getLong(EXTRA_CONNECTION_INFO_ID).ensure { t -> t != null && t > 0 }!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationUtils.getApplication(context).applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_query_history, container, false)
    }

    override fun onStart() {
        super.onStart()
        registerForContextMenu(listView1)
        listView1.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            onQueryClickListener.invoke(listView1.adapter.getItem(position) as BaseQuery)
        }
        listView1.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    // Disallow NestedScrollView to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(true);
                MotionEvent.ACTION_UP ->
                    // Allow NestedScrollView to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(false);
            }

            // Handle ListView touch events.
            v.onTouchEvent(event);

            true;
        }

        listView1.setOnCreateContextMenuListener(this)
    }

    override fun onResume() {
        super.onResume()

        val queries = historyQueryRepository.getQueryHistory(context, connectionInfo.id)
        listView1.adapter = QueryHistoryAdapter(context, queries)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.query_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item!!.itemId
        when (itemId) {
            R.id.clear -> {
                AlertDialog.Builder(context!!)
                    .setTitle(R.string.dialog_clear_history)
                    .setMessage(R.string.dialog_clear_history_confirm_msg)
                    .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                        historyQueryRepository.deleteQueryHistory(context, connectionInfo)
                        (listView1.adapter as QueryHistoryAdapter).clear()

                        Toast.makeText(this@HistoryFragment.context,
                            R.string.dialog_history_cleared,
                            Toast.LENGTH_SHORT).show()

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.dialog_no, null)
                    .create()
                    .show()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {

        super.onCreateContextMenu(menu, v, menuInfo)

        menu.setHeaderTitle("Action?")
        menu.add(0, MENU_OPEN, 0, "Open")
        menu.add(0, MENU_DELETE, 1, "Delete")
    }

    override fun onContextItemSelected(menuItem: MenuItem?): Boolean {

        val info = menuItem!!.menuInfo as AdapterView.AdapterContextMenuInfo

        val query = listView1.adapter.getItem(info.position) as HistoryQuery

        when (menuItem.itemId) {
            MENU_OPEN -> {
                onQueryClickListener.invoke(query)
                return true
            }
            MENU_DELETE -> {
                AlertDialog.Builder(context!!)
                    .setTitle(R.string.dialog_title_delete_query)
                    .setMessage(R.string.dialog_message_delete_query)
                    .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                        if (historyQueryRepository.deleteQueryHistory(context, query.id)) {
                            Toast.makeText(context, "Query Deleted!", Toast.LENGTH_SHORT).show()
                            (listView1.adapter as QueryHistoryAdapter).removeAt(info.position)
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.dialog_no, null)
                    .create()
                    .show()

                return true
            }
            else -> return super.onContextItemSelected(menuItem)
        }
    }

    companion object {
        private const val MENU_OPEN = 1
        private const val MENU_DELETE = 9
        private const val EXTRA_CONNECTION_INFO_ID = "EXTRA_CONNECTION_INFO_ID"

        fun newInstance(connectionInfoId: Long): HistoryFragment {
            return HistoryFragment().apply {
                arguments = Bundle().apply { putLong(EXTRA_CONNECTION_INFO_ID, connectionInfoId) }
            }
        }
    }
}
