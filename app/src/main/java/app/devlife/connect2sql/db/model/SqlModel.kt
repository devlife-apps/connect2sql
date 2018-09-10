package app.devlife.connect2sql.db.model

import android.content.ContentValues
import android.database.Cursor

import net.sqlcipher.database.SQLiteDatabase

interface SqlModel<T> {

    val modelClass: Class<T>

    val tableName: String

    val createSql: String

    fun upgradeSql(oldVersion: Int, newVersion: Int): List<String>

    @Throws(IllegalAccessException::class, InstantiationException::class)
    fun hydrateObject(cursor: Cursor): T

    fun toContentValues(`object`: T): ContentValues
}
