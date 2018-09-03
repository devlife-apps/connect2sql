package app.devlife.connect2sql.connection

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.driver.helper.DriverHelperFactory
import rx.Observable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap

/**

 */
class ConnectionAgent {

    private val activeConnections = ConcurrentHashMap<ConnectionInfo, Connection>()

    fun connect(connectionInfo: ConnectionInfo): Observable<Connection> {
        return Observable.create { subscriber ->
            try {
                if (!activeConnections.containsKey(connectionInfo) || activeConnections[connectionInfo]?.isClosed() == true) {
                    try {
                        val s = Socket()
                        val address = InetSocketAddress(
                            connectionInfo.host.split("\\\\".toRegex())[0],
                            connectionInfo.port)

                        s.connect(address, 10000)
                        if (s.isConnected) {
                            s.close()
                        }
                    } catch (e: IllegalArgumentException) {
                        throw SQLException(e.message, e)
                    } catch (e: IOException) {
                        throw SQLException(e.message, e)
                    }

                    val driverHelper = DriverHelperFactory.create(connectionInfo.driverType)
                        ?: throw SQLException("Driver not found for ${connectionInfo.driverType}")

                    try {
                        // import database driver
                        EzLogger.d("Importing database driver: ${driverHelper.driverClass}")
                        Class.forName(driverHelper.driverClass)

                        // build our connection path
                        val connectionPath = driverHelper.createConnectionString(connectionInfo)

                        // connect
                        EzLogger.d("Connecting to: $connectionPath")
                        DriverManager.setLoginTimeout(30)
                        val connection = DriverManager.getConnection(
                            connectionPath,
                            connectionInfo.username,
                            connectionInfo.password)

                        activeConnections.put(connectionInfo, connection)
                    } catch (e: ClassNotFoundException) {
                        throw SQLException("Class not found: " + e.message, e)
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                        throw SQLException(e.message, e)
                    }
                }

                subscriber.onNext(activeConnections[connectionInfo])
                subscriber.onCompleted()
            } catch (e: SQLException) {
                subscriber.onError(e)
            }
        }
    }

    fun disconnect(connection: Connection): Observable<Unit> {
        return Observable.create { subscriber ->
            try {
                if (!connection.isClosed) {
                    connection.close()
                }
                subscriber.onNext(Unit)
                subscriber.onCompleted()
            } catch (e: SQLException) {
                subscriber.onError(e)
            }
        }
    }
}
