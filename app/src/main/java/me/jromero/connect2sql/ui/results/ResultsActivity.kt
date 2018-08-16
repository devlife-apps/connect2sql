package me.jromero.connect2sql.ui.results

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import com.gitlab.connect2sql.R
import me.jromero.connect2sql.ApplicationUtils
import me.jromero.connect2sql.activity.BaseActivity
import me.jromero.connect2sql.connection.ConnectionAgent
import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.db.repo.ConnectionInfoRepository
import me.jromero.connect2sql.lang.ensure
import me.jromero.connect2sql.log.EzLogger
import me.jromero.connect2sql.sql.driver.agent.DefaultDriverAgent
import me.jromero.connect2sql.sql.driver.agent.DriverAgent
import me.jromero.connect2sql.sql.driver.helper.DriverHelper
import me.jromero.connect2sql.sql.driver.helper.DriverHelperFactory
import me.jromero.connect2sql.ui.widget.dialog.ProgressDialog
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import javax.inject.Inject

class ResultsActivity : BaseActivity() {

    private val connectionInfo: ConnectionInfo by lazy {
        val id = intent?.extras?.getLong(EXTRA_CONNECTION_INFO_ID).ensure({ t -> t != null && t > 0 })!!
        connectionInfoRepo.getConnectionInfo(id)
    }

    private val sqlString: String by lazy { intent?.extras?.getString(EXTRA_SQL_STRING)!! }
    private val databaseName: String? by lazy { intent?.extras?.getString(EXTRA_DATABASE) }

    private val resultsSets = SparseArray<ResultSet>()
    private val resultTableFragments = HashMap<ResultSet, ResultsTableFragment>()

    private lateinit var driverHelper: DriverHelper
    private lateinit var driverAgent: DriverAgent

    @Inject
    lateinit var connectionInfoRepo: ConnectionInfoRepository
    @Inject
    lateinit var connectionAgent: ConnectionAgent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_results)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        driverHelper = DriverHelperFactory.create(connectionInfo.driverType)!!
        driverAgent = DefaultDriverAgent(driverHelper)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.results, menu)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_refresh -> {
                removeAllResultTableFragments()
                clearResultSets()
                queryServer()
                return true
            }
            R.id.menu_font_increase -> {
                increaseFontSize(3)
                redrawTables()
                return true
            }
            R.id.menu_font_decrease -> {
                decreaseFontSize(3)
                redrawTables()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        queryServer()
    }


    private fun redrawTables() {
        for (entry in resultTableFragments.entries) {
            val tableFragment = entry.value
            tableFragment.redrawTable()
        }
    }

    private fun removeAllResultTableFragments() {
        if (resultTableFragments.size < 1) {
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
        for (entry in resultTableFragments.entries) {
            transaction.remove(entry.value)
        }
        transaction.commit()
    }

    private fun increaseFontSize(i: Int) {
        for (entry in resultTableFragments.entries) {
            val tableFragment = entry.value
            tableFragment.increaseFontSize(i)
        }
    }

    private fun decreaseFontSize(i: Int) {
        for (entry in resultTableFragments.entries) {
            val tableFragment = entry.value
            tableFragment.decreaseFontSize(i)
        }
    }

    protected fun queryServer() {
        // create progress dialog
        val progressDialog = ProgressDialog(this, "Running", "Querying server...")
        progressDialog.show()

        connectionAgent
            .connect(connectionInfo)
            .flatMap { connection ->
                driverAgent.execute(
                    connection,
                    databaseName?.let { DriverAgent.Database(it) },
                    sqlString)
            }
            .map { statement ->
                val resultSet = statement.resultSet
                if (resultSet != null) {
                    fun extractResults(statement: Statement, list: List<ResultSet>): List<ResultSet> {
                        if (statement.getMoreResults(Statement.KEEP_CURRENT_RESULT))
                            return extractResults(statement, list + statement.resultSet)
                        else
                            return list
                    }

                    ViewableResults(extractResults(statement, arrayListOf(resultSet)))
                } else {
                    UpdateResults(statement.updateCount)
                }
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Results>() {
                override fun onCompleted() {}

                override fun onError(e: Throwable) {
                    EzLogger.e(e.message, e)
                    progressDialog.dismiss()

                    val builder = AlertDialog.Builder(this@ResultsActivity)
                    builder.setTitle("Error")
                    builder.setMessage(e.message)
                    builder.setPositiveButton("OK") { dialog, which -> onBackPressed() }
                    builder.create().show()
                }

                override fun onNext(results: Results) {
                    progressDialog.dismiss()

                    when (results) {
                        is ViewableResults -> {
                            results.resultSets.forEachIndexed { i, resultSet -> resultsSets.append(i, resultSet) }
                            EzLogger.i("Total result sets: " + resultsSets)
                            displayResults()
                        }
                        is UpdateResults -> {
                            val builder = AlertDialog.Builder(this@ResultsActivity)
                            builder.setTitle("Success")
                            builder.setMessage("Records updated: " + results.updatedRecords)
                            builder.setPositiveButton("OK") { dialog, which ->
                                onBackPressed()
                            }
                            builder.create().show()
                        }
                    }
                }

            })
    }


    private fun populateTabs() {

        removeAllTabs()

        val totalResultSets = resultsSets.size()
        if (totalResultSets > 1) {
            supportActionBar?.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
            for (i in 0..totalResultSets - 1) {
                val tab = supportActionBar?.newTab()?.setText("Result " + i)?.setTag(resultsSets.get(i))?.setTabListener(tabListener)
                supportActionBar?.addTab(tab, i == 0)
            }
        }
    }

    private fun removeAllTabs() {
        supportActionBar?.removeAllTabs()
        supportActionBar?.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    }

    private fun displayResults() {
        populateTabs()
        showContents(resultsSets.get(0))
    }

    private fun showContents(rs: ResultSet) {
        var frag: ResultsTableFragment? = null
        if (resultTableFragments.containsKey(rs)) {
            frag = resultTableFragments[rs]
        } else {
            frag = ResultsTableFragment.newInstance(rs, 0)

            // store reference to table fragments
            resultTableFragments.put(rs, frag)
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.results_table_container, frag)
        transaction.commit()
    }

    /**
     * Clears our list of results sets and closes them
     */
    private fun clearResultSets() {
        for (i in 0..resultsSets.size() - 1) {
            Thread(ResultSetClosingRunnable(resultsSets.get(resultsSets.keyAt(i)))).start()
        }

        resultsSets.clear()
    }

    private val tabListener: android.support.v7.app.ActionBar.TabListener = object : android.support.v7.app.ActionBar.TabListener {
        override fun onTabReselected(tab: android.support.v7.app.ActionBar.Tab?, ft: android.support.v4.app.FragmentTransaction?) = Unit

        override fun onTabUnselected(tab: android.support.v7.app.ActionBar.Tab?, ft: android.support.v4.app.FragmentTransaction?) = Unit

        override fun onTabSelected(tab: android.support.v7.app.ActionBar.Tab?, ft: android.support.v4.app.FragmentTransaction?) = Unit
    }

    class ResultSetClosingRunnable(private val mResultSet: ResultSet) : Runnable {

        override fun run() {
            try {
                mResultSet.close()
            } catch (e: SQLException) {
                EzLogger.e(e.message, e)
            }

        }
    }


    companion object {

        private val EXTRA_CONNECTION_INFO_ID = "EXTRA_CONNECTION_INFO"
        private val EXTRA_DATABASE = "EXTRA_DATABASE"
        private val EXTRA_SQL_STRING = "EXTRA_SQL_STRING"

        fun newIntent(context: Context, connectionInfoId: Long, sql: String, databaseName: String?): Intent {
            val intent = Intent(context, ResultsActivity::class.java)

            intent.putExtra(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
            intent.putExtra(EXTRA_DATABASE, databaseName)
            intent.putExtra(EXTRA_SQL_STRING, sql)

            return intent
        }
    }
}

interface Results
data class UpdateResults(val updatedRecords: Int) : Results
data class ViewableResults(val resultSets: List<ResultSet>) : Results