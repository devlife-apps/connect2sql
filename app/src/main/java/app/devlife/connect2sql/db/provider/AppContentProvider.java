package app.devlife.connect2sql.db.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import net.sqlcipher.database.SQLiteDatabase;

import javax.inject.Inject;

import app.devlife.connect2sql.di.DaggerContentProviderComponent;
import app.devlife.connect2sql.di.DatabaseModule;
import app.devlife.connect2sql.di.SecurityModule;
import app.devlife.connect2sql.db.AppDatabaseHelperV3;
import app.devlife.connect2sql.db.model.connection.ConnectionInfo;
import app.devlife.connect2sql.db.model.connection.ConnectionInfoSqlModel;
import app.devlife.connect2sql.db.model.query.BuiltInQuery;
import app.devlife.connect2sql.db.model.query.BuiltInQuery.BuiltInQuerySqlModel;
import app.devlife.connect2sql.db.model.query.HistoryQuery;
import app.devlife.connect2sql.db.model.query.HistoryQuery.HistoryQuerySqlModel;
import app.devlife.connect2sql.db.model.query.SavedQuery;
import app.devlife.connect2sql.db.model.query.SavedQuery.SavedQuerySqlModel;
import app.devlife.connect2sql.di.DaggerContentProviderComponent;
import app.devlife.connect2sql.di.DatabaseModule;
import app.devlife.connect2sql.di.SecurityModule;
import app.devlife.connect2sql.log.EzLogger;
import app.devlife.connect2sql.log.Log;

public class AppContentProvider extends ContentProvider {

    private static final String TAG = AppContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "app.devlife.connect2sql.db.provider.AppContentProvider";
    private static final String VND_PATH = "vnd.app.devlife.connect2sql.db";
    private static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/";
    private static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/";

    private static final int URI_CODE_CONNECTION = 1;
    private static final int URI_CODE_HISTORY_QUERY = 2;
    private static final int URI_CODE_SAVED_QUERY = 3;
    private static final int URI_CODE_BUILTIN_QUERY = 4;

    @Inject
    AppDatabaseHelperV3 mDatabaseHelper;
    private UriMatcher mCachedUriMatcher;

    public String getAuthority() {
        return AUTHORITY;
    }

    public UriMatcher getUriMatcher() {
        if (mCachedUriMatcher == null) {
            mCachedUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            mCachedUriMatcher.addURI(getAuthority(), ConnectionInfoSqlModel.TABLE_NAME, URI_CODE_CONNECTION);
            mCachedUriMatcher.addURI(getAuthority(), HistoryQuerySqlModel.TABLE_NAME, URI_CODE_HISTORY_QUERY);
            mCachedUriMatcher.addURI(getAuthority(), SavedQuerySqlModel.TABLE_NAME, URI_CODE_SAVED_QUERY);
            mCachedUriMatcher.addURI(getAuthority(), BuiltInQuerySqlModel.TABLE_NAME, URI_CODE_BUILTIN_QUERY);
        }

        return mCachedUriMatcher;
    }

    @Override
    public boolean onCreate() {
        DaggerContentProviderComponent.builder()
                .databaseModule(new DatabaseModule(getContext()))
                .securityModule(new SecurityModule(getContext()))
                .build()
                .inject(this);

        SQLiteDatabase.loadLibs(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (getUriMatcher().match(uri)) {
            case URI_CODE_CONNECTION:
                return CONTENT_TYPE_DIR + VND_PATH + "." + ConnectionInfoSqlModel.TABLE_NAME;
            case URI_CODE_HISTORY_QUERY:
                return CONTENT_TYPE_DIR + VND_PATH + "." + HistoryQuerySqlModel.TABLE_NAME;
            case URI_CODE_SAVED_QUERY:
                return CONTENT_TYPE_DIR + VND_PATH + "." + SavedQuerySqlModel.TABLE_NAME;
            case URI_CODE_BUILTIN_QUERY:
                return CONTENT_TYPE_DIR + VND_PATH + "." + BuiltInQuerySqlModel.TABLE_NAME;
            default:
                EzLogger.e("No type defined for: " + uri);
                return null;
        }
    }


    @Override
    @Nullable
    public Cursor query(@Nullable Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        try {
            SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

            Cursor cursor;
            String groupBy = null;
            String having = null;

            switch (getUriMatcher().match(uri)) {
                case URI_CODE_CONNECTION:
                    cursor = db.query(ConnectionInfoSqlModel.TABLE_NAME, projection, selection, selectionArgs, groupBy, having, sortOrder);
                    break;
                case URI_CODE_HISTORY_QUERY:
                    cursor = db.query(HistoryQuerySqlModel.TABLE_NAME, projection, selection, selectionArgs, groupBy, having, sortOrder);
                    break;
                case URI_CODE_SAVED_QUERY:
                    cursor = db.query(SavedQuerySqlModel.TABLE_NAME, projection, selection, selectionArgs, groupBy, having, sortOrder);
                    break;
                case URI_CODE_BUILTIN_QUERY:
                    cursor = db.query(BuiltInQuerySqlModel.TABLE_NAME, projection, selection, selectionArgs, groupBy, having, sortOrder);
                    break;
                default:
                    EzLogger.e("No query defined for: " + uri);
                    return null;
            }

            cursor.setNotificationUri(getContext().getContentResolver(), uri);

            return cursor;
        } catch (AppDatabaseHelperV3.PassphraseNotEnteredException e) {
            EzLogger.e(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        try {

            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            Uri baseUri = null;
            Uri newObjectUri = null;
            long rowId = -1;

            switch (getUriMatcher().match(uri)) {
                case URI_CODE_CONNECTION:
                    rowId = db.insertWithOnConflict(ConnectionInfoSqlModel.TABLE_NAME,
                            ConnectionInfoSqlModel.Column.ID, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    baseUri = ContentUriHelper.getContentUri(ConnectionInfoSqlModel.class);
                    break;
                case URI_CODE_HISTORY_QUERY:
                    rowId = db.insertWithOnConflict(HistoryQuerySqlModel.TABLE_NAME,
                            HistoryQuery.Column.ID, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    baseUri = ContentUriHelper.getContentUri(HistoryQuerySqlModel.class);
                    break;
                case URI_CODE_SAVED_QUERY:
                    rowId = db.insertWithOnConflict(SavedQuerySqlModel.TABLE_NAME,
                            SavedQuery.Column.ID, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    baseUri = ContentUriHelper.getContentUri(SavedQuerySqlModel.class);
                    break;
                case URI_CODE_BUILTIN_QUERY:
                    rowId = db.insertWithOnConflict(BuiltInQuerySqlModel.TABLE_NAME,
                            BuiltInQuery.Column.ID, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    baseUri = ContentUriHelper.getContentUri(BuiltInQuerySqlModel.class);
                    break;
                default:
                    EzLogger.e("No insert defined for: " + uri);
                    return null;
            }

            if (rowId >= 0 && baseUri != null) {
                newObjectUri = ContentUris.withAppendedId(baseUri, rowId);
                getContext().getContentResolver().notifyChange(baseUri, null);
            }

            return newObjectUri;

        } catch (Exception e) {
            Log.e(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        try {
            int count = 0;
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

            switch (getUriMatcher().match(uri)) {
                case URI_CODE_CONNECTION:
                    count = db.updateWithOnConflict(ConnectionInfoSqlModel.TABLE_NAME, values,
                            selection, selectionArgs,
                            SQLiteDatabase.CONFLICT_IGNORE);
                    break;
                case URI_CODE_HISTORY_QUERY:
                    count = db.updateWithOnConflict(HistoryQuerySqlModel.TABLE_NAME, values,
                            selection, selectionArgs,
                            SQLiteDatabase.CONFLICT_IGNORE);
                    break;
                case URI_CODE_SAVED_QUERY:
                    count = db.updateWithOnConflict(SavedQuerySqlModel.TABLE_NAME, values,
                            selection, selectionArgs,
                            SQLiteDatabase.CONFLICT_IGNORE);
                    break;
                case URI_CODE_BUILTIN_QUERY:
                    count = db.updateWithOnConflict(BuiltInQuerySqlModel.TABLE_NAME, values,
                            selection, selectionArgs,
                            SQLiteDatabase.CONFLICT_IGNORE);
                    break;
                default:
                    EzLogger.e("No update defined for: " + uri);
                    // we do nothing!
            }

            if (count > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return count;
        } catch (Exception e) {
            EzLogger.e(e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        try {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

            int count = 0;

            switch (getUriMatcher().match(uri)) {
                case URI_CODE_CONNECTION:
                    count = db.delete(ConnectionInfoSqlModel.TABLE_NAME, selection, selectionArgs);
                    break;
                case URI_CODE_HISTORY_QUERY:
                    count = db.delete(HistoryQuerySqlModel.TABLE_NAME, selection, selectionArgs);
                    break;
                case URI_CODE_SAVED_QUERY:
                    count = db.delete(SavedQuerySqlModel.TABLE_NAME, selection, selectionArgs);
                    break;
                case URI_CODE_BUILTIN_QUERY:
                    count = db.delete(BuiltInQuerySqlModel.TABLE_NAME, selection, selectionArgs);
                    break;
                default:
                    EzLogger.e("No delete defined for: " + uri);
                    break;
            }

            if (count > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return count;
        } catch (Exception e){
            EzLogger.e(e.getMessage(), e);
            return 0;
        }
    }
}
