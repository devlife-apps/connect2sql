package app.devlife.connect2sql.db.model;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

public interface SqlModel<T> {

    Class<T> getModelClass();

    String getTableName();

    String getCreateSql();

    T hydrateObject(Cursor cursor) throws IllegalAccessException, InstantiationException;

    ContentValues toContentValues(T object);
}
