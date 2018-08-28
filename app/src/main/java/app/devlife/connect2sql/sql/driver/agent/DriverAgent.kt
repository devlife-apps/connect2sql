package app.devlife.connect2sql.sql.driver.agent

import rx.Observable
import java.sql.Connection
import java.sql.Statement

/**

 */
interface DriverAgent {

    fun databases(connection: Connection): Observable<Database>

    fun tables(connection: Connection, databaseName: Database): Observable<Table>

    fun columns(connection: Connection, databaseName: Database, tableName: Table): Observable<Column>

    fun execute(connection: Connection, databaseName: Database?, sql: String): Observable<Statement>

    fun close(statement: Statement): Observable<Void>

    /**
     * System Objects are items such as tables, schemas, databases, columns, etc.
     */
    interface SystemObject {
        val name: String
    }

    data class Database(override val name: String) : SystemObject
    data class Table(override val name: String, val type: TableType = TableType.TABLE) : SystemObject
    data class Column(override val name: String) : SystemObject

    enum class TableType {
        VIEW,
        TABLE
    }
}
