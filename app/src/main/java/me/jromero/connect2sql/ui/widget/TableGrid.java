package me.jromero.connect2sql.ui.widget;

import java.util.List;

import com.gitlab.connect2sql.R;
import me.jromero.connect2sql.log.EzLogger;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TableGrid extends FrameLayout implements
        HorizontalScrollViewListener {

    //private LayoutInflater mInflater;
    private ObservableHorizontalScrollView mContentHeaderScrollView;
    private TableLayout mFrozenHeader;
    private TableLayout mContentHeader;
    private ScrollView mVerticalScrollView;
    private TableLayout mFrozenColumns;
    private ObservableHorizontalScrollView mContentTableScrollView;
    private TableLayout mContentTable;

    private Typeface mFont = Typeface.create("monospace", Typeface.NORMAL);
    private float mFontSize = 13;
    private float mScale = (float) 1.25;
    private Context mContext;
    private TableRow mFrozenTableHeaderRow;
    private TableRow mContentTableHeaderRow;
    private Drawable mCellBackground;
    private Drawable mHeaderCellBackground;
    private Drawable mFrozenColumnBackground;
    private OnCellEventListener mOnCellEventListener;

    public TableGrid(Context context) {
        super(context);
        init(context);
    }

    public TableGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        readAttributes(context, attrs);
    }

    public TableGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        readAttributes(context, attrs);
    }

    private void readAttributes(Context context, AttributeSet attrs) {

    }

    private void init(Context context) {

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_table_grid, this, true);

        // get view references
        mContentHeaderScrollView = (ObservableHorizontalScrollView) findViewById(R.id.ohsv_content_header);
        mFrozenHeader = (TableLayout) findViewById(R.id.tl_frozen_header);
        mContentHeader = (TableLayout) findViewById(R.id.tl_content_header);
        mVerticalScrollView = (ScrollView) findViewById(R.id.sv_vertical);
        mFrozenColumns = (TableLayout) findViewById(R.id.tl_frozen_columns);
        mContentTableScrollView = (ObservableHorizontalScrollView) findViewById(R.id.ohsv_content_table);
        mContentTable = (TableLayout) findViewById(R.id.tl_content_table);

        // keep scroll views in sync
        mContentHeaderScrollView.setScrollViewListener(this);
        mContentTableScrollView.setScrollViewListener(this);

        // only show the scroll bar on the header table
        mContentTableScrollView.setHorizontalScrollBarEnabled(false);

        // set default cell backgrounds
        mCellBackground = getResources().getDrawable(
                R.drawable.widget_tg_content_cell);
        mHeaderCellBackground = getResources().getDrawable(
                R.drawable.widget_tg_header_cell);
        mFrozenColumnBackground = getResources().getDrawable(
                R.drawable.widget_tg_frozen_column_cell);
    }

    /**
     * Set the density (scale) to appropriately scale font and other scale
     * dependent elements
     *
     * @param scale
     */
    public void setScale(float scale) {
        mScale = scale;
    }

    public float getFontSize() {
        return mFontSize;
    }

    public void setFontSize(float size) {
        mFontSize = size;
    }

    public void setHeaderCellBackground(Drawable drawable) {
        mHeaderCellBackground = drawable;
    }

    public void setCellBackground(Drawable drawable) {
        mCellBackground = drawable;
    }

    public void setFrozenColumnBackground(Drawable drawable) {
        mFrozenColumnBackground = drawable;
    }

    public void setOnCellEventListener(OnCellEventListener listener) {
        mOnCellEventListener = listener;
    }

    /**
     * Draw given data onto table Note: We prevent storing the data to keep
     * memory use at a minimum since some datasets can be huge!
     *
     * @param data
     */
    public void draw(List<String[]> data) throws RuntimeException,
            OutOfMemoryError {
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        int maxFrozenChars = 0;
        int[] maxContentChars = new int[data.get(0).length - 1];

        EzLogger.d("Looping through content...");
        for (int i = 0; i < data.size(); i++) {

            TableRow frozenColumnRow = new TableRow(mContext);
            frozenColumnRow.setLayoutParams(tableRowParams);
            TextView frozenColumnCell = new TextView(mContext);

            // FIXME: allow for multi-line cells
            // frozen column should match content cell height
            frozenColumnCell.setSingleLine(true);
            frozenColumnCell.setText(data.get(i)[0]);
            frozenColumnCell.setTextColor(Color.parseColor("#000000"));
            frozenColumnCell
                    .setBackgroundResource(R.drawable.widget_tg_frozen_column_cell);
            if (0 == i) {
                frozenColumnCell.setTypeface(mFont, Typeface.BOLD);
                frozenColumnCell.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener.onCellClick(v, true, true);
                        }
                    }
                });
                frozenColumnCell
                        .setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (mOnCellEventListener != null) {
                                    mOnCellEventListener.onCellLongClick(v,
                                            true, true);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
            } else {
                frozenColumnCell.setTypeface(mFont, Typeface.NORMAL);
                frozenColumnCell.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener.onCellClick(v, false, true);
                        }
                    }
                });
                frozenColumnCell
                        .setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (mOnCellEventListener != null) {
                                    mOnCellEventListener.onCellLongClick(v,
                                            false, true);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
            }

            frozenColumnCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                    getFontSize());
            frozenColumnRow.addView(frozenColumnCell);

            if (data.get(i).length < 1) {
                continue;
            }

            if (data.get(i)[0].length() > maxFrozenChars) {
                maxFrozenChars = data.get(i)[0].length();
            }

            // The rest of them
            TableRow row = new TableRow(mContext);
            row.setLayoutParams(tableRowParams);
            for (int j = 1; j < data.get(0).length; j++) {
                TextView rowCell = new TextView(mContext);
                rowCell.setSingleLine(true);
                rowCell.setText(data.get(i)[j]);
                rowCell.setGravity(Gravity.LEFT);
                rowCell.setTextColor(Color.parseColor("#000000"));
                if (0 == i) {
                    rowCell.setTypeface(mFont, Typeface.BOLD);
                    rowCell.setBackgroundResource(R.drawable.widget_tg_header_cell);
                    rowCell.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnCellEventListener != null) {
                                mOnCellEventListener
                                        .onCellClick(v, true, false);
                            }
                        }
                    });
                    rowCell.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mOnCellEventListener != null) {
                                mOnCellEventListener.onCellLongClick(v, true,
                                        false);

                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                } else {
                    rowCell.setTypeface(mFont, Typeface.NORMAL);
                    rowCell.setBackgroundResource(R.drawable.widget_tg_content_cell);
                    rowCell.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnCellEventListener != null) {
                                mOnCellEventListener.onCellClick(v, false,
                                        false);
                            }
                        }
                    });
                    rowCell.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mOnCellEventListener != null) {
                                mOnCellEventListener.onCellLongClick(v, false,
                                        false);

                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                }
                rowCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getFontSize());
                rowCell.setLines(1);
                row.addView(rowCell);
                if (data.get(i)[j].length() > maxContentChars[j - 1]) {
                    maxContentChars[j - 1] = data.get(i)[j].length();
                }
            }

            if (i == 0) {
                mFrozenTableHeaderRow = frozenColumnRow;
                mContentTableHeaderRow = row;
                mFrozenHeader.addView(frozenColumnRow);
                mContentHeader.addView(row);
            } else {
                mFrozenColumns.addView(frozenColumnRow);
                mContentTable.addView(row);
            }
        }

        EzLogger.d("Setting child text view widths...");
        setCellWidths(mFrozenTableHeaderRow, new int[] { maxFrozenChars });
        setCellWidths(mContentTableHeaderRow, maxContentChars);
        for (int i = 0; i < mContentTable.getChildCount(); i++) {
            TableRow frozenRow = (TableRow) mFrozenColumns.getChildAt(i);
            setCellWidths(frozenRow, new int[] { maxFrozenChars });

            TableRow row = (TableRow) mContentTable.getChildAt(i);
            setCellWidths(row, maxContentChars);
        }
    }

    /**
     * Removes all data from current row
     */
    public void clear() {
        if (mFrozenHeader != null) {
            mFrozenHeader.removeAllViews();
        }

        if (mFrozenTableHeaderRow != null) {
            mFrozenTableHeaderRow.removeAllViews();
        }

        if (mFrozenColumns != null) {
            mFrozenColumns.removeAllViews();
        }

        if (mContentTableHeaderRow != null) {
            mContentTableHeaderRow.removeAllViews();
        }

        if (mContentTable != null) {
            mContentTable.removeAllViews();
        }
    }

    /**
     * Dynamically set the widths of each cell
     *
     * @param row
     * @param widths
     */
    private void setCellWidths(TableRow row, int[] widths) {
        if (row == null) {
            return;
        }

        int widthFactor = (int) Math.ceil(getFontSize() * mScale
                * (getFontSize() < 10 ? 0.9 : 0.7));

        int padding = 10; // account for padding
        for (int i = 0; i < row.getChildCount(); i++) {

            // get cell/textviews
            TextView cell = (TextView) row.getChildAt(i);

            int newWidth = 0;
            if (widths[i] == 1) {
                newWidth = (int) Math.ceil(widths[i] * widthFactor * 2);
            } else if (widths[i] < 3) {
                newWidth = (int) Math.ceil(widths[i] * widthFactor * 1.7);
            } else if (widths[i] < 5) {
                newWidth = (int) Math.ceil(widths[i] * widthFactor * 1.2);
            } else {
                newWidth = widths[i] * widthFactor;
            }

            cell.setMinimumWidth(newWidth + padding);
            cell.setMaxWidth(newWidth + padding);
        }
    }

    /**
     * Resize all cells in frozen (first) column
     *
     * @param width
     * @return original width before resize
     */
    public int resizeFrozenColumn(int width) {

        TextView headerCell = (TextView) mFrozenTableHeaderRow.getChildAt(0);

        int originalWidth = headerCell.getWidth();
        headerCell.setWidth(width);
        headerCell.setMaxWidth(width);
        headerCell.setMinimumWidth(width);

        for (int i = 0; i < mFrozenColumns.getChildCount(); i++) {
            TableRow frozenRow = (TableRow) mFrozenColumns.getChildAt(i);
            for (int j = 0; j < frozenRow.getChildCount(); j++) {
                TextView cell = (TextView) frozenRow.getChildAt(j);
                cell.setWidth(width);
                cell.setMaxWidth(width);
                cell.setMinimumWidth(width);
            }
        }

        return originalWidth;
    }

    /**
     * Keep scroll views in sync
     */
    @Override
    public void onScrollChanged(ObservableHorizontalScrollView scrollView,
            int x, int y, int oldX, int oldY) {
        if (scrollView == mContentHeaderScrollView) {
            mContentTableScrollView.scrollTo(x, y);
        } else if (scrollView == mContentTableScrollView) {
            mContentHeaderScrollView.scrollTo(x, y);
        }
    }

    public interface OnCellEventListener {
        /**
         * Called when a cell (TextView) is clicked
         *
         * @param cell
         * @param isHeader
         * @param isFrozenColumn
         */
        void onCellClick(View cell, boolean isHeader, boolean isFrozenColumn);

        /**
         * Called when a cell (TextView) is long clicked
         *
         * @param cell
         * @param isHeader
         * @param isFrozenColumn
         */
        void onCellLongClick(View cell, boolean isHeader, boolean isFrozenColumn);
    }
}
