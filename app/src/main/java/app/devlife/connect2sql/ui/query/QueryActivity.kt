package app.devlife.connect2sql.ui.query

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.activity.BaseActivity
import app.devlife.connect2sql.adapter.QuickKeysAdapter
import app.devlife.connect2sql.adapter.TableListAdapter
import app.devlife.connect2sql.connection.ConnectionAgent
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.query.SavedQuery
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.db.repo.HistoryQueryRepository
import app.devlife.connect2sql.db.repo.SavedQueryRepository
import app.devlife.connect2sql.lang.ensure
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.prefs.UserPreferences
import app.devlife.connect2sql.prefs.UserPreferences.Option.BooleanOption
import app.devlife.connect2sql.sql.DriverType
import app.devlife.connect2sql.sql.Table
import app.devlife.connect2sql.sql.driver.agent.DefaultDriverAgent
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.sql.driver.agent.DriverAgent.TableType.VIEW
import app.devlife.connect2sql.sql.driver.helper.DriverHelper
import app.devlife.connect2sql.sql.driver.helper.DriverHelperFactory
import app.devlife.connect2sql.ui.history.QueryHistoryActivity
import app.devlife.connect2sql.ui.results.ResultsActivity
import app.devlife.connect2sql.ui.savedqueries.SavedQueriesActivity
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.util.rx.ActivityAwareSubscriber
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.activity_query.fab
import kotlinx.android.synthetic.main.activity_query.lblCurrentDatabase
import kotlinx.android.synthetic.main.activity_query.lblCurrentTable
import kotlinx.android.synthetic.main.activity_query.txtQuery
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.sql.Connection
import java.util.ArrayList
import javax.inject.Inject

class QueryActivity : BaseActivity() {

    private val connectionInfo: ConnectionInfo by lazy {
        val id = intent?.extras?.getLong(EXTRA_CONNECTION_INFO_ID).ensure({ t -> t != null && t > 0 })!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    private var currentDatabase: String? = null
    private var currentTable: String? = null
    private val currentColumns: MutableList<DriverAgent.Column> = arrayListOf()
    private val serverGraph: MutableMap<DriverAgent.Database, List<DriverAgent.Table>> = hashMapOf()

    private lateinit var driverHelper: DriverHelper
    private lateinit var driverAgent: DefaultDriverAgent
    private lateinit var quickKeysAdapter: QuickKeysAdapter
    private lateinit var tableAdapter: TableListAdapter
    private lateinit var databaseAdapter: ArrayAdapter<String>
    private lateinit var databaseDialog: Dialog
    private lateinit var tableDialog: Dialog

    private var mQuickKeysList: ExpandableListView? = null
    private var mQueryToLoad: String? = null
    private var mHasInlineQuickKeys: Boolean = false
    private var mIsQuickKeysHidden: Boolean = false
    private var mDialogQuickKeys: Dialog? = null

    private var serverGraphSubscription: Subscription? = null

    @Inject
    lateinit var mConnectionAgent: ConnectionAgent
    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var mHistoryQueryRepository: HistoryQueryRepository
    @Inject
    lateinit var mSavedQueryRepository: SavedQueryRepository
    @Inject
    lateinit var mUserPreferences: UserPreferences

    private fun readExtras(extras: Bundle?) {
        if (extras == null) {
            return
        }

        currentDatabase = extras.getString(SAVED_DATABASE)
        currentTable = extras.getString(SAVED_TABLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        if (savedInstanceState != null) {
            readExtras(savedInstanceState)
        }

        driverHelper = DriverHelperFactory.create(connectionInfo.driverType)!!
        driverAgent = DefaultDriverAgent(driverHelper)

        fab.setOnClickListener { executeQuery() }

        /**
         * Add operators to quick keys
         */
        quickKeysAdapter = QuickKeysAdapter(this)

        val res = resources
        for (i in res.getStringArray(R.array.operators)) {
            quickKeysAdapter.addChild(i, QuickKeysAdapter.SECTION_OPERATORS)
        }

        for (i in res.getStringArray(R.array.snippets)) {
            quickKeysAdapter.addChild(i, QuickKeysAdapter.SECTION_SNIPPETS)
        }

        quickKeysAdapter.listItemLayout = R.layout.widget_quickkeys_text

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)

        /***
         * Build table list dialog
         */
        tableAdapter = TableListAdapter(this, ArrayList<Table>())

        val tableList = ListView(this)
        tableList.adapter = tableAdapter
        tableList.onItemClickListener = OnTableSelected()

        tableDialog = AppCompatDialog(this)
        tableDialog.setTitle("Select Table")
        tableDialog.setContentView(tableList, params)

        /***
         * Build database list dialog
         */
        databaseAdapter = ArrayAdapter<String>(this, R.layout.item_simple_text_1)

        val databasesList = ListView(this)
        databasesList.adapter = databaseAdapter
        databasesList.onItemClickListener = OnDatabaseSelected()

        databaseDialog = AppCompatDialog(this)
        databaseDialog.setTitle("Select Database")
        databaseDialog.setContentView(databasesList, params)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)

        // this only lives in tablet sized
        mQuickKeysList = findViewById(R.id.elvQuickKeys) as? ExpandableListView
        if (mQuickKeysList != null) {
            mHasInlineQuickKeys = true
        } else {
            // create a dialog version
            mDialogQuickKeys = AppCompatDialog(this)
            mDialogQuickKeys!!.setTitle("Quick Keys")
            mDialogQuickKeys!!.setOnShowListener { dialog ->
                val d = dialog as Dialog
                d.window.setBackgroundDrawable(ColorDrawable(0))
            }

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quick_keys, null)
            val clear = dialogView.findViewById(R.id.button1) as Button
            clear.setOnClickListener { clearQuery() }
            mQuickKeysList = dialogView.findViewById(R.id.list) as ExpandableListView
            mQuickKeysList!!.setBackgroundColor(Color.TRANSPARENT)
            mQuickKeysList!!.cacheColorHint = Color.TRANSPARENT
            mDialogQuickKeys!!.setContentView(dialogView)
        }

        /*****************************
         * Set view display
         */
        lblCurrentDatabase.setText(R.string.query_loading)
        lblCurrentTable.setText(R.string.query_loading)

        mQuickKeysList!!.setAdapter(quickKeysAdapter)

        /*****************************
         * Attach event listeners to objects
         */

        lblCurrentDatabase.setOnClickListener {
            if (connectionInfo.driverType == DriverType.POSTGRES) {
                Toast.makeText(this@QueryActivity, getString(R.string.error_postgres_changing_databases), Toast.LENGTH_SHORT).show()
            } else if (databaseAdapter.isEmpty) {
                Toast.makeText(this@QueryActivity, getString(R.string.error_no_detected_databases), Toast.LENGTH_SHORT).show()
            } else {
                databaseDialog.show()
            }
        }

        lblCurrentTable.setOnClickListener {
            if (currentDatabase == null) {
                Toast.makeText(this@QueryActivity, getString(R.string.error_select_database), Toast.LENGTH_SHORT).show()
            } else if (tableAdapter.isEmpty) {
                Toast.makeText(this@QueryActivity, getString(R.string.error_no_detected_tables), Toast.LENGTH_SHORT).show()
            } else {
                tableDialog.show()
            }
        }

        /***
         * Quick keys
         */

        // quick keys
        mQuickKeysList!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val text = quickKeysAdapter.getChild(groupPosition, childPosition).toString()

            when (groupPosition) {
                QuickKeysAdapter.SECTION_DATABASES ->
                    driverHelper.safeObject(DriverAgent.Database(text))
                QuickKeysAdapter.SECTION_TABLES ->
                    driverHelper.safeObject(DriverAgent.Table(text))
                QuickKeysAdapter.SECTION_COLUMNS ->
                    driverHelper.safeObject(DriverAgent.Column(text))
                else -> text
            }.also { txtQuery.append("$it ") }

            true
        }

        if (!TextUtils.isEmpty(connectionInfo.database)) {
            currentDatabase = connectionInfo.database
        }
    }

    override fun onStart() {
        super.onStart()

        txtQuery.clearFocus()
        txtQuery.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.query_top, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        EzLogger.d("Has Inline QuickKey: " + mHasInlineQuickKeys)
        EzLogger.d("Use QK Hidden: " + mIsQuickKeysHidden)

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.save -> {
                showSaveQueryDialog()
                return true
            }
            R.id.open_saved -> {
                startActivityForResult(SavedQueriesActivity.newIntent(this, connectionInfo.id), REQUEST_SAVED_QUERY)
                return true
            }
            R.id.open_history -> {
                startActivityForResult(QueryHistoryActivity.newIntent(this, connectionInfo.id), REQUEST_HISTORY_QUERY)
                return true
            }
            R.id.quick_keys -> {
                if (mIsQuickKeysHidden) {
                    showQuickKeys()
                } else {
                    hideQuickKeys()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    public override fun onResume() {
        super.onResume()

        // load preference
        mIsQuickKeysHidden = mUserPreferences.read(PREF_IS_QUICK_KEYS_HIDDEN, false)

        EzLogger.d("Use QK Hidden: " + mIsQuickKeysHidden)

        if (mIsQuickKeysHidden) {
            hideQuickKeys()
        } else if (mHasInlineQuickKeys) {
            hideQuickKeys()
            showQuickKeys()
        }

        if (serverGraphSubscription == null) {
            serverGraphSubscription = retrieveServerGraph()
        }
    }

    override fun onStop() {
        serverGraphSubscription?.unsubscribe()
        serverGraphSubscription = null
        super.onStop()
    }

    private fun retrieveServerGraph(): Subscription {
        EzLogger.d("[retrieveServerGraph]")
        return mConnectionAgent
            .connect(connectionInfo)
            .flatMap<Pair<Connection, DriverAgent.Database>> { connection ->
                when {
                    serverGraphSubscription == null -> Observable.empty()
                    !connectionInfo.database.isNullOrBlank() -> {
                        Observable.just(Pair(connection, DriverAgent.Database(connectionInfo.database!!)))
                    }
                    else -> driverAgent.databases(connection).map { Pair(connection, it) }
                }
            }
            .flatMap<Pair<DriverAgent.Database, DriverAgent.Table>> { (connection, database) ->
                EzLogger.v("[call] database retrieved=$database")
                if (serverGraphSubscription == null) Observable.empty()
                else driverAgent
                    .tables(connection, database)
                    .map<Pair<DriverAgent.Database, DriverAgent.Table>> { table ->
                        EzLogger.v("[call] table retrieved=$database/$table")
                        Pair(database, table)
                    }
            }
            .collect<MutableMap<DriverAgent.Database, MutableList<DriverAgent.Table>>>(
                {
                    hashMapOf()
                },
                { aggregation, databaseTable ->
                    val database = databaseTable.first
                    val table = databaseTable.second

                    if (!aggregation.containsKey(database)) {
                        aggregation.put(database, arrayListOf<DriverAgent.Table>())
                    }

                    aggregation[database]?.add(table)
                }
            )
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ActivityAwareSubscriber(
                activity = this@QueryActivity,
                delegate = object : Subscriber<Map<DriverAgent.Database, List<DriverAgent.Table>>>() {
                    override fun onCompleted() {
                        EzLogger.v("[onComplete]")
                    }

                    override fun onError(e: Throwable) {
                        EzLogger.e("[onError] error=" + e.message, e)
                        val builder = AlertDialog.Builder(this@QueryActivity)
                        builder.setTitle("Error")
                        builder.setMessage(e.message)
                        builder.setNeutralButton("OK", null)
                        builder.create().show()
                    }

                    override fun onNext(graph: Map<DriverAgent.Database, List<DriverAgent.Table>>) {
                        EzLogger.d("[onNext] databases=" + graph.keys)

                        serverGraph.clear()
                        serverGraph.putAll(graph)

                        onServerGraphRetrieved()
                    }
                }))
    }

    private fun onServerGraphRetrieved() {
        EzLogger.d("[onServerGraphRetrieved]")
        loadDatabaseTableSelectionDialogs()
        loadCurrentDatabaseTableUi()
        loadQuickKeys()
        retrieveColumns()
    }

    private fun loadQueryIfAny() {
        if (!TextUtils.isEmpty(mQueryToLoad)) {
            val databaseName = currentDatabase?.let { DriverAgent.Database(it) }?.let { driverHelper.safeObject(it) }
                ?: ""
            val tableName = currentTable?.let { DriverAgent.Table(it) }?.let { driverHelper.safeObject(it) }
                ?: ""

            var columnsCsv = ""
            if (!currentColumns.isEmpty()) {
                val columns = currentColumns.map { col -> col.name }

                columnsCsv = columns.joinToString()
            }

            // process replacements
            mQueryToLoad = mQueryToLoad!!.replace("{~database~}", databaseName)

            mQueryToLoad = mQueryToLoad!!.replace("{~table~}", tableName)

            mQueryToLoad = mQueryToLoad!!.replace("{~columns~}", columnsCsv)

            // find cursor position
            val cursorPosition = mQueryToLoad!!.lastIndexOf("{~cursor~}")
            mQueryToLoad = mQueryToLoad!!.replace("{~cursor~}", "")

            EzLogger.i("Loading: " + mQueryToLoad!!)

            txtQuery.setText(mQueryToLoad)

            // set cursor position
            if (cursorPosition < 0) {
                txtQuery.setSelection(txtQuery.text?.length ?: 0)
            } else {
                txtQuery.setSelection(cursorPosition)
            }

            mQueryToLoad = null
        }
    }

    public override fun onPause() {
        mUserPreferences.save(BooleanOption(PREF_IS_QUICK_KEYS_HIDDEN, mIsQuickKeysHidden))
        super.onPause()
    }

    /**
     * Remove all text from query text field
     */
    fun clearQuery() {
        // clear all text from text field
        txtQuery.text?.clear()
    }

    fun executeQuery() {

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(txtQuery.applicationWindowToken, 0)

        val queryText = txtQuery.text.toString()

        mHistoryQueryRepository.saveQueryHistory(this, connectionInfo, queryText)

        mHistoryQueryRepository.purgeQueryHistory(this, connectionInfo, 50)

        val intent = ResultsActivity.newIntent(this, connectionInfo.id, queryText, currentDatabase)
        startActivity(intent)
    }

    fun showQuickKeys() {
        EzLogger.d("Has Inline QuickKeys: " + mHasInlineQuickKeys)
        EzLogger.d("Use QK Hidden: " + mIsQuickKeysHidden)

        mQuickKeysList!!.visibility = View.VISIBLE
        if (!mHasInlineQuickKeys) {
            mDialogQuickKeys!!.show()
            return
        }

        mIsQuickKeysHidden = false
    }

    fun hideQuickKeys() {
        EzLogger.d("Has Inline QuickKeys: " + mHasInlineQuickKeys)
        EzLogger.d("Use QK Hidden: " + mIsQuickKeysHidden)

        mQuickKeysList!!.visibility = View.GONE
        if (!mHasInlineQuickKeys) {
            mDialogQuickKeys!!.hide()
        }

        mIsQuickKeysHidden = true
    }

    fun showSaveQueryDialog() {
        val saveQueryDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_query, null)

        val txtQueryName = saveQueryDialogView.findViewById(R.id.txtQueryName) as EditText
        val txtQueryText = saveQueryDialogView.findViewById(R.id.txtQueryText) as EditText

        txtQueryText.setText(txtQuery.text.toString())

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setView(saveQueryDialogView)
        alertBuilder.setNegativeButton("Cancel", null)
        alertBuilder.setPositiveButton("Save", null)

        val saveQueryDialog = alertBuilder.create()
        saveQueryDialog.setOnShowListener { dialog ->
            val button = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (txtQueryName.length() > 0) {
                    val savedQuery = SavedQuery(0,
                        connectionInfo.id,
                        txtQueryName.text.toString(),
                        txtQueryText.text.toString())

                    if (mSavedQueryRepository.saveQuery(savedQuery)) {
                        Toast.makeText(this@QueryActivity, "Query saved!",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@QueryActivity,
                            "Failed to save query",
                            Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@QueryActivity,
                        "Please enter a name for the query.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        saveQueryDialog.show()
    }

    override fun onDestroy() {

        /***
         * Dismiss all dialogs
         */
        databaseDialog.dismiss()
        tableDialog.dismiss()

        super.onDestroy()
    }

    fun loadCurrentDatabaseTableUi() {
        if (currentDatabase == null) {
            // lets update the UI
            lblCurrentDatabase.setText(R.string.query_none)
            lblCurrentTable.setText(R.string.query_none)

            // nothing else to do without a database selected
            return
        }

        lblCurrentDatabase.text = currentDatabase

        if (currentTable == null) {
            // update ui display of current table
            lblCurrentTable.setText(R.string.query_none)
        } else {
            lblCurrentTable.text = currentTable
        }
    }

    fun loadQuickKeys() {
        // clear sections
        quickKeysAdapter.clearSection(QuickKeysAdapter.SECTION_DATABASES)
        quickKeysAdapter.clearSection(QuickKeysAdapter.SECTION_TABLES)
        quickKeysAdapter.clearSection(QuickKeysAdapter.SECTION_COLUMNS)

        /***
         * Populate databases
         */
        val databases = serverGraph.keys
        for (d in databases) {
            quickKeysAdapter.addChild(d.name, QuickKeysAdapter.SECTION_DATABASES)
        }

        serverGraph.entries.firstOrNull { it ->
            it.key.name == currentDatabase
        }?.value?.forEach { it ->
            quickKeysAdapter.addChild(it.name, QuickKeysAdapter.SECTION_TABLES)
        }

        if (currentTable == null) {
            return
        }

        for (c in currentColumns) {
            quickKeysAdapter.addChild(c.name, QuickKeysAdapter.SECTION_COLUMNS)
        }

        quickKeysAdapter.notifyDataSetChanged()
    }

    fun loadDatabaseTableSelectionDialogs() {
        // clear dialogs
        databaseAdapter.clear()
        databaseAdapter.notifyDataSetChanged()
        tableAdapter.clear()
        tableAdapter.notifyDataSetChanged()

        /***
         * Populate databases
         */
        val databases = serverGraph.keys
        for (d in databases) {
            databaseAdapter.add(d.name)
            databaseAdapter.notifyDataSetChanged()
        }

        /***
         * Populate tables - In order to do that we need to have a selected
         * database
         */
        if (currentDatabase == null) {
            // nothing else to do without a database selected
            return
        }

        serverGraph.entries.firstOrNull { it ->
            it.key.name == currentDatabase
        }?.value?.forEach { it ->
            tableAdapter.add(Table(it.name, if (it.type == VIEW) Table.TYPE_VIEW else Table.TYPE_TABLE))
        }
    }

    private inner class OnDatabaseSelected : AdapterView.OnItemClickListener {

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            // hide dialog
            databaseDialog.hide()

            val databaseName = (view as TextView).text.toString()
            lblCurrentDatabase.text = databaseName

            currentDatabase = databaseName
            currentTable = null
            currentColumns.clear()

            loadCurrentDatabaseTableUi()
            loadDatabaseTableSelectionDialogs()
            loadQuickKeys()
        }
    }

    private inner class OnTableSelected : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            // hide dialog
            tableDialog.hide()

            val table = tableAdapter.getItem(position) as Table
            currentTable = table.name

            lblCurrentTable.text = table.name

            retrieveColumns()
        }
    }

    private fun retrieveColumns() {
        EzLogger.d("[retrieveColumns]")
        if (currentDatabase == null) {
            EzLogger.w("Database not yet selected!")
            onColumnsRetrieved()
        } else if (currentTable == null) {
            EzLogger.w("Table not yet selected!")
            onColumnsRetrieved()
        } else {
            val databaseName = DriverAgent.Database(currentDatabase!!)
            val tableName = DriverAgent.Table(currentTable!!)

            mConnectionAgent.connect(connectionInfo).flatMap { connection -> driverAgent.columns(connection, databaseName, tableName) }.collect({ arrayListOf<DriverAgent.Column>() }) { columns, column -> columns.add(column) }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(object : Subscriber<List<DriverAgent.Column>>() {
                override fun onCompleted() {
                    EzLogger.v("[onCompleted]")
                }

                override fun onError(e: Throwable) {
                    EzLogger.e("[onError] e=" + e.message, e)
                    Toast.makeText(this@QueryActivity, "Error: " + e.message, Toast.LENGTH_LONG).show()
                }

                override fun onNext(columns: List<DriverAgent.Column>) {
                    EzLogger.v("[onNext] columns.size=" + columns.size)
                    currentColumns.clear()
                    currentColumns.addAll(columns)

                    onColumnsRetrieved()
                }
            })
        }
    }

    private fun onColumnsRetrieved() {
        EzLogger.d("[onColumnsRetrieved]")
        loadQuickKeys()
        loadQueryIfAny()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_SAVED_QUERY -> {
                    mQueryToLoad = data?.getStringExtra(SavedQueriesActivity.RESULT_QUERY)
                    Toast.makeText(this, "Loading query...", Toast.LENGTH_SHORT).show()
                }
                REQUEST_HISTORY_QUERY -> {
                    val historyQuery = data?.getStringExtra(QueryHistoryActivity.RESULT_QUERY)
                    txtQuery.setText(historyQuery)
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    companion object {

        val PREF_IS_QUICK_KEYS_HIDDEN = "IsQuickKeysHidden"

        private val REQUEST_SAVED_QUERY = 9283
        private val REQUEST_HISTORY_QUERY = 9330

        private val EXTRA_CONNECTION_INFO_ID = "CONNECTION_INFO"

        private val SAVED_DATABASE = "SAVED_DATABASE"
        private val SAVED_TABLE = "SAVED_TABLE"

        fun newIntent(context: Context, connectionInfoId: Long): Intent {
            val intent = Intent(context, QueryActivity::class.java)
            intent.putExtra(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
            return intent
        }
    }
}
