package me.jromero.connect2sql.test.db

import kdbc.execute
import kdbc.list
import kdbc.query
import me.jromero.connect2sql.TestServer
import org.junit.Before
import java.sql.Connection
import java.sql.DriverManager

/**
 *
 */
abstract class MySqlTestSpec {

    abstract val testServer: TestServer

    fun db(testServer: TestServer): Connection {
        Class.forName("org.mariadb.jdbc.Driver")
        return DriverManager.getConnection("jdbc:mariadb://${testServer.host}:${testServer.port}",
                testServer.username, testServer.password)
    }

    @Before
    fun setup() {
        with(db(testServer)) {
            val databases = query("SHOW DATABASES") list { getString(1) }
            databases.filterNot({ it.toLowerCase().endsWith("schema")}).forEach { database ->
                println("Dropping DB: $database")
                execute("DROP DATABASE `${database}`")
            }

            close()
        }
    }

}