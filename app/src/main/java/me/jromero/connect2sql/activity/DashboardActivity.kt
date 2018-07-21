package me.jromero.connect2sql.activity

import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.view.ActionMode
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import kotlinx.android.synthetic.main.activity_connections.*
import me.jromero.connect2sql.ApplicationUtils
import me.jromero.connect2sql.connection.ConnectionAgent
import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.db.model.connection.ConnectionInfoSqlModel
import me.jromero.connect2sql.db.repo.ConnectionInfoRepository
import com.gitlab.connect2sql.R
import me.jromero.connect2sql.loader.ConnectionInfoCursorLoader
import me.jromero.connect2sql.sql.DriverType
import me.jromero.connect2sql.ui.connection.ConnectionInfoDriverChooserActivity
import me.jromero.connect2sql.ui.connection.ConnectionInfoEditorActivity
import me.jromero.connect2sql.ui.connection.ConnectionInfoEditorRequest
import me.jromero.connect2sql.ui.query.QueryActivity
import me.jromero.connect2sql.ui.widget.BlockItem
import me.jromero.connect2sql.ui.widget.Toast
import me.jromero.connect2sql.ui.widget.dialog.ProgressDialog
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.sql.Connection
import java.util.*
import javax.inject.Inject

class DashboardActivity : BaseActivity() {

    private var actionMode: ActionMode? = null

    @Inject
    lateinit var connectionAgent: ConnectionAgent
    @Inject
    lateinit var connectionInfoRepository: ConnectionInfoRepository
    @Inject
    lateinit var connectionInfoSqlModel: ConnectionInfoSqlModel
    @Inject
    lateinit var mAnswers: Answers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connections)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        fab.setOnClickListener {
            startActivity(ConnectionInfoDriverChooserActivity.newIntent(this));
        }
    }

    override fun onResume() {
        super.onResume()

        supportLoaderManager.restartLoader(LOADER_CONNECTIONS, Bundle(), mConnectionsLoaderCallbacks)
    }


    private fun connect(connectionInfo: ConnectionInfo) {
        mAnswers.logCustom(CustomEvent("connect").putCustomAttribute("DriverType", connectionInfo.driverType.toString()))

        // define a progress dialog to display
        val progressDialog = ProgressDialog(
            this@DashboardActivity,
            "Connecting",
            "Please wait while we connect to the server...")
        progressDialog.setCancelable(true)

        val subscription = connectionAgent
            .connect(connectionInfo)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Connection>() {
                override fun onCompleted() {}

                override fun onError(e: Throwable?) {
                    progressDialog.dismiss()

                    val builder = AlertDialog.Builder(this@DashboardActivity)
                    builder.setTitle("Error")
                    builder.setMessage("Application Error: ${e?.message}")
                    builder.setNeutralButton("OK", null)
                    builder.create().show()
                }

                override fun onNext(t: Connection?) {
                    progressDialog.dismiss()

                    startActivity(QueryActivity.newIntent(this@DashboardActivity, connectionInfo.id))
                }
            })

        progressDialog.setOnCancelListener { subscription.unsubscribe() }
        progressDialog.show();
    }


    private fun populateDashboard(connections: List<ConnectionInfo>) {
        connections_dashboard.removeAllViews()

        for (connectionInfo in connections) {
            val connectionItem = BlockItem(this@DashboardActivity)
            connectionItem.title = connectionInfo.name

            // set image to display
            if (connectionInfo.driverType == DriverType.MYSQL) {
                connectionItem.imageResource = R.drawable.db_mysql
            } else if (connectionInfo.driverType == DriverType.MSSQL) {
                connectionItem.imageResource = R.drawable.db_mssql
            } else if (connectionInfo.driverType == DriverType.POSTGRES) {
                connectionItem.imageResource = R.drawable.db_postgre
            } else if (connectionInfo.driverType == DriverType.SYBASE) {
                connectionItem.imageResource = R.drawable.db_sybase
            }

            // set subtitle
            connectionItem.subtitle = connectionInfo.host

            // save connectionInfo to item
            connectionItem.tag = connectionInfo

            connectionItem.setOnLongClickListener(mOnConnectionLongClickListener)
            connectionItem.setOnClickListener(mOnConnectionClickListener)

            connections_dashboard.addView(connectionItem)
        }
    }

    fun activatedBlockItems(): ArrayList<BlockItem> {
        val activatedItems = ArrayList<BlockItem>()
        val totalChildren = connections_dashboard.childCount

        for (i in 0..totalChildren - 1) {
            val item = connections_dashboard.getChildAt(i) as BlockItem
            if (item.isActivated) {
                activatedItems.add(item)
            }
        }

        return activatedItems
    }

    private fun deactivateBlockItems() {
        val activatedItems = activatedBlockItems()
        for (item in activatedItems) {
            item.isActivated = false
        }
    }

    private val mOnConnectionLongClickListener = View.OnLongClickListener { v ->
        Log.d(TAG, "Item long click!")

        if (actionMode != null) {
            return@OnLongClickListener false
        }

        val item = v as BlockItem
        item.isActivated = true

        // Start the CAB using the ActionMode.Callback defined above
        actionMode = startSupportActionMode(mActionModeCallback)
        actionMode!!.setTitle("${activatedBlockItems().size} selected")

        true
    }

    private val mOnConnectionClickListener = View.OnClickListener { v ->
        Log.d(TAG, "Item clicked!")

        val item = v as BlockItem
        if (actionMode != null) {
            /***
             * Single click selection when in action mode
             */
            /***
             * Single click selection when in action mode
             */

            if (item.isActivated) {
                item.isActivated = false
            } else {
                item.isActivated = true
            }

            if (activatedBlockItems().size < 1) {
                actionMode!!.finish()
            } else {
                actionMode!!.setTitle("${activatedBlockItems().size} selected")
            }
        } else {
            try {
                /***
                 * Single click when not in selection mode
                 */
                /***
                 * Single click when not in selection mode
                 */


                // get connection info from item
                val connectionInfo = (item.tag as ConnectionInfo)

                if (TextUtils.isEmpty(connectionInfo.password)) {
                    val promptDialogView = LayoutInflater.from(this@DashboardActivity).inflate(R.layout.dialog_prompt, null)

                    (promptDialogView.findViewById(R.id.textView1) as TextView).visibility = View.GONE

                    val passwordText = promptDialogView.findViewById(R.id.editView1) as EditText

                    passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                    val alertBuilder = AlertDialog.Builder(this@DashboardActivity)
                    alertBuilder.setTitle("Password?")
                    alertBuilder.setView(promptDialogView)
                    alertBuilder.setPositiveButton("OK"
                    ) { dialog, which ->
                        // execute connecting to connection
                        connect(connectionInfo.copy(password = passwordText.text.toString()))
                    }
                    alertBuilder.create().show()
                } else {
                    connect(connectionInfo)
                }
            } catch (e: CloneNotSupportedException) {
                val builder = AlertDialog.Builder(this@DashboardActivity)
                builder.setTitle("Error")
                builder.setMessage("Application Error: " + e.message)
                builder.setNeutralButton("OK", null)
                builder.create().show()
            }

        }
    }

    private val mConnectionsLoaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return ConnectionInfoCursorLoader(this@DashboardActivity)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {

            val connections = ArrayList<ConnectionInfo>()

            while (cursor != null && cursor.moveToNext()) {
                val connectionInfo = connectionInfoSqlModel.hydrateObject(cursor)
                connections.add(connectionInfo)
            }

            populateDashboard(connections)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            connections_dashboard.removeAllViews()
        }
    }


    private val mActionModeCallback = object : ActionMode.Callback {

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // TODO Auto-generated method stub
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            deactivateBlockItems()
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.connections_selected, menu)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

            val activatedBlockItems = activatedBlockItems()

            when (item.itemId) {
                R.id.configure -> {
                    if (activatedBlockItems.size > 1) {
                        Toast.makeText(this@DashboardActivity,
                            "Please select only ONE connection to configure.",
                            Toast.LENGTH_LONG).show()
                        return false
                    }
                    for (connectionItem in activatedBlockItems) {
                        val connectionInfo = connectionItem.tag as ConnectionInfo
                        val request = ConnectionInfoEditorRequest(connectionInfo.id.toLong())
                        val newIntent = ConnectionInfoEditorActivity.newIntent(this@DashboardActivity, request)
                        startActivity(newIntent)
                        // there should only be one but just in case...
                        break
                    }
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.duplicate -> {
                    for (connectionItem in activatedBlockItems) {
                        try {
                            val connectionInfo = connectionItem.tag as ConnectionInfo
                            connectionInfoRepository.save(connectionInfo.copy(id = -1, name = "Copy: " + connectionInfo.name))
                        } catch (e: CloneNotSupportedException) {
                            Toast.makeText(this@DashboardActivity, "Failed to create copy", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }

                    }
                    mode.finish()

                    return true
                }
                R.id.delete -> {
                    for (connectionItem in activatedBlockItems) {
                        val connectionInfo = connectionItem.tag as ConnectionInfo
                        connectionInfoRepository.delete(connectionInfo.id.toLong())
                        connections_dashboard.removeView(connectionItem)
                    }
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                else -> return false
            }
        }
    }

    companion object {
        private val TAG = DashboardActivity::class.java.simpleName
        private val LOADER_CONNECTIONS = 1398039107
    }
}