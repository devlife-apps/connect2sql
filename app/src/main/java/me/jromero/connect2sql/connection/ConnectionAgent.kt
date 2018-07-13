package me.jromero.connect2sql.connection

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.driver.DriverFactory
import rx.Observable
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.ConcurrentHashMap

/**

 */
class ConnectionAgent {

    private val mActiveConnections = ConcurrentHashMap<ConnectionInfo, Connection>()

    fun connect(connectionInfo: ConnectionInfo): Observable<Connection> {
        return Observable.create { subscriber ->
            try {
                val driver = DriverFactory.newDriverInstance(connectionInfo.driverType)

                if (!mActiveConnections.containsKey(connectionInfo) || mActiveConnections[connectionInfo]?.isClosed() == true) {
                    val connection = driver.connect(connectionInfo)
                    mActiveConnections.put(connectionInfo, connection)
                }

                subscriber.onNext(mActiveConnections[connectionInfo])
                subscriber.onCompleted()
            } catch (e: DriverFactory.DriverNotFoundException) {
                subscriber.onError(e)
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

    data class ConnectionContext(val connectionInfo: ConnectionInfo, val connection: Connection)
}
