package app.devlife.connect2sql.ui.browse

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.fragment.BaseFragment
import app.devlife.connect2sql.lang.ensure
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.util.ext.disableParentInterception
import app.devlife.connect2sql.util.ext.onClickToggleVisibilityOf
import app.devlife.connect2sql.viewmodel.ConnectionViewModel
import app.devlife.connect2sql.viewmodel.ViewModelFactory
import com.gitlab.connect2sql.R
import javax.inject.Inject

class BrowseFragment : BaseFragment() {

    private val connectionInfo: ConnectionInfo by lazy {
        val id = arguments?.getLong(EXTRA_CONNECTION_INFO_ID).ensure { t -> t != null && t > 0 }!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    private val connectionViewModel: ConnectionViewModel by lazy {
        ViewModelProviders.of(activity!!, viewModelFactory).get(ConnectionViewModel::class.java)
    }

    private val noneSelectedSubtitleText by lazy {
        resources.getString(R.string.item_browse_selected_,
            resources.getString(R.string.item_browse_none_selected))
    }

    private val databasesAdapter: BrowseDatabasesAdapter = BrowseDatabasesAdapter()
    private val tablesAdapter: BrowseTablesAdapter = BrowseTablesAdapter()

    private lateinit var databasesViewHolder: ViewHolder
    private lateinit var tablesViewHolder: ViewHolder

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    var onTableSelectedListener: (DriverAgent.Table) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationUtils.getApplication(context).applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_browse, container, false)

        databasesViewHolder = ViewHolder(view.findViewById(R.id.browse_databases)).apply {
            root.onClickToggleVisibilityOf(recyclerView)
            icon.setImageDrawable(resources.getDrawable(R.drawable.ic_database, null))
            title.setText(R.string.item_browse_databases)
            subtitle.text = noneSelectedSubtitleText
            recyclerView.disableParentInterception()
            recyclerView.adapter = databasesAdapter
        }

        tablesViewHolder = ViewHolder(view.findViewById(R.id.browse_tables)).apply {
            root.onClickToggleVisibilityOf(recyclerView)
            icon.setImageDrawable(resources.getDrawable(R.drawable.ic_table_type_table, null))
            title.setText(R.string.item_browse_tables)
            subtitle.text = noneSelectedSubtitleText
            recyclerView.disableParentInterception()
            recyclerView.adapter = tablesAdapter
        }

        databasesAdapter.onItemClickListener = { database ->
            connectionViewModel.setSelectedDatabase(database)
            databasesViewHolder.recyclerView.visibility = View.GONE
            tablesViewHolder.recyclerView.visibility = View.VISIBLE
        }
        tablesAdapter.onItemClickListener = { table ->
            connectionViewModel.setSelectedTable(table)
            tablesViewHolder.recyclerView.visibility = View.GONE

            onTableSelectedListener.invoke(table)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        activity?.apply {
            connectionViewModel.selectedDatabase.observe(this, Observer { database ->
                databasesViewHolder.subtitle.text = when (database) {
                    null -> noneSelectedSubtitleText
                    else -> resources.getString(
                        R.string.item_browse_selected_,
                        database.name)
                }
            })
            connectionViewModel.selectedTable.observe(this, Observer { table ->
                tablesViewHolder.subtitle.text = when (table) {
                    null -> noneSelectedSubtitleText
                    else -> resources.getString(
                        R.string.item_browse_selected_,
                        table.name)
                }
            })
            connectionViewModel.databases.observe(this, Observer { newDatabases ->
                databasesAdapter.databases.apply {
                    clear()
                    newDatabases?.also { addAll(newDatabases) }
                }
                databasesAdapter.notifyDataSetChanged()
            })
            connectionViewModel.tables.observe(this, Observer { newTables ->
                tablesAdapter.tables.apply {
                    clear()
                    newTables?.also { addAll(newTables) }
                }
                tablesAdapter.notifyDataSetChanged()
            })
        }
    }

    data class ViewHolder(val root: View) {
        val icon: ImageView = root.findViewById(R.id.item_icon)
        val title: TextView = root.findViewById(R.id.item_title)
        val subtitle: TextView = root.findViewById(R.id.item_subtitle)
        val recyclerView: RecyclerView = root.findViewById(R.id.item_recycler_view)
    }

    companion object {
        private const val EXTRA_CONNECTION_INFO_ID = "EXTRA_CONNECTION_INFO_ID"

        fun newInstance(connectionInfoId: Long): BrowseFragment {
            return BrowseFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
                }
            }
        }
    }
}