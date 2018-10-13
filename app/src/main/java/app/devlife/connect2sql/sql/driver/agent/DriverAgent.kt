package app.devlife.connect2sql.sql.driver.agent

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import rx.Observable
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

/**

 */
interface DriverAgent {

    fun databases(connection: Connection): Observable<Database>

    fun tables(connection: Connection, database: Database): Observable<Table>

    fun columns(connection: Connection,
                table: Table): Observable<Column>

    fun execute(connection: Connection, database: Database?, sql: String): Observable<Statement>

    fun extract(resultSet: ResultSet,
                startIndex: Int,
                displayLimit: Int): Observable<DisplayResults>

    fun close(statement: Statement): Observable<Void>

    /**
     * System Objects are items such as tables, schemas, databases, columns, etc.
     */

    interface SystemObject : Parcelable {
        val name: String
    }

    @Parcelize
    data class Database(override val name: String) : SystemObject

    @Parcelize
    data class Table(val database: Database,
                     override val name: String,
                     val type: TableType = TableType.TABLE) : SystemObject

    @Parcelize
    data class Column(val table: Table, override val name: String) : SystemObject

    enum class TableType {
        VIEW,
        TABLE
    }

    data class DisplayResults(val columnNames: List<String>,
                              val data: List<List<String>>,
                              val totalCount: Int)
}
