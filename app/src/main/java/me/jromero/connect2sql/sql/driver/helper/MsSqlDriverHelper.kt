package me.jromero.connect2sql.sql.driver.helper

/**
 */
class MsSqlDriverHelper : JtdsDriverHelper() {
    override val serverType: String = "sqlserver"

    override val databasesQuery: String = "EXEC sp_databases;"

    override val databaseNameIndex: Int
        get() = 1

    override fun getTablesQuery(database: String): String {
        return "EXEC sp_tables @table_qualifier = " + safeObject(database)
    }

    override val tableNameIndex: Int
        get() = 3

    override val tableTypeIndex: Int
        get() = 4

    override fun getColumnsQuery(table: String): String {
        return "EXEC sp_columns @table_name = " + safeObject(table)
    }

    override val columnNameIndex: Int
        get() = 4

    override val columnTypeIndex: Int
        get() = 6

}
