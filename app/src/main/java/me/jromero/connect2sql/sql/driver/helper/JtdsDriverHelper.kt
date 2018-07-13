package me.jromero.connect2sql.sql.driver.helper

import android.text.TextUtils

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.driver.agent.DriverAgent

/**

 */
abstract class JtdsDriverHelper : DriverHelper {

    override val driverClass: String
        get() = "net.sourceforge.jtds.jdbc.Driver"

    abstract val serverType: String


    override fun getConnectionString(connectionInfo: ConnectionInfo): String {

        var connectionPath = "jdbc:jtds:" + serverType + "://" + connectionInfo.host
        connectionPath += ":" + connectionInfo.port

        val database = connectionInfo.database
        if (!TextUtils.isEmpty(database)) {
            connectionPath += "/" + database
        }

        connectionPath += ";"

        val instance = connectionInfo.options.get(ConnectionInfo.OPTION_INSTANCE) ?: ""
        if (!TextUtils.isEmpty(instance)) {
            connectionPath += "instance=$instance;"
        }

        val domain = connectionInfo.options.get(ConnectionInfo.OPTION_DOMAIN) ?: ""
        if (!TextUtils.isEmpty(domain)) {
            connectionPath += "domain=$domain;"
        }

        return connectionPath
    }

    override fun getUseDatabaseSql(databaseName: String): String? {
        return "USE " + safeObject(databaseName)
    }

    override fun safeObject(`object`: String): String {
        return "[$`object`]"
    }

    override fun safeValue(value: String): String {
        return "'$value'"
    }

    override fun safeObject(column: DriverAgent.Column): String {
        return "[" + column.name + "]"
    }

    override fun safeObject(table: DriverAgent.Table): String {
        return "[" + table.name + "]"
    }

    override fun safeObject(database: DriverAgent.Database): String {
        return "[" + database.name + "]"
    }
}
