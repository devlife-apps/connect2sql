package app.devlife.connect2sql.sql.driver.helper

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.driver.agent.DriverAgent

/**

 */
class PostgresDriverHelper : DriverHelper {
    override val driverClass: String
        get() = "org.postgresql.Driver"

    override fun createConnectionString(connectionInfo: ConnectionInfo): String {
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

    override fun getTablesQuery(database: DriverAgent.Database): String {
        return "SELECT table_name,table_type " +
                "FROM information_schema.tables " +
                "WHERE table_schema != 'pg_catalog'" +
                "AND table_schema != 'information_schema'"
    }

    override val tableNameIndex: Int
        get() = 1

    override val tableTypeIndex: Int
        get() = 2

    override fun getColumnsQuery(table: DriverAgent.Table): String {
        return "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = " + safeObject(table) + ";"
    }

    override val columnNameIndex: Int
        get() = 1

    override fun createUseDatabaseSql(database: DriverAgent.Database): String? {
        return null
    }

    override fun safeValue(value: String): String {
        return "'$value'"
    }

    override fun safeObject(systemObject: DriverAgent.SystemObject): String {
        return "\"${systemObject.name}\""
    }
}
