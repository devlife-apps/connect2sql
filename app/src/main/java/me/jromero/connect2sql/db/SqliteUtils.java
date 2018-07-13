package me.jromero.connect2sql.db;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.text.TextUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by javier.romero on 4/29/14.
 */
public class SqliteUtils {

    public static void dropColumn(SQLiteDatabase db,
                                  String tableName,
                                  String createTableSql,
                                  String[] colsToRemove) throws java.sql.SQLException {

        List<String> updatedTableColumns = getTableColumns(db, tableName);

        // Remove the columns we don't want anymore from the table's list of columns
        updatedTableColumns.removeAll(Arrays.asList(colsToRemove));

        db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");

        // Creating the table on its new format (no redundant columns)
        db.execSQL(createTableSql);

        // Populating the table with the data
        String columnsSeparated = TextUtils.join(",", updatedTableColumns);
        db.execSQL("INSERT INTO " + tableName + "(" + columnsSeparated + ") SELECT "
                + columnsSeparated + " FROM " + tableName + "_old;");
        db.execSQL("DROP TABLE " + tableName + "_old;");
    }

    public static List<String> getTableColumns(SQLiteDatabase db, String tableName) throws SQLException{
        List<String> columns = new ArrayList<String>();
        String sql = "PRAGMA table_info(" + tableName + ");";

        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(cursor.getColumnIndex("name")));
        }

        cursor.close();

        return columns;
    }
}
