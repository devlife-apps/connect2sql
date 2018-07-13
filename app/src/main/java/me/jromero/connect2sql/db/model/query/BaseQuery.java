package me.jromero.connect2sql.db.model.query;

import me.jromero.connect2sql.db.model.SqlModel;
import android.content.ContentValues;
import android.database.Cursor;

public abstract class BaseQuery {

    private int mId;
    private String mQuery;

    public BaseQuery() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    public static class Column {
        public static final String ID = "id";
        public static final String QUERY = "query";
    }

    public static abstract class BaseQuerySqlModel<T extends BaseQuery> implements SqlModel<T> {

        @Override
        public T hydrateObject(Cursor cursor) throws IllegalAccessException, InstantiationException {
            final T t = getModelClass().newInstance();
            t.setId(cursor.getInt(cursor.getColumnIndex(Column.ID)));
            t.setQuery(cursor.getString(cursor.getColumnIndex(Column.QUERY)));
            return t;
        }

        @Override
        public ContentValues toContentValues(T object) {
            ContentValues cv = new ContentValues();
            if (object.getId() > 0) {
                cv.put(Column.ID, object.getId());
            }
            cv.put(Column.QUERY, object.getQuery());
            return cv;
        }
    }
}
