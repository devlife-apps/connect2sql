package me.jromero.connect2sql.sql.driver.helper

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.driver.agent.DriverAgent

/**

 */
class PostgresDriverHelper : DriverHelper {
    override val driverClass: String
        get() = "org.postgresql.Driver"

    override fun getConnectionString(connectionInfo: ConnectionInfo): String {
        var connectionPath = "jdbc:postgresql://" + connectionInfo.host
        connectionPath += ":" + connectionInfo.port

        if (connectionInfo.database != null) {
            connectionPath += "/" + connectionInfo.database
        }

        val options = connectionInfo.options

        if (options.get(ConnectionInfo.OPTION_USE_SSL)?.toBoolean() ?: false) {
            connectionPath += if (connectionPath.contains("?")) "&" else "?"
            connectionPath += "ssl=true"

            if (options.get(ConnectionInfo.OPTION_TRUST_CERT)?.toBoolean() ?: false) {
                connectionPath += "&sslfactory=org.postgresql.ssl.NonValidatingFactory"
            }
        }

        return connectionPath
    }

    override val databasesQuery: String
        get() = "SELECT datname FROM pg_database;"

    override val databaseNameIndex: Int
        get() = 1

    override fun getTablesQuery(database: String): String {
        return "SELECT table_name,table_type " +
                "FROM information_schema.tables " +
                "WHERE table_schema != 'pg_catalog'" +
                "AND table_schema != 'information_schema'"
    }

    override val tableNameIndex: Int
        get() = 1

    override val tableTypeIndex: Int
        get() = 2

    override fun getColumnsQuery(table: String): String {
        return "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = " + safeValue(table) + ";"
    }

    override val columnNameIndex: Int
        get() = 1

    override val columnTypeIndex: Int
        get() = 2

    override fun getUseDatabaseSql(databaseName: String): String? {
        return null
    }

    override fun safeObject(`object`: String): String {
        return "\"" + `object` + "\""
    }

    override fun safeValue(value: String): String {
        return "'$value'"
    }

    override fun safeObject(table: DriverAgent.Table): String {
        return "\"" + table.name + "\""
    }

    override fun safeObject(database: DriverAgent.Database): String {
        return "\"" + database.name + "\""
    }

    override fun safeObject(column: DriverAgent.Column): String {
        return "\"" + column.name + "\""
    }
}
