package app.devlife.connect2sql

import app.devlife.connect2sql.connection.ConnectionType

/**
 *
 */
enum class TestServer(
        val type: ConnectionType,
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val database: String? = null) {

    MSSQL_BASIC_USER(ConnectionType.MSSQL, "SOME_HOST", 1433, "sa", "SOME_PASSWORD"),
    MYSQL_BASIC_USER(ConnectionType.MYSQL, "SOME_HOST", 3306, "basic_user", "SOME_PASSWORD"),
    POSTGRES_ADMIN(ConnectionType.POSTGRES, "SOME_HOST", 5432, "postgres", "SOME_PASSWORD", "test")
}