package app.devlife.connect2sql.ui.query

import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
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
import app.devlife.connect2sql.sql.driver.agent.DefaultDriverAgent
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.sql.driver.helper.DriverHelper
import app.devlife.connect2sql.sql.driver.helper.DriverHelperFactory
import app.devlife.connect2sql.ui.browse.BrowseFragment
import app.devlife.connect2sql.ui.history.HistoryFragment
import app.devlife.connect2sql.ui.results.ResultsActivity
import app.devlife.connect2sql.ui.savedqueries.SavedFragment
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.viewmodel.ConnectionViewModel
import app.devlife.connect2sql.viewmodel.ViewModelFactory
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.activity_query.fab
import kotlinx.android.synthetic.main.activity_query.nav_bottom
import kotlinx.android.synthetic.main.activity_query.sheet
import kotlinx.android.synthetic.main.activity_query.txtQuery
import rx.Subscription
import javax.inject.Inject

class QueryActivity : BaseActivity() {

    private val connectionInfo: ConnectionInfo by lazy {
        val id = intent?.extras?.getLong(EXTRA_CONNECTION_INFO_ID).ensure { t -> t != null && t > 0 }!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    private val connectionViewModel: ConnectionViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ConnectionViewModel::class.java)
    }

    private val browseFragment: BrowseFragment?
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_BROWSE) as BrowseFragment?
    private val historyFragment: HistoryFragment?
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_HISTORY) as HistoryFragment?
    private val savedFragment: SavedFragment?
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_SAVED) as SavedFragment?
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(sheet)
    }

    private val currentColumns: MutableList<DriverAgent.Column> = arrayListOf()
    private val serverGraph: MutableMap<DriverAgent.Database, List<DriverAgent.Table>> = hashMapOf()

    private lateinit var driverHelper: DriverHelper
    private lateinit var driverAgent: DefaultDriverAgent
    private lateinit var quickKeysAdapter: QuickKeysAdapter
    private lateinit var tableAdapter: TableListAdapter
    private lateinit var databaseAdapter: ArrayAdapter<String>

    private var mQuickKeysList: ExpandableListView? = null
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
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        txtQuery.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }

        nav_bottom.inflateMenu(R.menu.query_bottom)
        nav_bottom.navigationIcon = null
        nav_bottom.setOnMenuItemClickListener { menuItem ->
            if (!menuItem.isChecked) {
                clearMenuSelection()
                menuItem.icon.setColorFilter(getColor(R.color.blueLight), PorterDuff.Mode.SRC_IN)

                return@setOnMenuItemClickListener when (menuItem.itemId) {
                    R.id.menu_browse -> {
                        showFragment(browseFragment
                            ?: BrowseFragment.newInstance(connectionInfo.id))
                        true
                    }
                    R.id.menu_history -> {
                        showFragment(historyFragment
                            ?: HistoryFragment.newInstance(connectionInfo.id))
                        true
                    }
                    R.id.menu_saved -> {
                        showFragment(savedFragment ?: SavedFragment.newInstance(connectionInfo.id))
                        true
                    }
                    else -> false
                }
            }

            false
        }

        driverHelper = DriverHelperFactory.create(connectionInfo.driverType)!!
        driverAgent = DefaultDriverAgent(driverHelper)

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            private var disableDragging = true

            override fun onStateChanged(v: View, state: Int) {
                when (state) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        disableDragging = true
                        fab.show()
                        clearMenuSelection()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        if (disableDragging) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        disableDragging = false
                        fab.hide()
                    }
                }
            }

            override fun onSlide(v: View, position: Float) {
                // ignored
            }
        })

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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)

        // this only lives in tablet sized
//        mQuickKeysList = findViewById(R.id.elvQuickKeys) as? ExpandableListView
        if (mQuickKeysList != null) {
            mHasInlineQuickKeys = true
        } else {
            // create a dialog version
            mDialogQuickKeys = AppCompatDialog(this)
            mDialogQuickKeys!!.setTitle("Quick Keys")
            mDialogQuickKeys!!.setOnShowListener { dialog ->
                val d = dialog as Dialog
                d.window?.setBackgroundDrawable(ColorDrawable(0))
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

        mQuickKeysList!!.setAdapter(quickKeysAdapter)

        /***
         * Quick keys
         */

        // quick keys
//        mQuickKeysList!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
//            val text = quickKeysAdapter.getChild(groupPosition, childPosition).toString()
//
//            when (groupPosition) {
//                QuickKeysAdapter.SECTION_DATABASES ->
//                    driverHelper.safeObject(DriverAgent.Database(text))
//                QuickKeysAdapter.SECTION_TABLES ->
//                    driverHelper.safeObject(DriverAgent.Table(text))
//                QuickKeysAdapter.SECTION_COLUMNS ->
//                    driverHelper.safeObject(DriverAgent.Column(text))
//                else -> text
//            }.also { txtQuery.append("$it ") }
//
//            true
//        }
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        when (fragment) {
            is HistoryFragment -> fragment.apply {
                onQueryClickListener = { query ->
                    loadQueryIfAny(query.query)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            is BrowseFragment -> fragment.apply {
                onTableSelectedListener = { _ ->
                    loadQueryIfAny("SELECT * FROM {~table~} LIMIT 100")
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        val tag = when (fragment) {
            is HistoryFragment -> FRAG_TAG_HISTORY
            is SavedFragment -> FRAG_TAG_SAVED
            is BrowseFragment -> FRAG_TAG_BROWSE
            else -> TODO()
        }

        when (fragment) {
            supportFragmentManager.findFragmentByTag(tag) ->
                supportFragmentManager.inTransaction {
                    hideAllFragmentsExcept { it == fragment }
                    show(fragment)
                }
            else ->
                supportFragmentManager.inTransaction {
                    hideAllFragmentsExcept { it == fragment }
                    add(R.id.fragment_content_sheet, fragment, tag)
                    show(fragment)
                }
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun clearMenuSelection() {
        (0 until nav_bottom.menu.size()).forEach {
            nav_bottom.menu.getItem(it).icon.clearColorFilter()
        }
    }

    private fun FragmentManager.inTransaction(t: FragmentTransaction.() -> Unit): Unit =
        beginTransaction().apply(t).commitNow()

    private fun FragmentTransaction.hideAllFragmentsExcept(predicate: (Fragment) -> Boolean) {
        listOfNotNull(browseFragment, historyFragment, savedFragment)
            .filter { !predicate.invoke(it) }
            .forEach {
                hide(it)
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
    }

    override fun onStop() {
        serverGraphSubscription?.unsubscribe()
        serverGraphSubscription = null
        super.onStop()
    }

    private fun loadQueryIfAny(query: String?) {
        if (!TextUtils.isEmpty(query)) {
            val databaseName = connectionViewModel.selectedDatabase.value
                ?.let { driverHelper.safeObject(it) }
                ?: ""

            val tableName = connectionViewModel.selectedTable.value
                ?.let { driverHelper.safeObject(it) }
                ?: ""

            var columnsCsv = ""
            if (!currentColumns.isEmpty()) {
                val columns = currentColumns.map { col -> col.name }
                columnsCsv = columns.joinToString()
            }

            // process replacements
            query!!.replace("{~database~}", databaseName)
                .replace("{~table~}", tableName)
                .replace("{~columns~}", columnsCsv)
                .apply {
                    val cursorPosition = lastIndexOf("{~cursor~}")
                    txtQuery.setText(replace("{~cursor~}", ""))
                    if (cursorPosition < 0) {
                        txtQuery.setSelection(txtQuery.text?.length ?: 0)
                    } else {
                        txtQuery.setSelection(cursorPosition)
                    }
                }
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

        val intent = ResultsActivity.newIntent(this,
            connectionInfo.id,
            queryText,
            connectionViewModel.selectedDatabase.value?.name)

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

    private fun showSaveQueryDialog() {
        val saveQueryDialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_save_query, null)

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

//    fun loadQuickKeys() {
//        // clear sections
//        quickKeysAdapter.clearSection(QuickKeysAdapter.SECTION_DATABASES)
//        quickKeysAdapter.clearSection(QuickKeysAdapter.SECTION_TABLES)
//        quickKeysAdapter.clearSection(QuickKeysAdapter.SECTION_COLUMNS)
//
//        /***
//         * Populate databases
//         */
//        val databases = serverGraph.keys
//        for (d in databases) {
//            quickKeysAdapter.addChild(d.name, QuickKeysAdapter.SECTION_DATABASES)
//        }
//
//        serverGraph.entries.firstOrNull { it ->
//            it.key.name == currentDatabase
//        }?.value?.forEach { it ->
//            quickKeysAdapter.addChild(it.name, QuickKeysAdapter.SECTION_TABLES)
//        }
//
//        if (currentTable == null) {
//            return
//        }
//
//        for (c in currentColumns) {
//            quickKeysAdapter.addChild(c.name, QuickKeysAdapter.SECTION_COLUMNS)
//        }
//
//        quickKeysAdapter.notifyDataSetChanged()
//    }


    companion object {

        val PREF_IS_QUICK_KEYS_HIDDEN = "IsQuickKeysHidden"

        private const val FRAG_TAG_BROWSE = "FRAG_TAG_BROWSE"
        private const val FRAG_TAG_HISTORY = "FRAG_TAG_HISTORY"
        private const val FRAG_TAG_SAVED = "FRAG_TAG_SAVED"

        private const val EXTRA_CONNECTION_INFO_ID = "CONNECTION_INFO"

        fun newIntent(context: Context, connectionInfoId: Long): Intent {
            val intent = Intent(context, QueryActivity::class.java)
            intent.putExtra(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
            return intent
        }
    }
}
