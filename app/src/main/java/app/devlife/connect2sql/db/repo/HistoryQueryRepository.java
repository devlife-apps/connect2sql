package app.devlife.connect2sql.db.repo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import app.devlife.connect2sql.db.model.connection.ConnectionInfo;
import app.devlife.connect2sql.db.model.query.HistoryQuery;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.ui.widget.Toast;
import app.devlife.connect2sql.db.model.connection.ConnectionInfo;
import app.devlife.connect2sql.db.model.query.HistoryQuery;
import app.devlife.connect2sql.db.model.query.HistoryQuery.HistoryQuerySqlModel;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.exceptions.NotableException;
import app.devlife.connect2sql.log.EzLogger;
import app.devlife.connect2sql.ui.widget.Toast;

/**
 *
 */
public class HistoryQueryRepository {

    private ContentResolver mContentResolver;
    private Uri mContentUri;

    public HistoryQueryRepository(ContentResolver contentResolver) throws ContentUriHelper.BaseUriNotFoundException {
        mContentResolver = contentResolver;
        mContentUri = ContentUriHelper.getContentUri(HistoryQuery.HistoryQuerySqlModel.class);
    }

    public List<HistoryQuery> getQueryHistory(Context context,
                                              long connectionId) {

        List<HistoryQuery> queries = new ArrayList<HistoryQuery>();

        try {
            Cursor cursor = mContentResolver.query(mContentUri, null, "connection_id=?", new String[]{"" + connectionId}, "datetime DESC");

            // no data checks
            if (cursor == null) {
                throw new NotableException("Cursor was null, no results?");
            }

            if (!cursor.moveToFirst()) {
                throw new NotableException(
                        "Cursor failed to move to first result.");
            }

            do {
                int id = cursor.getInt(0);
                int connection_id = cursor.getInt(1);
                String query = cursor.getString(2);
                long dateTime = cursor.getLong(3);
                queries.add(new HistoryQuery(id, connection_id, dateTime, query));
            } while (cursor.moveToNext());

            cursor.close();
        } catch (NotableException e) {
            EzLogger.i(e.getMessage());
        } catch (Exception e) {
            EzLogger.e(e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return queries;
    }

    public boolean purgeQueryHistory(Context context,
                                     ConnectionInfo connectionInfo, int keepAmount) {
        // FIXME: Should be implemented!
        return true;
    }


    public boolean deleteQueryHistory(Context context,
                                      ConnectionInfo connectionInfo) {
        return mContentResolver.delete(mContentUri, HistoryQuery.Column.CONNECTION_ID + "=?", new String[]{"" + connectionInfo}) >= 1;
    }

    public boolean deleteQueryHistory(Context context, int queryId) {
        return mContentResolver.delete(mContentUri, HistoryQuery.Column.ID + "=?", new String[]{"" + queryId}) >= 1;
    }

    public boolean saveQueryHistory(Context context,
                                    ConnectionInfo connectionInfo, String queryText) {
        boolean success = false;

        if (connectionInfo == null) {
            EzLogger.i("Failed to save query history because connection info was null!");
            return false;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(HistoryQuery.Column.CONNECTION_ID, connectionInfo.getId());
            values.put(HistoryQuery.Column.QUERY, queryText);
            values.put(HistoryQuery.Column.DATETIME, System.currentTimeMillis() / 1000L);
            if (mContentResolver.insert(mContentUri, values) != null) {
                success = true;
            }
        } catch (SQLException e) {
            EzLogger.e(e.getMessage());
        }

        return success;
    }
}
