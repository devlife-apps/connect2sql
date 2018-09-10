package app.devlife.connect2sql.connection

import app.devlife.connect2sql.db.model.connection.Address
import app.devlife.connect2sql.db.model.connection.BasicAuth
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.connection.PrivateKey
import app.devlife.connect2sql.db.model.connection.SshTunnelConfig
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.driver.helper.DriverHelperFactory
import com.jcraft.jsch.JSch
import rx.Observable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

class ConnectionAgent {

    private val activeConnections = ConcurrentHashMap<ConnectionInfo, Connection>()

    fun connect(connectionInfo: ConnectionInfo): Observable<Connection> {
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
                            connectionInfo.sshConfig != null -> {
                                val tunnelAddress = startSshTunnel(SshTunnelConfig(
                                    connectionInfo.sshConfig,
                                    Address(connectionInfo.host, connectionInfo.port)
                                ))

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

    private fun startSshTunnel(sshTunnelConfig: SshTunnelConfig): Address {
        val jSch = JSch()
        val sshConfig = sshTunnelConfig.proxy
        val serviceAddress = sshTunnelConfig.serviceAddress

        val session = jSch.getSession(
            sshConfig.authentication.username,
            sshConfig.address.host,
            sshConfig.address.port)

        // FIXME: Ask user to accept host
        session.setConfig(Properties().apply {
            this["StrictHostKeyChecking"] = "no"
        })

        when (sshConfig.authentication) {
            is PrivateKey ->
                jSch.addIdentity(
                    "${sshConfig.authentication.username}@${sshConfig.address.host}:${sshConfig.address.port}",
                    sshConfig.authentication.privateKeyContents.toByteArray(),
                    null,
                    null
                )
            is BasicAuth ->
                session.setPassword(sshConfig.authentication.password)
        }

        session.connect()
        val boundPort = session.setPortForwardingL(
            LOCALHOST,
            unusedPort,
            serviceAddress.host,
            serviceAddress.port)

        return Address(LOCALHOST, boundPort)
    }

    private val unusedPort: Int
        get() = ServerSocket(0).let {
            val port = it.localPort
            it.close()
            port
        }

    companion object {
        private const val LOCALHOST = "127.0.0.1"
    }
}
