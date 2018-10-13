package app.devlife.connect2sql.ui.query

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.activity.BaseActivity
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.query.SavedQuery
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.db.repo.HistoryQueryRepository
import app.devlife.connect2sql.db.repo.SavedQueryRepository
import app.devlife.connect2sql.lang.ensure
import app.devlife.connect2sql.sql.driver.agent.DefaultDriverAgent
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.sql.driver.helper.DriverHelper
import app.devlife.connect2sql.sql.driver.helper.DriverHelperFactory
import app.devlife.connect2sql.ui.browse.BrowseFragment
import app.devlife.connect2sql.ui.history.HistoryFragment
import app.devlife.connect2sql.ui.quickkeys.QuickKeysAdapter
import app.devlife.connect2sql.ui.quickkeys.QuickKeysFragment
import app.devlife.connect2sql.ui.results.ResultsActivity
import app.devlife.connect2sql.ui.savedqueries.SavedQueryFragment
import app.devlife.connect2sql.ui.widget.SqlEditText
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.util.ext.equals
import app.devlife.connect2sql.viewmodel.ConnectionViewModel
import app.devlife.connect2sql.viewmodel.SavedQueriesViewModel
import app.devlife.connect2sql.viewmodel.ViewModelFactory
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.activity_query.fab
import kotlinx.android.synthetic.main.activity_query.nav_bottom
import kotlinx.android.synthetic.main.activity_query.query_label_breadcrumbs
import kotlinx.android.synthetic.main.activity_query.query_save_btn
import kotlinx.android.synthetic.main.activity_query.sheet
import kotlinx.android.synthetic.main.activity_query.txtQuery
import kotlinx.android.synthetic.main.dialog_save_query.view.txtQueryName
import kotlinx.android.synthetic.main.dialog_save_query.view.txtQueryText
import javax.inject.Inject

class QueryActivity : BaseActivity() {

    private val connectionInfo: ConnectionInfo by lazy {
        val id = intent?.extras?.getLong(EXTRA_CONNECTION_INFO_ID).ensure { t -> t != null && t > 0 }!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    private val connectionViewModel: ConnectionViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ConnectionViewModel::class.java)
    }

    private val savedQueriesViewModel: SavedQueriesViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SavedQueriesViewModel::class.java)
    }

    private val browseFragment: BrowseFragment
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_BROWSE) as BrowseFragment?
            ?: BrowseFragment.newInstance(connectionInfo.id)
    private val historyFragment: HistoryFragment
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_HISTORY) as HistoryFragment?
            ?: HistoryFragment.newInstance(connectionInfo.id)
    private val quickKeysFragment: QuickKeysFragment
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_QUICK_KEYS) as QuickKeysFragment?
            ?: QuickKeysFragment.newInstance(connectionInfo.id)
    private val savedQueryFragment: SavedQueryFragment
        get() = supportFragmentManager.findFragmentByTag(FRAG_TAG_SAVED) as SavedQueryFragment?
            ?: SavedQueryFragment.newInstance(connectionInfo.id)

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(sheet)
    }

    private val interpolator = AccelerateInterpolator(1.0f)

    private val currentColumns: MutableList<DriverAgent.Column> = arrayListOf()

    private lateinit var driverHelper: DriverHelper
    private lateinit var driverAgent: DefaultDriverAgent

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var mHistoryQueryRepository: HistoryQueryRepository
    @Inject
    lateinit var savedQueryRepository: SavedQueryRepository
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        connectionViewModel.init(connectionInfo)

        BreadcrumbsBinder(this, connectionViewModel, query_label_breadcrumbs)
            .onBreadcrumbClicked = { nav_bottom.performClickOn(R.id.menu_browse) }

        txtQuery.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }

        query_save_btn.setOnClickListener { showSaveQueryDialog() }

        nav_bottom.selectFunction = { menuItem ->
            menuItem.icon.setColorFilter(getColor(R.color.blueLight), PorterDuff.Mode.SRC_IN)
        }
        nav_bottom.deselectFunction = { it.icon.clearColorFilter() }
        nav_bottom.inflateMenu(R.menu.query_bottom)
        nav_bottom.setOnMenuItemClickListener { menuItem ->
            if (!menuItem.isChecked) {
                fab.hide()
                nav_bottom.background.alpha = 0
                nav_bottom.clearMenuSelection()

                return@setOnMenuItemClickListener when (menuItem.itemId) {
                    R.id.menu_browse -> {
                        showFragment(browseFragment)
                        true
                    }
                    R.id.menu_quick_keys -> {
                        showFragment(quickKeysFragment)
                        true
                    }
                    R.id.menu_history -> {
                        showFragment(historyFragment)
                        true
                    }
                    R.id.menu_saved -> {
                        showFragment(savedQueryFragment)
                        true
                    }
                    else -> false
                }
            }

            false
        }

        driverHelper = DriverHelperFactory.create(connectionInfo.driverType)
        driverAgent = DefaultDriverAgent(driverHelper)

        sheet.setBackgroundColor(resources.getColor(R.color.greyDarker, null))
        sheet.background.alpha = 0

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            private var disableDragging = true

            override fun onStateChanged(v: View, state: Int) {
                when (state) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        disableDragging = true


                        sheet.background.alpha = 0
                        nav_bottom.background.alpha = 255

                        fab.show()
                        nav_bottom.clearMenuSelection()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        if (disableDragging) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        disableDragging = false

                        sheet.background.alpha = MIN_BOTTOMSHEET_ALPHA.toInt()
                        nav_bottom.background.alpha = 0

                        fab.hide()
                    }
                }
            }

            override fun onSlide(v: View, position: Float) {
                interpolator.getInterpolation(1f - position)
                    .also { percentage ->
                        ((percentage * (MAX_BOTTOMSHEET_ALPHA - MIN_BOTTOMSHEET_ALPHA)) + MIN_BOTTOMSHEET_ALPHA)
                            .toInt()
                            .also { calculatedAlpha ->
                                sheet.background.alpha = calculatedAlpha
                                query_save_btn.alpha = calculatedAlpha / 255f
                            }
                    }

            }
        })

        fab.setOnClickListener { executeQuery() }


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        when (fragment) {
            is BrowseFragment -> fragment.apply {
                onTableSelectedListener = { _ ->
                    loadQueryIfAny("SELECT * FROM {~table~}\nLIMIT 100")
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            is HistoryFragment -> fragment.apply {
                onQueryClickListener = { query ->
                    loadQueryIfAny(query.query)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            is QuickKeysFragment -> fragment.apply {
                onClearClickListener = { this@QueryActivity.txtQuery.text?.clear() }
                onQuickKeyClickListener = { quickKey ->
                    val additionalText = when (quickKey) {
                        is QuickKeysAdapter.QuickKey.OfTypeSystemObject ->
                            driverHelper.safeObject(quickKey.obj)
                        is QuickKeysAdapter.QuickKey.OfTypeString ->
                            quickKey.obj
                    }

                    with(this@QueryActivity.txtQuery) {
                        text?.insert(selectionStart, "$additionalText ")
                    }
                }
            }
            is SavedQueryFragment -> {
                fragment.onQueryClickListener = { query ->
                    loadQueryIfAny(query.query)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        val tag = when (fragment) {
            is BrowseFragment -> FRAG_TAG_BROWSE
            is HistoryFragment -> FRAG_TAG_HISTORY
            is QuickKeysFragment -> FRAG_TAG_QUICK_KEYS
            is SavedQueryFragment -> FRAG_TAG_SAVED
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

    private fun FragmentManager.inTransaction(t: FragmentTransaction.() -> Unit): Unit =
        beginTransaction().apply(t).commitNow()

    private fun FragmentTransaction.hideAllFragmentsExcept(predicate: (Fragment) -> Boolean) {
        listOfNotNull(browseFragment, historyFragment, quickKeysFragment, savedQueryFragment)
            .filter { !predicate.invoke(it) }
            .forEach {
                hide(it)
            }
    }

    override fun onStart() {
        super.onStart()

        txtQuery.clearFocus()
        txtQuery.requestFocus()
        txtQuery.onTextChangedListener = SqlEditText.OnTextChangedListener { refreshSavedButton() }
    }

    private fun refreshSavedButton() {
        val alreadySaved = savedQueriesViewModel.getSavedQueries(connectionInfo.id).value
            ?.any { savedQuery ->
                savedQuery.query.equals(txtQuery.cleanText,
                    ignoreCase = true,
                    ignoreWhitespace = true)
            }
            ?: false

        if (alreadySaved) {
            query_save_btn.isSelected = true
            query_save_btn.isEnabled = false
        } else {
            query_save_btn.isSelected = false
            query_save_btn.isEnabled = true
        }
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

    private fun executeQuery() {
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

    override fun onBackPressed() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else -> super.onBackPressed()
        }
    }

    private fun showSaveQueryDialog() {
        with(layoutInflater.inflate(R.layout.dialog_save_query, null)) {
            txtQueryText.setText(txtQuery.text.toString())
            AlertDialog.Builder(this.context)
                .setView(this)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_save, null)
                .create()
                .also { dialog ->
                    dialog.setOnShowListener { _ ->
                        val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        button.setOnClickListener {
                            if (txtQueryName.length() > 0) {
                                if (savedQueryRepository.saveQuery(SavedQuery(0,
                                        connectionInfo.id,
                                        txtQueryName.text.toString(),
                                        txtQueryText.text.toString()))) {

                                    refreshSavedButton()

                                    Toast.makeText(this@QueryActivity,
                                        R.string.query_saved,
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@QueryActivity,
                                        R.string.query_failed_saving,
                                        Toast.LENGTH_SHORT).show()
                                }
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this@QueryActivity,
                                    R.string.query_missing_name,
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .show()
        }
    }

    companion object {
        private const val MIN_BOTTOMSHEET_ALPHA = 125f
        private const val MAX_BOTTOMSHEET_ALPHA = 255f

        private const val FRAG_TAG_BROWSE = "FRAG_TAG_BROWSE"
        private const val FRAG_TAG_HISTORY = "FRAG_TAG_HISTORY"
        private const val FRAG_TAG_QUICK_KEYS = "FRAG_TAG_QUICK_KEYS"
        private const val FRAG_TAG_SAVED = "FRAG_TAG_SAVED"

        private const val EXTRA_CONNECTION_INFO_ID = "CONNECTION_INFO"

        fun newIntent(context: Context, connectionInfoId: Long): Intent {
            val intent = Intent(context, QueryActivity::class.java)
            intent.putExtra(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
            return intent
        }
    }
}
