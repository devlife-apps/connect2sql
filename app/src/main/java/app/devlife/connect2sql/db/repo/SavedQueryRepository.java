package app.devlife.connect2sql.db.repo;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import net.sqlcipher.database.SQLiteException;

import app.devlife.connect2sql.db.model.query.SavedQuery;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.ui.widget.Toast;
import app.devlife.connect2sql.db.model.query.SavedQuery;
import app.devlife.connect2sql.db.model.query.SavedQuery.SavedQuerySqlModel;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.ui.widget.Toast;

/**
 *
 */
public class SavedQueryRepository {

    private ContentResolver mContentResolver;
    private SavedQuery.SavedQuerySqlModel mSavedQuerySqlModel;
    private Uri mContentUri;

    public SavedQueryRepository(ContentResolver contentResolver, SavedQuery.SavedQuerySqlModel savedQuerySqlModel) throws ContentUriHelper.BaseUriNotFoundException {
        mContentResolver = contentResolver;
        mSavedQuerySqlModel = savedQuerySqlModel;
        mContentUri = ContentUriHelper.getContentUri(SavedQuery.SavedQuerySqlModel.class);
    }

    public boolean saveQuery(SavedQuery savedQuery) {
        return mContentResolver.insert(mContentUri, mSavedQuerySqlModel.toContentValues(savedQuery)) != null;
    }

    /**
     * Delete saved query
     *
     * @param context
     * @param queryId
     * @return boolean - TRUE if query was successfully deleted
     */
    public boolean deleteSavedQuery(Context context, int queryId) {
        boolean success = true;

        try {
            // delete connection
            mContentResolver.delete(mContentUri, "id=?", new String[]{"" + queryId});
        } catch (SQLiteException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            success = false;
        }

        return success;
    }
}
