package app.devlife.connect2sql.ui.results;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gitlab.connect2sql.R;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import app.devlife.connect2sql.fragment.BaseFragment;
import app.devlife.connect2sql.log.EzLogger;
import app.devlife.connect2sql.ui.widget.TableGrid;
import app.devlife.connect2sql.ui.widget.TableGrid.OnCellEventListener;
import app.devlife.connect2sql.ui.widget.Toast;

public class ResultsTableFragment extends BaseFragment implements
    OnCellEventListener, android.view.View.OnClickListener {

    private static final String TAG = ResultsTableFragment.class.getSimpleName();

    private TableGrid mResultsTable;
    private List<String[]> mData = new ArrayList<>();
    private int mNormalFrozenColumnWidth = 0;
    private TextView mPagingTextView;
    private ResultSet mResultSet;

    private ImageButton mPagingPrevButton;
    private ImageButton mPagingNextButton;

    public static ResultsTableFragment newInstance(ResultSet rs, int startIndex) {
        ResultsTableFragment frag = new ResultsTableFragment();
        //FIXME: We need this to rely on a host instead otherwise it WILL crash on orientation change.
        frag.setResultSet(rs);
        frag.setStartIndex(startIndex);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EzLogger.d("Creating fragment...");
        if (container == null) {
            return null;
        }

        EzLogger.d("Setting main content view...");
        View v = inflater.inflate(R.layout.fragment_results_table, container, false);

        mResultsTable = (TableGrid) v.findViewById(R.id.tg_results);

        float scale = getActivity().getResources().getDisplayMetrics().density;
        EzLogger.i("Scale: " + scale);
        mResultsTable.setScale(scale);
        mResultsTable.setOnCellEventListener(this);

        LinearLayout paginationBar = (LinearLayout) v
            .findViewById(R.id.pagination_bar);
        mPagingTextView = (TextView) paginationBar.findViewById(R.id.text1);

        mPagingPrevButton = ((ImageButton) paginationBar.findViewById(R.id.button1));
        mPagingNextButton = ((ImageButton) paginationBar.findViewById(R.id.button2));

        mPagingPrevButton.setOnClickListener(this);
        mPagingNextButton.setOnClickListener(this);

        setPaginationText(0, 0, 0);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mResultSet != null) {
            new ResultSetPullTask(mResultSet, mStartIndex, mDisplayLimit,
                mResultSetPullListener).execute((Void) null);
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onCellClick(View cell, boolean isHeader, boolean isFrozenColumn) {
        if (isFrozenColumn) {
            if (mNormalFrozenColumnWidth == 0) {
                EzLogger.d("Resizing frozen column...");
                mNormalFrozenColumnWidth = mResultsTable
                    .resizeFrozenColumn(100);
            } else {
                EzLogger.d("Resizing (to original) frozen column...");
                mResultsTable.resizeFrozenColumn(mNormalFrozenColumnWidth);
                mNormalFrozenColumnWidth = 0;
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onCellLongClick(View cell, boolean isHeader,
                                boolean isFrozenColumn) {
        String text = ((TextView) cell).getText().toString();

        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);

        Toast.makeText(getActivity(),
            "'" + text + "' has been copied to clipboard.",
            Toast.LENGTH_LONG).show();
    }

    public float increaseFontSize(int i) {
        float fontSize = mResultsTable.getFontSize() + i;
        mResultsTable.setFontSize(fontSize);
        return fontSize;
    }

    public float decreaseFontSize(int i) {
        float fontSize = mResultsTable.getFontSize() - i;
        mResultsTable.setFontSize(fontSize);
        return fontSize;
    }

    public void redrawTable() {
        mResultsTable.clear();
        mResultsTable.draw(mData);
    }

    private void setResultSet(ResultSet resultSet) {
        mResultSet = resultSet;
    }

    protected void setPaginationText(int from, int to, int total) {
        mPagingTextView.setText(getString(R.string.results_showing_records_to, from, to, total));
    }

    /***
     * Pagination tracking variables
     */
    private final int mDisplayLimit = 30;
    private int mStartIndex = 1;
    private int mTotalRows = 0;

    private ResultSetPullTask.CallbackListener mResultSetPullListener = new ResultSetPullTask.CallbackListener() {

        @Override
        public void onProgressUpdate(int current, int total) {
            Log.d(TAG, "onProgressUpdate: " + current + ", " + total);
        }

        @Override
        public void onPreExecute() {
            Log.d(TAG, "onPreExecute");
        }

        @Override
        public void onError(Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);

            final FragmentActivity activity = getActivity();
            if (activity == null) {
                EzLogger.w("Activity has gone away!");
                return;
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Error");
            builder.setMessage(throwable.getMessage());
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    activity.finish();
                }
            });
            builder.create().show();
        }

        @Override
        public void onComplete(String[] columnsNames, List<String[]> data,
                               int totalRecords) {
            Log.d(TAG, "columnsNames count: " + columnsNames.length);
            Log.d(TAG, "data count: " + data.size());
            Log.d(TAG, "totalRecords: " + totalRecords);
            mData.clear();
            mData.addAll(data);
            mData.add(0, columnsNames);
            redrawTable();
            mTotalRows = totalRecords;

            updatePaging(mStartIndex, data.size(), mTotalRows);
        }
    };

    /**
     * Sets the index of the record to retrieve on next execution of
     * ProcessResultSet
     *
     * @param index
     */
    private void setStartIndex(int index) {
        if (index < 1) {
            index = 1;
        }

        mStartIndex = index;
    }

    public int getPreviousStartIndex() {
        int previousStartIndex = mStartIndex - mDisplayLimit;
        if (previousStartIndex < 1) {
            previousStartIndex = 1;
        }

        return previousStartIndex;
    }

    public int getNextStartIndex() {
        return mStartIndex + mDisplayLimit;
    }

    private void updatePaging(int startIndex, int rows, int totalRows) {
        int start = startIndex;
        int end = startIndex + (rows - 1);
        if (rows == 0) {
            start--;
        }

        if (start <= 1) {
            mPagingPrevButton.setEnabled(false);
        } else {
            mPagingPrevButton.setEnabled(true);
        }

        if (end >= totalRows) {
            mPagingNextButton.setEnabled(false);
        } else {
            mPagingNextButton.setEnabled(true);
        }

        setPaginationText(start, end, totalRows);
    }

    @Override
    public void onClick(View button) {
        int id = button.getId();
        switch (button.getId()) {
            case R.id.button1:
                // previous button
                setStartIndex(getPreviousStartIndex());
                mResultsTable.clear();
                mData.clear();
                new ResultSetPullTask(mResultSet, mStartIndex, mDisplayLimit,
                    mResultSetPullListener).execute((Void) null);
                break;
            case R.id.button2:
                // next button
                setStartIndex(getNextStartIndex());
                mResultsTable.clear();
                mData.clear();
                new ResultSetPullTask(mResultSet, mStartIndex, mDisplayLimit,
                    mResultSetPullListener).execute((Void) null);
                break;
            default:
                Log.wtf(TAG, "What does this button do? " + id);
                break;
        }
    }
}