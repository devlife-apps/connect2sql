package me.jromero.connect2sql.sql.driver.agent

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

import me.jromero.connect2sql.log.EzLogger
import me.jromero.connect2sql.sql.driver.helper.DriverHelper
import rx.Observable
import java.sql.ResultSet

/**

 */
class DefaultDriverAgent(private val mDriverHelper: DriverHelper) : DriverAgent {

    override fun databases(connection: Connection): Observable<DriverAgent.Database> {
        return Observable.create { subscriber ->
            try {
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(mDriverHelper.databasesQuery)
                while (resultSet.next()) {
                    val name = resultSet.getString(mDriverHelper.databaseNameIndex)
                    subscriber.onNext(DriverAgent.Database(name))
                }
                resultSet.close()
                statement.close()

                subscriber.onCompleted()
            } catch (e: SQLException) {
                EzLogger.e(e.message, e)
                subscriber.onError(e)
            }
        }
    }

    override fun tables(connection: Connection, databaseName: String): Observable<DriverAgent.Table> {
        EzLogger.v("[tables] databaseName=" + databaseName)
        return Observable.create { subscriber ->
            try {
                useDatabase(connection, databaseName)

                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(mDriverHelper.getTablesQuery(databaseName))
                while (resultSet.next()) {
                    val tableName = resultSet.getString(mDriverHelper.tableNameIndex)
                    val tableType = resultSet.getString(mDriverHelper.tableTypeIndex)
                    subscriber.onNext(DriverAgent.Table(tableName, DriverAgent.TableType(tableType)))
                }
                resultSet.close()
                statement.close()

                subscriber.onCompleted()
            } catch (e: SQLException) {
                EzLogger.e(e.message, e)
                subscriber.onError(e)
            }
        }
    }

    override fun columns(connection: Connection, databaseName: String, tableName: String): Observable<DriverAgent.Column> {
        EzLogger.v("[columns] databaseName=$databaseName, tableName=$tableName")
        return Observable.create { subscriber ->
            try {
                useDatabase(connection, databaseName)

                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(mDriverHelper.getColumnsQuery(tableName))

                while (resultSet.next()) {
                    val columnName = resultSet.getString(mDriverHelper.columnNameIndex)
                    val columnType = resultSet.getString(mDriverHelper.columnTypeIndex)

                    subscriber.onNext(DriverAgent.Column(columnName, DriverAgent.ColumnType(columnType)))
                }
                resultSet.close()
                statement.close()

                subscriber.onCompleted()
            } catch (e: SQLException) {
                EzLogger.e(e.message, e)
                subscriber.onError(e)
            }
        }
    }

    override fun execute(connection: Connection, databaseName: String?, sql: String): Observable<Statement> {
        return Observable.create { subscriber ->
            try {

                if (databaseName != null) {
                    useDatabase(connection, databaseName)
                }

                val statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                statement.execute(sql)
                subscriber.onNext(statement)
                subscriber.onCompleted()
            } catch (e: SQLException) {
                subscriber.onError(e)
            }
        }
    }

    override fun close(statement: Statement): Observable<Void> {
        return Observable.create { subscriber ->
            try {
                statement.close()
                subscriber.onNext(null)
                subscriber.onCompleted()
            } catch (e: SQLException) {
                subscriber.onError(e)
            }
        }
    }

    /**
     * Executes statement to use another databases
     *
     * @param connection
     * @param databaseName
     */
    private fun useDatabase(connection: Connection, databaseName: String) {
        val statement = connection.createStatement()
        val useDatabaseSql = mDriverHelper.getUseDatabaseSql(databaseName)
        if (useDatabaseSql != null) {
            if (statement.execute(useDatabaseSql)) {
                statement.close()
            }
        }
    }
}
