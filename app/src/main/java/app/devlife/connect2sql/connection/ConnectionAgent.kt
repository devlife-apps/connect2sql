package app.devlife.connect2sql.connection

import app.devlife.connect2sql.db.model.connection.Address
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.connection.SshTunnelConfig
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
import javax.inject.Inject

class ConnectionAgent(private val sshTunnelAgent: SshTunnelAgent) {

    private val activeConnections = ConcurrentHashMap<ConnectionInfo, Connection>()

    fun connect(connectionInfo: ConnectionInfo): Observable<Connection> {
        return if (connectionInfo.sshConfig != null) {
            val serviceAddress = Address(connectionInfo.host, connectionInfo.port)
            val sshTunnelConfig = SshTunnelConfig(connectionInfo.sshConfig, serviceAddress)
            sshTunnelAgent.startSshTunnel(sshTunnelConfig).flatMap { (_, tunnelAddress) ->
                doConnect(connectionInfo, tunnelAddress)
            }
        } else {
            doConnect(connectionInfo, null)
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

    private fun doConnect(connectionInfo: ConnectionInfo,
                          tunnelAddress: Address?): Observable<Connection> {
        return Observable.create { subscriber ->
            try {
                if (!activeConnections.containsKey(connectionInfo) || activeConnections[connectionInfo]?.isClosed == true) {
                    val driverHelper = DriverHelperFactory.create(connectionInfo.driverType)
                        ?: throw SQLException("Driver not found for ${connectionInfo.driverType}")

                    try {
                        // import database driver
                        EzLogger.d("Importing database driver: ${driverHelper.driverClass}")
                        Class.forName(driverHelper.driverClass)

                        // determine the final connection info (if tunnel is configured)
                        val finalConnectionInfo = when {
                            tunnelAddress != null -> {
                                connectionInfo.copy(
                                    host = tunnelAddress.host,
                                    port = tunnelAddress.port
                                )
                            }
                            else -> connectionInfo
                        }

                        // check raw connection since DriverManager timeout appears to not be working
                        try {
                            val s = Socket()
                            val address = InetSocketAddress(
                                finalConnectionInfo.host.split("\\\\".toRegex())[0],
                                finalConnectionInfo.port)

                            s.connect(address, 10000)
                            if (s.isConnected) {
                                s.close()
                            }
                        } catch (e: IllegalArgumentException) {
                            throw SQLException(e.message, e)
                        } catch (e: IOException) {
                            throw SQLException(e.message, e)
                        }

                        // connect
                        val connectionPath = driverHelper.createConnectionString(finalConnectionInfo)
                        EzLogger.d("Connecting to: $connectionPath")
                        DriverManager.setLoginTimeout(30)
                        val connection = DriverManager.getConnection(
                            connectionPath,
                            finalConnectionInfo.username,
                            finalConnectionInfo.password)

                        activeConnections[connectionInfo] = connection
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
}
