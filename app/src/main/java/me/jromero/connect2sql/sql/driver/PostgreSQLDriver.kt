package me.jromero.connect2sql.sql.driver

import java.sql.SQLException

import me.jromero.connect2sql.db.model.connection.ConnectionInfo

@Deprecated("")
class PostgreSQLDriver : BaseDriver("org.postgresql.Driver") {

    override fun getColumnsQuery(table: String): String {
        return "SELECT column_name,data_type FROM information_schema.columns WHERE table_name = '${table}';"
    }

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

    override fun getDatabasesQuery(): String {
        return "SELECT datname FROM pg_database;"
    }

    override fun getTablesQuery(database: String): String? {
        return null
    }

    override fun safeObject(`object`: String): String {
        return "\"" + `object` + "\""
    }

    @Throws(SQLException::class)
    override fun useDatabase(database: String) {
        // Impossible in Postgres
        return
    }

    override fun getTableNameIndex(): Int {
        return 0
    }

    override fun getTableTypeIndex(): Int {
        return 0
    }

    override fun getColumnNameIndex(): Int {
        return 1
    }

    override fun getColumnTypeIndex(): Int {
        return 2
    }
}