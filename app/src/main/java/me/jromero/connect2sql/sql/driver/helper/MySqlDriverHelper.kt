package me.jromero.connect2sql.sql.driver.helper

import android.os.Build

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.driver.agent.DriverAgent

/**

 */
class MySqlDriverHelper : DriverHelper {

    override val driverClass: String
        get() {
            if (canSupportMariaDb()) {
                return MARIA_DRIVER_PATH
            } else {
                return MYSQL_DRIVER_PATH
            }
        }

    private fun canSupportMariaDb(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    override fun getColumnsQuery(table: String): String {
        return "DESCRIBE " + safeObject(table) + ";"
    }

    override fun getConnectionString(connectionInfo: ConnectionInfo): String {

        val driver = if (canSupportMariaDb()) "mariadb" else "mysql"

        var connectionPath = "jdbc:" + driver + "://" + connectionInfo.host
        connectionPath += ":" + connectionInfo.port + "/"

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

    override fun getTablesQuery(database: String): String {
        return "SHOW FULL TABLES FROM " + safeObject(database) + ";"
    }

    override val tableNameIndex: Int
        get() = 1

    override val tableTypeIndex: Int
        get() = 2

    override val columnNameIndex: Int
        get() = 1

    override val columnTypeIndex: Int
        get() = 2

    override fun getUseDatabaseSql(databaseName: String): String? {
        return "USE " + safeObject(databaseName) + ";"
    }


    override fun safeObject(`object`: String): String {
        return "`$`object``"
    }

    override fun safeValue(value: String): String {
        return "'$value'"
    }

    override fun safeObject(table: DriverAgent.Table): String {
        return safeObject(table.name)
    }

    override fun safeObject(database: DriverAgent.Database): String {
        return safeObject(database.name)
    }

    override fun safeObject(column: DriverAgent.Column): String {
        return safeObject(column.name)
    }

    companion object {

        private val MARIA_DRIVER_PATH = "org.mariadb.jdbc.Driver"
        private val MYSQL_DRIVER_PATH = "com.mysql.jdbc.Driver"
    }
}
