package me.jromero.connect2sql.db.model.connection

import android.content.ContentValues
import android.database.Cursor

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

import me.jromero.connect2sql.db.model.SqlModel
import me.jromero.connect2sql.log.EzLogger
import me.jromero.connect2sql.sql.DriverType

/**

 */
class ConnectionInfoSqlModel : SqlModel<ConnectionInfo> {


    object Column {
        const val ID = "id"
        const val NAME = "name"
        const val DRIVER = "driver"
        const val HOST = "host"
        const val PORT = "port"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val DATABASE = "database"
        const val OPTIONS = "options"
    }

    override fun getModelClass(): Class<ConnectionInfo> {
        return ConnectionInfo::class.java
    }

    override fun getTableName(): String {
        return TABLE_NAME
    }

    override fun getCreateSql(): String {
        return "CREATE TABLE " +
                "IF NOT EXISTS '" + TABLE_NAME + "' (" +
                "'" + Column.ID + "' integer NOT NULL," +
                "'" + Column.NAME + "' text NOT NULL," +
                "'" + Column.DRIVER + "' text NOT NULL," +
                "'" + Column.HOST + "' text NOT NULL," +
                "'" + Column.PORT + "' integer NOT NULL," +
                "'" + Column.USERNAME + "' text NOT NULL," +
                "'" + Column.PASSWORD + "' text NOT NULL," +
                "'" + Column.DATABASE + "' text," +
                "'" + Column.OPTIONS + "' text NOT NULL DEFAULT '{}'," +
                "PRIMARY KEY('" + Column.ID + "'))"
    }

    override fun hydrateObject(cursor: Cursor): ConnectionInfo {

        val options = HashMap<String, String>()
        val json = cursor.getString(cursor.getColumnIndex(Column.OPTIONS))
        try {
            val jsonOptions = JSONObject(json)
            val keys = jsonOptions.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                options.put(key, jsonOptions.getString(key))
            }
        } catch (e: JSONException) {
            EzLogger.e(e.message)
        }

        return ConnectionInfo(
                cursor.getLong(cursor.getColumnIndex(Column.ID)),
                cursor.getString(cursor.getColumnIndex(Column.NAME)),
                DriverType.valueOf(cursor.getString(cursor.getColumnIndex(Column.DRIVER))),
                cursor.getString(cursor.getColumnIndex(Column.HOST)),
                cursor.getInt(cursor.getColumnIndex(Column.PORT)),
                cursor.getString(cursor.getColumnIndex(Column.USERNAME)),
                cursor.getString(cursor.getColumnIndex(Column.PASSWORD)),
                cursor.getString(cursor.getColumnIndex(Column.DATABASE)),
                options)
    }

    override fun toContentValues(`object`: ConnectionInfo): ContentValues {
        val cv = ContentValues()
        if (`object`.id > 0) {
            cv.put(Column.ID, `object`.id)
        }
        cv.put(Column.NAME, `object`.name)
        cv.put(Column.DRIVER, `object`.driverType.name)
        cv.put(Column.HOST, `object`.host)
        cv.put(Column.PORT, `object`.port)
        cv.put(Column.USERNAME, `object`.username)
        cv.put(Column.PASSWORD, `object`.password)
        cv.put(Column.DATABASE, `object`.database)


        val jsonOptions = JSONObject()
        for (entry in `object`.options.entries) {
            try {
                jsonOptions.put(entry.key, entry.value)
            } catch (e: JSONException) {
                EzLogger.e(e.message)
            }

        }

        cv.put(Column.OPTIONS, jsonOptions.toString())
        return cv
    }

    companion object {
        const val TABLE_NAME = "connections"
    }
}
