package me.jromero.connect2sql.db.model.query;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class BaseNamedQuery extends BaseQuery {

    private String mName;

    public BaseNamedQuery() {
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public static class Column extends BaseQuery.Column {
        public static final String NAME = "name";
    }

    public static abstract class BaseNamedQuerySqlModel<T extends BaseNamedQuery> extends BaseQuerySqlModel<T> {

        @Override
        public T hydrateObject(Cursor cursor) throws InstantiationException, IllegalAccessException {
            final T t = super.hydrateObject(cursor);
            t.setName(cursor.getString(cursor.getColumnIndex(Column.NAME)));
            return t;
        }

        @Override
        public ContentValues toContentValues(T object) {
            ContentValues cv = super.toContentValues(object);
            cv.put(Column.NAME, object.getName());
            return cv;
        }
    }
}
