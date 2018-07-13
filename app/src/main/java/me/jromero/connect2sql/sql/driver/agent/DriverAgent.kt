package me.jromero.connect2sql.sql.driver.agent

import java.sql.Connection

import rx.Observable
import java.sql.Statement


/**

 */
interface DriverAgent {

    fun databases(connection: Connection): Observable<Database>

    fun tables(connection: Connection, databaseName: String): Observable<Table>

    fun columns(connection: Connection, databaseName: String, tableName: String): Observable<Column>

    fun execute(connection: Connection, databaseName: String?, sql: String): Observable<Statement>

    fun close(statement: Statement): Observable<Void>

    /**
     * System Objects are items such as tables, schemas, databases, columns, etc.
     */
    interface SystemObject
    data class Database(val name: String) : SystemObject
    data class Table(val name: String, val type: TableType) : SystemObject
    data class Column(val name: String, val type: ColumnType) : SystemObject

    data class TableType(val value: String)
    data class ColumnType(val value: String)
}
