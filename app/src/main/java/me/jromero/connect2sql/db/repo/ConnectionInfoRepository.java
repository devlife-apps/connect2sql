package me.jromero.connect2sql.db.repo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import me.jromero.connect2sql.db.model.connection.ConnectionInfo;
import me.jromero.connect2sql.db.model.connection.ConnectionInfoSqlModel;
import me.jromero.connect2sql.db.provider.ContentUriHelper;

/**
 *
 */
public class ConnectionInfoRepository {

    private ContentResolver mContentResolver;
    private ConnectionInfoSqlModel mConnectionInfoSqlModel;
    private Uri mContentUri;

    public ConnectionInfoRepository(ContentResolver contentResolver, ConnectionInfoSqlModel connectionInfoSqlModel) throws ContentUriHelper.BaseUriNotFoundException {
        mContentResolver = contentResolver;
        mConnectionInfoSqlModel = connectionInfoSqlModel;
        mContentUri = ContentUriHelper.getContentUri(ConnectionInfoSqlModel.class);
    }

    public long save(ConnectionInfo connectionInfo) {
        Uri uri = mContentResolver.insert(mContentUri, mConnectionInfoSqlModel.toContentValues(connectionInfo));
        return uri == null ? -1 : ContentUris.parseId(uri);
    }

    public boolean delete(long connectionId) {
        return mContentResolver.delete(mContentUri,
                ConnectionInfoSqlModel.Column.ID + "=" + connectionId, null) >= 1;
    }

    public ConnectionInfo getConnectionInfo(long id) {
        Cursor cursor = mContentResolver.query(mContentUri, null,
                ConnectionInfoSqlModel.Column.ID + " = ?",
                new String[]{"" + id},
                null);

        if (cursor == null || !cursor.moveToFirst()) {
            throw new RuntimeException("ConnectionInfo not found: " + id);
        }

        final ConnectionInfo connectionInfo = mConnectionInfoSqlModel.hydrateObject(cursor);
        cursor.close();

        return connectionInfo;
    }
}
