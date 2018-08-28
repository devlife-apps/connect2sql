package app.devlife.connect2sql.loader;

import app.devlife.connect2sql.db.model.connection.ConnectionInfoSqlModel;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.db.model.connection.ConnectionInfo;
import app.devlife.connect2sql.db.model.connection.ConnectionInfoSqlModel;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.db.provider.ContentUriHelper.BaseUriNotFoundException;
import android.content.Context;
import android.support.v4.content.CursorLoader;

public class ConnectionInfoCursorLoader extends CursorLoader {

    public ConnectionInfoCursorLoader(Context context) throws ContentUriHelper.BaseUriNotFoundException {
        super(context);
        setUri(ContentUriHelper.getContentUri(ConnectionInfoSqlModel.class));
        setSortOrder(ConnectionInfoSqlModel.Column.NAME + " ASC");
    }

    public ConnectionInfoCursorLoader(Context context, long connectionId) throws ContentUriHelper.BaseUriNotFoundException {
        super(context);
        setUri(ContentUriHelper.getContentUri(ConnectionInfoSqlModel.class));
        setSelection(ConnectionInfoSqlModel.Column.ID + " = ?");
        setSelectionArgs(new String[]{"" + connectionId});
    }
}
