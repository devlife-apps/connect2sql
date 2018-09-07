package app.devlife.connect2sql.sql.driver.agent

import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.driver.helper.DriverHelper
import rx.Observable
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types

class DefaultDriverAgent(private val driverHelper: DriverHelper) : DriverAgent {

    override fun databases(connection: Connection): Observable<DriverAgent.Database> {
        return Observable.create { subscriber ->
            try {
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(driverHelper.databasesQuery)
                while (resultSet.next()) {
                    val name = resultSet.getString(driverHelper.databaseNameIndex)
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

    override fun tables(connection: Connection,
                        databaseName: DriverAgent.Database): Observable<DriverAgent.Table> {
        EzLogger.v("[tables] databaseName=$databaseName")
        return Observable.create { subscriber ->
            try {
                useDatabase(connection, databaseName)

                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(driverHelper.getTablesQuery(databaseName))
                while (resultSet.next()) {
                    val tableName = resultSet.getString(driverHelper.tableNameIndex)
                    val tableType = resultSet.getString(driverHelper.tableTypeIndex)?.let {
                        when (it) {
                            "SYSTEM VIEW",
                            "VIEW" ->
                                DriverAgent.TableType.VIEW
                            "BASE TABLE",
                            "TABLE" ->
                                DriverAgent.TableType.TABLE
                            else -> {
                                EzLogger.w("Skipping table type: $it")
                                null
                            }
                        }
                    }

                    if (tableType != null) {
                        subscriber.onNext(DriverAgent.Table(tableName, tableType))
                    }
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

    override fun columns(connection: Connection,
                         databaseName: DriverAgent.Database,
                         tableName: DriverAgent.Table): Observable<DriverAgent.Column> {
        EzLogger.v("[columns] databaseName=$databaseName, tableName=$tableName")
        return Observable.create { subscriber ->
            try {
                useDatabase(connection, databaseName)

                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(driverHelper.getColumnsQuery(tableName))

                while (resultSet.next()) {
                    val columnName = resultSet.getString(driverHelper.columnNameIndex)
                    subscriber.onNext(DriverAgent.Column(columnName))
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

    override fun execute(connection: Connection,
                         databaseName: DriverAgent.Database?,
                         sql: String): Observable<Statement> {
        return Observable.create { subscriber ->
            try {
                if (databaseName != null) {
                    useDatabase(connection, databaseName)
                }

                val statement = connection.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY)

                statement.execute(sql)
                subscriber.onNext(statement)
                subscriber.onCompleted()
            } catch (e: SQLException) {
                subscriber.onError(e)
            }
        }
    }

    override fun extract(resultSet: ResultSet,
                         startIndex: Int,
                         displayLimit: Int): Observable<DriverAgent.DisplayResults> {
        return Observable.create { subscriber ->
            try {
                val metaData = resultSet.metaData
                val columnCount = metaData.columnCount

                resultSet.last()
                val totalRows = resultSet.row


                val columnNameAndTypes = (0 until columnCount).map { i ->
                    Pair(
                        metaData.getColumnLabel(i + 1),
                        metaData.getColumnType(i + 1)
                    )
                }

                val startRow = startIndex + 1
                val lastDisplayedRow = when {
                    startRow + (displayLimit - 1) < totalRows -> startRow + (displayLimit - 1)
                    else -> totalRows
                }

                val data = (startRow..lastDisplayedRow).map { row ->
                    resultSet.absolute(row)

                    (0 until columnCount).map { columnIndex ->
                        when (columnNameAndTypes[columnIndex].second) {
                            Types.BLOB, Types.LONGVARBINARY -> "[blob]"
                            else -> resultSet.getString(columnIndex + 1) ?: "[null]"
                        }
                    }
                }

                subscriber.onNext(DriverAgent.DisplayResults(
                    columnNames = columnNameAndTypes.map { (name, _) -> name },
                    data = data,
                    totalCount = totalRows
                ))
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
    private fun useDatabase(connection: Connection, databaseName: DriverAgent.Database) {
        val statement = connection.createStatement()
        val useDatabaseSql = driverHelper.createUseDatabaseSql(databaseName)
        if (useDatabaseSql != null) {
            if (statement.execute(useDatabaseSql)) {
                statement.close()
            }
        }
    }
}
