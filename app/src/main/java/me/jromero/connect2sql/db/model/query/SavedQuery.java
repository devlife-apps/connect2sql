package me.jromero.connect2sql.db.model.query;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

public class SavedQuery extends BaseNamedQuery {

    private long mConnectionId;

    public SavedQuery() {
    }

    public SavedQuery(int id, long connection_id, String name, String query) {
        setId(id);
        mConnectionId = connection_id;
        setName(name);
        setQuery(query);
    }

    public long getConnectionId() {
        return mConnectionId;
    }

    public void setConnectionId(int connectionId) {
        mConnectionId = connectionId;
    }

    public static class Column extends BaseNamedQuery.Column {
        public static final String CONNECTION_ID = "connection_id";
    }

    public static class SavedQuerySqlModel extends BaseNamedQuerySqlModel<SavedQuery> {

        public static String TABLE_NAME = "queries";

        @Override
        public Class<SavedQuery> getModelClass() {
            return SavedQuery.class;
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
                    + "'" + Column.CONNECTION_ID + "' integer NOT NULL,"
                    + "'" + Column.NAME + "' text NOT NULL,"
                    + "'" + Column.QUERY + "' text NOT NULL,"
                    + "PRIMARY KEY('" + Column.ID + "'))";
        }

        @Override
        public SavedQuery hydrateObject(Cursor cursor) throws IllegalAccessException, InstantiationException {
            final SavedQuery savedQuery = super.hydrateObject(cursor);
            savedQuery.setConnectionId(cursor.getInt(cursor.getColumnIndex(Column.CONNECTION_ID)));
            return savedQuery;
        }

        @Override
        public ContentValues toContentValues(SavedQuery object) {
            ContentValues cv = super.toContentValues(object);
            cv.put(Column.CONNECTION_ID, object.getConnectionId());
            return cv;
        }
    }
}
