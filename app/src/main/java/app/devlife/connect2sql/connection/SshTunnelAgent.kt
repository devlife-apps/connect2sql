package app.devlife.connect2sql.connection

import app.devlife.connect2sql.db.model.connection.Address
import app.devlife.connect2sql.db.model.connection.BasicAuth
import app.devlife.connect2sql.db.model.connection.PrivateKey
import app.devlife.connect2sql.db.model.connection.SshTunnelConfig
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import rx.Observable
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap

class SshTunnelAgent(private val jSch: JSch) {
    private val activeTunnels = ConcurrentHashMap<SshTunnelConfig, Pair<Session, Address>>()

    fun startSshTunnel(sshTunnelConfig: SshTunnelConfig): Observable<Pair<Session, Address>> {
        return Observable.create { subscriber ->
            try {
                if (activeTunnels[sshTunnelConfig] == null || activeTunnels[sshTunnelConfig]!!.first.isConnected) {
                    val sshConfig = sshTunnelConfig.proxy
                    val serviceAddress = sshTunnelConfig.serviceAddress

                    val session = jSch.getSession(
                        sshConfig.authentication.username,
                        sshConfig.address.host,
                        sshConfig.address.port)

                    when (sshConfig.authentication) {
                        is PrivateKey -> jSch.addIdentity(
                            "${sshConfig.authentication.username}@${sshConfig.address.host}:${sshConfig.address.port}",
                            sshConfig.authentication.privateKeyContents.toByteArray(),
                            null,
                            null
                        )
                        is BasicAuth -> session.setPassword(sshConfig.authentication.password)
                    }

                    try {
                        session.connect()
                    } catch (e: JSchException) {
                        throw when {
                            e.message?.startsWith("UnknownHostKey") == true -> {
                                UnknownHostException(
                                    session.hostKey)
                            }
                            else -> e
                        }
                    }

                    val boundPort = session.setPortForwardingL(
                        LOCALHOST,
                        unusedPort,
                        serviceAddress.host,
                        serviceAddress.port)

                    activeTunnels[sshTunnelConfig] = Pair(session, Address(LOCALHOST, boundPort))
                }

                subscriber.onNext(activeTunnels[sshTunnelConfig])
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    private val unusedPort: Int
        get() = ServerSocket(0).let {
            val port = it.localPort
            it.close()
            port
        }

    data class UnknownHostException(val hostKey: HostKey) :
        Exception("Unknown host ${hostKey.host}!")

    companion object {
        private const val LOCALHOST = "127.0.0.1"
    }
}