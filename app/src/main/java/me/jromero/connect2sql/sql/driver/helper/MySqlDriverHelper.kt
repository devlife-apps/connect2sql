package me.jromero.connect2sql.sql.driver.helper

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.driver.agent.DriverAgent
import me.jromero.connect2sql.sql.driver.agent.DriverAgent.Table

/**

 */
class MySqlDriverHelper : DriverHelper {

    override val driverClass: String = MARIA_DRIVER_PATH

    override fun getColumnsQuery(table: Table): String {
        return "DESCRIBE " + safeObject(table) + ";"
    }

    override fun createConnectionString(connectionInfo: ConnectionInfo): String {

        var connectionPath = "jdbc:mariadb://${connectionInfo.host}:${connectionInfo.port}/"

        if (connectionInfo.database != null) {
            connectionPath += connectionInfo.database
        }

        connectionPath += "?zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&connectTimeout=30000&socketTimeout=30000&autoReconnect=true"

        return connectionPath
    }

    override val databasesQuery: String
        get() = "SHOW DATABASES;"

    override val databaseNameIndex: Int
        get() = 1

    override fun getTablesQuery(database: DriverAgent.Database): String {
        return "SHOW FULL TABLES FROM " + safeObject(database) + ";"
    }

    override val tableNameIndex: Int
        get() = 1

    override val tableTypeIndex: Int
        get() = 2

    override val columnNameIndex: Int
        get() = 1

    override fun createUseDatabaseSql(database: DriverAgent.Database): String? {
        return "USE " + safeObject(database) + ";"
    }


    override fun safeValue(value: String): String {
        return "'$value'"
    }

    override fun safeObject(systemObject: DriverAgent.SystemObject): String {
        return "`${systemObject.name}`"
    }

    companion object {
        private val MARIA_DRIVER_PATH = "org.mariadb.jdbc.Driver"
    }
}
