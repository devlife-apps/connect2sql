package me.jromero.connect2sql.db.model.query;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

public class HistoryQuery extends BaseQuery {

    private int mConnectionId;
    private long mDateTime;

    public HistoryQuery() {
        // used for table creation
    }

    public HistoryQuery(int id, int connection_id, long dateTime, String query) {
        setId(id);
        mConnectionId = connection_id;
        setQuery(query);
        setDateTime(dateTime);
    }

    public int getConnectionId() {
        return mConnectionId;
    }

    public void setConnectionId(int connectionId) {
        mConnectionId = connectionId;
    }

    public long getDateTime() {
        return mDateTime;
    }

    public void setDateTime(long dateTime) {
        mDateTime = dateTime;
    }


    public static class Column extends BaseQuery.Column {
        public static final String CONNECTION_ID = "connection_id";
        public static final String DATETIME = "datetime";
    }

    public static class HistoryQuerySqlModel extends BaseQuerySqlModel<HistoryQuery> {
        public static String TABLE_NAME = "queries_history";

        @Override
        public Class<HistoryQuery> getModelClass() {
            return HistoryQuery.class;
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
                    + "'" + Column.QUERY + "' text NOT NULL,"
                    + "'" + Column.DATETIME + "' integer NOT NULL,"
                    + "PRIMARY KEY('" + Column.ID + "'))";
        }

        @Override
        public HistoryQuery hydrateObject(Cursor cursor) throws InstantiationException, IllegalAccessException {
            final HistoryQuery historyQuery = super.hydrateObject(cursor);
            historyQuery.setConnectionId(cursor.getInt(cursor.getColumnIndex(Column.CONNECTION_ID)));
            historyQuery.setDateTime(cursor.getLong(cursor.getColumnIndex(Column.DATETIME)));
            return historyQuery;
        }

        @Override
        public ContentValues toContentValues(HistoryQuery object) {
            ContentValues cv = super.toContentValues(object);
            cv.put(Column.CONNECTION_ID, object.getConnectionId());
            cv.put(Column.DATETIME, object.getDateTime());
            return cv;
        }
    }
}
