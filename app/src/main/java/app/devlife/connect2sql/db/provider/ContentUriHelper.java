package app.devlife.connect2sql.db.provider;

import java.util.HashMap;

import app.devlife.connect2sql.db.model.SqlModel;
import android.net.Uri;

public class ContentUriHelper {
    private static HashMap<String, HashMap<Class<? extends SqlModel>, Uri>> sCachedUris = new HashMap<>();

    public static Uri getContentUri(Class<? extends SqlModel> clazz) throws BaseUriNotFoundException {
        return getContentUri(AppContentProvider.AUTHORITY, clazz);
    }

    public static Uri getContentUri(String authority,
            Class<? extends SqlModel> clazz) throws BaseUriNotFoundException {

        if (!sCachedUris.containsKey(authority)) {
            sCachedUris.put(authority, new HashMap<Class<? extends SqlModel>, Uri>());
        }

        Uri uri = sCachedUris.get(authority).get(clazz);
        if (uri != null) {
            return uri;
        }

        try {
            uri = Uri.parse("content://" + authority + "/" + clazz.newInstance().getTableName());
        } catch (InstantiationException e) {
            throw new BaseUriNotFoundException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new BaseUriNotFoundException(e.getMessage(), e);
        }

        // cache uri
        sCachedUris.get(authority).put(clazz, uri);

        return uri;
    }

    public static class BaseUriNotFoundException extends Exception {

        private static final long serialVersionUID = -3112681143778552353L;

        public BaseUriNotFoundException(String message) {
            super(message);
        }

        public BaseUriNotFoundException(String message, Throwable e) {
            super(message, e);
        }
    }
}
