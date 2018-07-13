package me.jromero.connect2sql.db

import android.content.Context

import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

import java.util.ArrayList

import me.jromero.connect2sql.data.LockManager
import me.jromero.connect2sql.db.model.connection.ConnectionInfoSqlModel
import me.jromero.connect2sql.db.model.query.BuiltInQuery
import me.jromero.connect2sql.db.model.query.BuiltInQuery.BuiltInQuerySqlModel
import me.jromero.connect2sql.db.model.query.HistoryQuery.HistoryQuerySqlModel
import me.jromero.connect2sql.db.model.query.SavedQuery.SavedQuerySqlModel
import me.jromero.connect2sql.log.EzLogger
import me.jromero.connect2sql.sql.DriverType

class AppDatabaseHelperV3
/**
 * Constructor Takes and keeps a reference of the passed context in order to
 * access to the application assets and resources.

 * @param context
 * *
 * @param builtInQuerySqlModel
 * *
 * @param connectionInfoSqlModel
 * *
 * @param historyQuerySqlModel
 * *
 * @param savedQuerySqlModel
 */
(context: Context,
 private val lockManager: LockManager,
 private val builtInQuerySqlModel: BuiltInQuerySqlModel,
 private val connectionInfoSqlModel: ConnectionInfoSqlModel,
 private val historyQuerySqlModel: HistoryQuerySqlModel,
 savedQuerySqlModel: SavedQuerySqlModel) : SQLiteOpenHelper(context, AppDatabaseHelperV3.DB_NAME, null, AppDatabaseHelperV3.DB_VERSION) {

    private val models = arrayOf(
            builtInQuerySqlModel,
            connectionInfoSqlModel,
            historyQuerySqlModel,
            savedQuerySqlModel)

    val writableDatabase: SQLiteDatabase
        @Synchronized @Throws(PassphraseNotEnteredException::class)
        get() {
            val passphrase = lockManager.passphrase ?: throw PassphraseNotEnteredException()

            return super.getWritableDatabase(passphrase)
        }

    val readableDatabase: SQLiteDatabase
        @Synchronized @Throws(PassphraseNotEnteredException::class)
        get() {
            val passphrase = lockManager.passphrase ?: throw PassphraseNotEnteredException()

            return super.getReadableDatabase(passphrase)
        }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        if (!db!!.isReadOnly) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        EzLogger.d("SQLite DB does not exist, creating it...")

        for (model in models) {
            db.execSQL(model.createSql)
        }

        updateBuiltInQueries(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    private fun updateBuiltInQueries(db: SQLiteDatabase) {

        /**
         * Truncate table
         */
        db.delete(builtInQuerySqlModel.tableName, null, null)

        /**
         * Add built in queries
         */
        val builtInQueries = ArrayList<BuiltInQuery>()
        builtInQueries.add(BuiltInQuery(0, "Count table rows",
                "SELECT COUNT(*) FROM {~table~};", DriverType.MYSQL))
        builtInQueries.add(BuiltInQuery(0, "Select all columns except",
                "SELECT {~columns~}{~cursor~} FROM {~table~};", DriverType.MYSQL))
        builtInQueries.add(BuiltInQuery(0, "Insert a record",
                "INSERT INTO {~table~} ({~columns~}) VALUES ({~cursor~});",
                DriverType.MYSQL))

        for (builtinQuery in builtInQueries) {
            db.insert(builtInQuerySqlModel.tableName, null, builtInQuerySqlModel.toContentValues(builtinQuery))
        }
    }

    class PassphraseNotEnteredException : Exception()

    companion object {

        private val DB_NAME = "connect2sql.v3.db"

        /**
         * Initial encrypted database
         */
        val DB_VERSION_1 = 1

        private val DB_VERSION = DB_VERSION_1
    }
}
