package app.devlife.connect2sql.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import app.devlife.connect2sql.connection.ConnectionAgent
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.driver.agent.DefaultDriverAgent
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.sql.driver.helper.DriverHelperFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class ConnectionViewModel @Inject constructor(private val connectionAgent: ConnectionAgent) :
    ViewModel() {

    private lateinit var connectionInfo: ConnectionInfo

    val databases: MutableLiveData<List<DriverAgent.Database>?> = MutableLiveData()
    val tables: MutableLiveData<List<DriverAgent.Table>?> = MutableLiveData()
    val columns: MutableLiveData<List<DriverAgent.Column>?> = MutableLiveData()
    val selectedDatabase = MutableLiveData<DriverAgent.Database?>()
    val selectedTable = MutableLiveData<DriverAgent.Table>()

    fun init(connectionInfo: ConnectionInfo) {
        this.connectionInfo = connectionInfo

        val driverAgent = DefaultDriverAgent(DriverHelperFactory.create(connectionInfo.driverType))
        connectionAgent.connect(connectionInfo)
            .flatMap { driverAgent.databases(it).toList() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { databases ->
                this.databases.value = databases

                connectionInfo.database?.also { connectionDatabase ->
                    databases
                        .firstOrNull { it.name.equals(connectionDatabase, ignoreCase = true) }
                        ?.also { setSelectedDatabase(it) }
                }
            }
    }

    fun setSelectedDatabase(database: DriverAgent.Database?) {
        if (selectedDatabase.value != database) {
            selectedDatabase.value = database
            selectedTable.value = null

            if (database != null) {
                val driverAgent = DefaultDriverAgent(DriverHelperFactory.create(connectionInfo.driverType))
                connectionAgent.connect(connectionInfo)
                    .flatMap { driverAgent.tables(it, database).toList() }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { tables ->
                        this.tables.value = tables
                    }
            } else {
                tables.value = null
            }
        }
    }

    fun setSelectedTable(table: DriverAgent.Table?) {
        selectedTable.value = table
    }

    override fun onCleared() {
        databases.value = null
        tables.value = null
        selectedDatabase.value = null
        selectedTable.value = null
    }
}