package app.devlife.connect2sql.db.model.connection

import android.content.ContentValues
import android.database.Cursor
import app.devlife.connect2sql.db.AppDatabaseHelperV3
import app.devlife.connect2sql.db.model.SqlModel
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.DriverType
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

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

        const val SSH_HOST = "ssh_host"
        const val SSH_PORT = "ssh_port"
        const val SSH_USERNAME = "ssh_username"
        const val SSH_PASSWORD = "ssh_password"
        const val SSH_PRIVATE_KEY = "ssh_private_key"
    }

    override val modelClass: Class<ConnectionInfo>
        get() = ConnectionInfo::class.java

    override val tableName: String
        get() = TABLE_NAME

    override val createSql: String
        get() =
            """
            CREATE TABLE IF NOT EXISTS '$TABLE_NAME' (
                '${Column.ID}' integer NOT NULL,
                '${Column.NAME}' text NOT NULL,
                '${Column.DRIVER}' text NOT NULL,
                '${Column.HOST}' text NOT NULL,
                '${Column.PORT}' integer NOT NULL,
                '${Column.USERNAME}' text NOT NULL,
                '${Column.PASSWORD}' text NOT NULL,
                '${Column.DATABASE}' text,
                '${Column.SSH_HOST}' text,
                '${Column.SSH_PORT}' integer,
                '${Column.SSH_USERNAME}' text,
                '${Column.SSH_PASSWORD}' text,
                '${Column.SSH_PRIVATE_KEY}' text,
                '${Column.OPTIONS}' text NOT NULL DEFAULT '{}',
                PRIMARY KEY('${Column.ID}')
            )
            """.trimIndent()

    override fun upgradeSql(oldVersion: Int, newVersion: Int): List<String> {
        return when (newVersion) {
            AppDatabaseHelperV3.DB_VERSION_2 -> listOf(
                "ALTER TABLE '$TABLE_NAME' ADD COLUMN '${Column.SSH_HOST}' text",
                "ALTER TABLE '$TABLE_NAME' ADD COLUMN '${Column.SSH_PORT}' integer",
                "ALTER TABLE '$TABLE_NAME' ADD COLUMN '${Column.SSH_USERNAME}' text",
                "ALTER TABLE '$TABLE_NAME' ADD COLUMN '${Column.SSH_PASSWORD}' text",
                "ALTER TABLE '$TABLE_NAME' ADD COLUMN '${Column.SSH_PRIVATE_KEY}' text"
            )
            else -> listOf()
        }
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

        val sshConfig: SshConfig? = cursor.getString(Column.SSH_HOST)?.let { host ->
            cursor.getInt(Column.SSH_PORT)?.let { port ->
                cursor.getString(Column.SSH_USERNAME)?.let { username ->
                    val password = cursor.getString(Column.SSH_PASSWORD)
                    val privateKeyContents = cursor.getString(Column.SSH_PRIVATE_KEY)

                    when {
                        privateKeyContents != null ->
                            SshConfig(Address(host, port), PrivateKey(username, password, privateKeyContents))
                        password != null ->
                            SshConfig(Address(host, port), BasicAuth(username, password))
                        else ->
                            SshConfig(Address(host, port), None(username))
                    }
                }
            }
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
            sshConfig,
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

        `object`.sshConfig?.apply {
            cv.put(Column.SSH_HOST, this.address.host)
            cv.put(Column.SSH_PORT, this.address.port)
            when (this.authentication) {
                is BasicAuth -> {
                    cv.put(Column.SSH_USERNAME, this.authentication.username)
                    cv.put(Column.SSH_PASSWORD, this.authentication.password)
                }
                is PrivateKey -> {
                    cv.put(Column.SSH_USERNAME, this.authentication.username)
                    cv.put(Column.SSH_PRIVATE_KEY, this.authentication.privateKeyContents)
                }
                is None -> {
                    cv.put(Column.SSH_USERNAME, this.authentication.username)
                }
            }
        }

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

    fun Cursor.getString(columnName: String): String? {
        val colIndex = this.getColumnIndex(columnName)

        return when {
            this.isNull(colIndex) -> null
            else -> this.getString(colIndex)
        }
    }

    fun Cursor.getInt(columnName: String): Int? {
        val colIndex = this.getColumnIndex(columnName)

        return when {
            this.isNull(colIndex) -> null
            else -> this.getInt(colIndex)
        }
    }
}
