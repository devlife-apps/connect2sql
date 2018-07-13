package me.jromero.connect2sql.sql.driver.helper


import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.driver.agent.DriverAgent

/**

 */
interface DriverHelper {

    /**
     * The driver class to load

     * @return
     */
    val driverClass: String

    /**
     * Construct a connection string given the ConnectionInfo
     * @param connectionInfo
     * *
     * @return
     */
    fun getConnectionString(connectionInfo: ConnectionInfo): String

    /**
     * Get a list of all databases available

     * @return
     */
    val databasesQuery: String

    /**
     * Get index of column for the database name
     * @return
     */
    val databaseNameIndex: Int

    /**
     * Get a list of tables in the current database connected

     * @return tables in current database
     */
    fun getTablesQuery(database: String): String

    /**
     * Get the index of where the table name is located in the results from
     * getTablesQuery();

     * @return table column index
     */
    val tableNameIndex: Int

    /**
     * Get the index of where the table type is located in the results from
     * getTablesQuery();

     * @return table column index
     */
    val tableTypeIndex: Int

    /**
     * Get a list of columns for the given table in the current connection

     * @return columns
     */
    fun getColumnsQuery(table: String): String

    /**
     * Get the index of where the column name is located in the results from
     * getColumnsQuery();

     * @return column index
     */
    val columnNameIndex: Int

    /**
     * Get the index of where the column type is located in the results from
     * getColumnsQuery();

     * @return column index
     */
    val columnTypeIndex: Int

    /**
     * Returns SQL to execute to change or select database

     * @return
     */
    fun getUseDatabaseSql(databaseName: String): String?

    /**
     * Manipulate string to make it safe to insert into a query string

     * @param object
     * *
     * @return
     */
    @Deprecated("use @{link DriverHelper#safeObject} or @{link DriverHelper#safeValue}")
    fun safeObject(`object`: String): String

    /**

     * @param value
     * *
     * @return
     */
    fun safeValue(value: String): String

    fun safeObject(table: DriverAgent.Table): String

    fun safeObject(database: DriverAgent.Database): String

    fun safeObject(column: DriverAgent.Column): String
}
