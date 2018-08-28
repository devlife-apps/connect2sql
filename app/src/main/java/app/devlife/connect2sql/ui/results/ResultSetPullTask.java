package app.devlife.connect2sql.ui.results;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class ResultSetPullTask extends AsyncTask<Void, Integer, Throwable> {

    private static final String TAG = ResultSetPullTask.class.getSimpleName();

    private ResultSet mResultSet;
    private int mStartIndex;
    private int mDisplayLimit;
    /**
     * TODO: Make it a weak reference?
     */
    private CallbackListener mCallbackListener;

    private String[] mColumnNames;
    private List<String[]> mData = new ArrayList<String[]>();
    private int mTotalRows;

    public ResultSetPullTask(ResultSet rs, int startIndex, int displayLimit,
            CallbackListener listener) {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet cannot be null!");
        } else if (listener == null) {
            throw new IllegalArgumentException("CallbackListener cannot be null!");
        }

        mResultSet = rs;
        mStartIndex = startIndex;
        mDisplayLimit = displayLimit;
        mCallbackListener = listener;
        if (mCallbackListener == null) {
            throw new IllegalArgumentException("CallbackListener must be set!");
        }

        mCallbackListener = listener;
    }

    @Override
    protected Throwable doInBackground(Void... params) {

        try {
            ResultSetMetaData rsmd = mResultSet.getMetaData();
            int columnCount = rsmd.getColumnCount();
            mResultSet.last();
            mTotalRows = mResultSet.getRow();

            Log.d(TAG, "Getting column names...");
            mColumnNames = new String[columnCount];
            Integer[] columnTypes = new Integer[columnCount];
            for (int i = 0; i < columnCount; i++) {
                mColumnNames[i] = rsmd.getColumnLabel(i + 1);
                Log.d(TAG, "Got column: " + mColumnNames[i]);

                columnTypes[i] = rsmd.getColumnType(i + 1);
                Log.d(TAG, "Type: " + columnTypes[i]);
            }

            /**
             * Calculate last "displayed" index
             */
            int lastDisplayedIndex = mStartIndex + (mDisplayLimit - 1);
            lastDisplayedIndex = lastDisplayedIndex < mTotalRows ?
                    lastDisplayedIndex : mTotalRows;

            int displayIndex = mStartIndex;
            while (displayIndex <= lastDisplayedIndex) {

                mResultSet.absolute(displayIndex);
                String[] record = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    if (mResultSet.getString(i + 1) == null) {
                        record[i] = "[null]";
                    } else if (columnTypes[i] == Types.BLOB
                            || columnTypes[i] == Types.LONGVARBINARY) {
                        record[i] = "[blob]";
                    } else {
                        record[i] = mResultSet.getString(i + 1);
                    }
                }

                mData.add(record);
                displayIndex++;

                publishProgress(displayIndex, lastDisplayedIndex);
            }

        }catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
            return e;
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallbackListener.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        mCallbackListener.onProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(Throwable result) {
        super.onPostExecute(result);
        if (result != null) {
            mCallbackListener.onError(result);
        } else {
            mCallbackListener.onComplete(mColumnNames, mData , mTotalRows);
        }
    }

    public static interface CallbackListener {
        public void onPreExecute();

        public void onProgressUpdate(int current, int total);

        public void onError(Throwable throwable);

        public void onComplete(String[] columnsNames, List<String[]> data, int totalRecords);
    }
}
