package app.devlife.connect2sql.sql.driver.helper

import app.devlife.connect2sql.sql.driver.agent.DriverAgent

/**

 */
class SybaseDriverHelper : JtdsDriverHelper() {
    override val serverType: String = "sybase"

    override val databasesQuery: String = "SELECT name FROM master..sysdatabases ORDER BY name ASC"

    override val databaseNameIndex: Int = 1

    override fun getTablesQuery(database: DriverAgent.Database): String {
        return "SELECT * FROM sysobjects WHERE type = 'U' ORDER BY name ASC"
    }

    override val tableNameIndex: Int = 1

    override val tableTypeIndex: Int = 7

    override fun getColumnsQuery(table: DriverAgent.Table): String {
        return """
                SELECT sc.name, st.name AS usertypename, sc.*
                FROM syscolumns sc
                INNER JOIN sysobjects so ON sc.id = so.id
                INNER JOIN systypes st ON sc.usertype = st.usertype
                WHERE so.name = ${safeObject(table)}
                """.trimIndent().replace("\n", " ")
    }

    override val columnNameIndex: Int = 1
}
