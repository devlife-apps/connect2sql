package app.devlife.connect2sql.ui.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_query_history.*
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.activity.BaseActivity
import app.devlife.connect2sql.adapter.QueryHistoryAdapter
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.query.BaseQuery
import app.devlife.connect2sql.db.model.query.HistoryQuery
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.db.repo.HistoryQueryRepository
import com.gitlab.connect2sql.R
import app.devlife.connect2sql.lang.ensure
import app.devlife.connect2sql.ui.widget.Toast
import javax.inject.Inject

class QueryHistoryActivity : BaseActivity() {

    private val connectionInfo: ConnectionInfo by lazy {
        val id = intent?.extras?.getLong(EXTRA_CONNECTION_INFO_ID).ensure({ t -> t != null && t > 0 })!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var mHistoryQueryRepository: HistoryQueryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_query_history)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        registerForContextMenu(listView1)
        listView1.onItemClickListener = onItemClickListener
        listView1.setOnCreateContextMenuListener(this)
    }

    override fun onResume() {
        super.onResume()

        val queries = mHistoryQueryRepository.getQueryHistory(this, connectionInfo.id.toLong())
        listView1.adapter = QueryHistoryAdapter(this, queries)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.query_history, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item!!.itemId
        when (itemId) {
            R.id.clear -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Clear History?")
                builder.setMessage("Are you sure you want to clear all history for this connection?")
                builder.setPositiveButton("Yes") { dialog, which ->
                    mHistoryQueryRepository.deleteQueryHistory(this, connectionInfo)
                    (listView1.adapter as QueryHistoryAdapter).clear()

                    Toast.makeText(this@QueryHistoryActivity, "History cleared", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                builder.setNegativeButton("No", null)
                builder.create().show()
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
                onItemClickListener.onItemClick(listView1, null, info.position, 0)
                return true
            }
            MENU_DELETE -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete Query?")
                builder.setMessage("Are you sure you want to delete this query?")
                builder.setPositiveButton("Yes") { dialog, which ->
                    if (mHistoryQueryRepository.deleteQueryHistory(this, query.id)) {
                        Toast.makeText(this, "Query Deleted!", Toast.LENGTH_SHORT).show()
                        (listView1.adapter as QueryHistoryAdapter).removeAt(info.position)
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

    private val onItemClickListener: AdapterView.OnItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val query = (listView1.adapter.getItem(position) as BaseQuery).query

            val data = Intent()
            data.putExtra(RESULT_QUERY, query)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    companion object {
        val RESULT_QUERY = "RESULT_QUERY"

        private val MENU_OPEN = 1
        private val MENU_DELETE = 9
        private val EXTRA_CONNECTION_INFO_ID = "EXTRA_CONNECTION_INFO"

        fun newIntent(context: Context, connectionInfoId: Long): Intent {
            val intent = Intent(context, QueryHistoryActivity::class.java)
            intent.putExtra(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
            return intent
        }
    }
}
