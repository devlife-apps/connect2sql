package me.jromero.connect2sql.db.model.query;

import me.jromero.connect2sql.sql.DriverType;
import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

public class BuiltInQuery extends BaseNamedQuery {

    private DriverType mDriver;

    public BuiltInQuery() {
        // Used for table creation
    }

    public BuiltInQuery(int id, String name, String query, DriverType driver) {
        setId(id);
        setName(name);
        setQuery(query);
        setDriver(driver);
    }

    public DriverType getDriver() {
        return mDriver;
    }

    public void setDriver(DriverType driver) {
        mDriver = driver;
    }

    public static class Column extends BaseNamedQuery.Column {
        public static final String DRIVER = "driver";
    }

    public static class BuiltInQuerySqlModel extends BaseNamedQuerySqlModel<BuiltInQuery> {

        public static String TABLE_NAME = "queries_builtin";

        @Override
        public Class<BuiltInQuery> getModelClass() {
            return BuiltInQuery.class;
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public String getCreateSql() {
            return "CREATE TABLE "
                    + "IF NOT EXISTS '" + TABLE_NAME + "' ("
                    + "'" + Column.ID + "' integer NOT NULL,"
                    + "'" + Column.DRIVER + "' text NOT NULL,"
                    + "'" + Column.NAME + "' text NOT NULL,"
                    + "'" + Column.QUERY + "' text NOT NULL,"
                    + "PRIMARY KEY('" + Column.ID + "'))";
        }

        @Override
        public BuiltInQuery hydrateObject(Cursor cursor) throws IllegalAccessException, InstantiationException {
            final BuiltInQuery builtInQuery = super.hydrateObject(cursor);
            builtInQuery.setDriver(DriverType.valueOf(cursor.getString(cursor.getColumnIndex(Column.DRIVER))));
            return builtInQuery;
        }

        @Override
        public ContentValues toContentValues(BuiltInQuery object) {
            ContentValues cv = super.toContentValues(object);
            cv.put(Column.DRIVER, object.getDriver().name());
            return cv;
        }
    }
}
