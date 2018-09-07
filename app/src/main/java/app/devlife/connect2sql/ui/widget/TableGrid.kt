package app.devlife.connect2sql.ui.widget

import com.gitlab.connect2sql.R
import app.devlife.connect2sql.log.EzLogger
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

class TableGrid : FrameLayout, HorizontalScrollViewListener {

    //private LayoutInflater mInflater;
    private var mContentHeaderScrollView: ObservableHorizontalScrollView? = null
    private var mFrozenHeader: TableLayout? = null
    private var mContentHeader: TableLayout? = null
    private var mVerticalScrollView: ScrollView? = null
    private var mFrozenColumns: TableLayout? = null
    private var mContentTableScrollView: ObservableHorizontalScrollView? = null
    private var mContentTable: TableLayout? = null

    private val mFont = Typeface.create("monospace", Typeface.NORMAL)
    var fontSize = 13f
    private var mScale = 1.25.toFloat()
    private var mContext: Context? = null
    private var mFrozenTableHeaderRow: TableRow? = null
    private var mContentTableHeaderRow: TableRow? = null
    private var mCellBackground: Drawable? = null
    private var mHeaderCellBackground: Drawable? = null
    private var mFrozenColumnBackground: Drawable? = null
    private var mOnCellEventListener: OnCellEventListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {

        mContext = context

        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.widget_table_grid, this, true)

        // get view references
        mContentHeaderScrollView = findViewById<View>(R.id.ohsv_content_header) as ObservableHorizontalScrollView
        mFrozenHeader = findViewById<View>(R.id.tl_frozen_header) as TableLayout
        mContentHeader = findViewById<View>(R.id.tl_content_header) as TableLayout
        mVerticalScrollView = findViewById<View>(R.id.sv_vertical) as ScrollView
        mFrozenColumns = findViewById<View>(R.id.tl_frozen_columns) as TableLayout
        mContentTableScrollView = findViewById<View>(R.id.ohsv_content_table) as ObservableHorizontalScrollView
        mContentTable = findViewById<View>(R.id.tl_content_table) as TableLayout

        // keep scroll views in sync
        mContentHeaderScrollView!!.setScrollViewListener(this)
        mContentTableScrollView!!.setScrollViewListener(this)

        // only show the scroll bar on the header table
        mContentTableScrollView!!.isHorizontalScrollBarEnabled = false

        // set default cell backgrounds
        mCellBackground = resources.getDrawable(
            R.drawable.widget_tg_content_cell)
        mHeaderCellBackground = resources.getDrawable(
            R.drawable.widget_tg_header_cell)
        mFrozenColumnBackground = resources.getDrawable(
            R.drawable.widget_tg_frozen_column_cell)
    }

    /**
     * Set the density (scale) to appropriately scale font and other scale
     * dependent elements
     *
     * @param scale
     */
    fun setScale(scale: Float) {
        mScale = scale
    }

    fun setHeaderCellBackground(drawable: Drawable) {
        mHeaderCellBackground = drawable
    }

    fun setCellBackground(drawable: Drawable) {
        mCellBackground = drawable
    }

    fun setFrozenColumnBackground(drawable: Drawable) {
        mFrozenColumnBackground = drawable
    }

    fun setOnCellEventListener(listener: OnCellEventListener) {
        mOnCellEventListener = listener
    }

    /**
     * Draw given data onto table Note: We prevent storing the data to keep
     * memory use at a minimum since some datasets can be huge!
     *
     * @param data
     */
    @Throws(RuntimeException::class, OutOfMemoryError::class)
    fun draw(data: List<List<String>>) {
        val tableRowParams = TableLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        var maxFrozenChars = 0
        val maxContentChars = IntArray(data[0].size - 1)

        EzLogger.d("Looping through content...")
        for (i in data.indices) {

            val frozenColumnRow = TableRow(mContext)
            frozenColumnRow.layoutParams = tableRowParams
            val frozenColumnCell = TextView(mContext)

            // FIXME: allow for multi-line cells
            // frozen column should match content cell height
            frozenColumnCell.setSingleLine(true)
            frozenColumnCell.text = data[i][0]
            frozenColumnCell.setTextColor(Color.parseColor("#000000"))
            frozenColumnCell
                .setBackgroundResource(R.drawable.widget_tg_frozen_column_cell)
            if (0 == i) {
                frozenColumnCell.setTypeface(mFont, Typeface.BOLD)
                frozenColumnCell.setOnClickListener { v ->
                    if (mOnCellEventListener != null) {
                        mOnCellEventListener!!.onCellClick(v, true, true)
                    }
                }
                frozenColumnCell
                    .setOnLongClickListener { v ->
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener!!.onCellLongClick(v,
                                true, true)
                            true
                        } else {
                            false
                        }
                    }
            } else {
                frozenColumnCell.setTypeface(mFont, Typeface.NORMAL)
                frozenColumnCell.setOnClickListener { v ->
                    if (mOnCellEventListener != null) {
                        mOnCellEventListener!!.onCellClick(v, false, true)
                    }
                }
                frozenColumnCell
                    .setOnLongClickListener { v ->
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener!!.onCellLongClick(v,
                                false, true)
                            true
                        } else {
                            false
                        }
                    }
            }

            frozenColumnCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                fontSize)
            frozenColumnRow.addView(frozenColumnCell)

            if (data[i].size < 1) {
                continue
            }

            if (data[i][0].length > maxFrozenChars) {
                maxFrozenChars = data[i][0].length
            }

            // The rest of them
            val row = TableRow(mContext)
            row.layoutParams = tableRowParams
            for (j in 1 until data[0].size) {
                val rowCell = TextView(mContext)
                rowCell.setSingleLine(true)
                rowCell.text = data[i][j]
                rowCell.gravity = Gravity.LEFT
                rowCell.setTextColor(Color.parseColor("#000000"))
                if (0 == i) {
                    rowCell.setTypeface(mFont, Typeface.BOLD)
                    rowCell.setBackgroundResource(R.drawable.widget_tg_header_cell)
                    rowCell.setOnClickListener { v ->
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener!!
                                .onCellClick(v, true, false)
                        }
                    }
                    rowCell.setOnLongClickListener { v ->
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener!!.onCellLongClick(v, true,
                                false)

                            true
                        } else {
                            false
                        }
                    }
                } else {
                    rowCell.setTypeface(mFont, Typeface.NORMAL)
                    rowCell.setBackgroundResource(R.drawable.widget_tg_content_cell)
                    rowCell.setOnClickListener { v ->
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener!!.onCellClick(v, false,
                                false)
                        }
                    }
                    rowCell.setOnLongClickListener { v ->
                        if (mOnCellEventListener != null) {
                            mOnCellEventListener!!.onCellLongClick(v, false,
                                false)

                            true
                        } else {
                            false
                        }
                    }
                }
                rowCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
                rowCell.setLines(1)
                row.addView(rowCell)
                if (data[i][j].length > maxContentChars[j - 1]) {
                    maxContentChars[j - 1] = data[i][j].length
                }
            }

            if (i == 0) {
                mFrozenTableHeaderRow = frozenColumnRow
                mContentTableHeaderRow = row
                mFrozenHeader!!.addView(frozenColumnRow)
                mContentHeader!!.addView(row)
            } else {
                mFrozenColumns!!.addView(frozenColumnRow)
                mContentTable!!.addView(row)
            }
        }

        EzLogger.d("Setting child text view widths...")
        setCellWidths(mFrozenTableHeaderRow, intArrayOf(maxFrozenChars))
        setCellWidths(mContentTableHeaderRow, maxContentChars)
        for (i in 0 until mContentTable!!.childCount) {
            val frozenRow = mFrozenColumns!!.getChildAt(i) as TableRow
            setCellWidths(frozenRow, intArrayOf(maxFrozenChars))

            val row = mContentTable!!.getChildAt(i) as TableRow
            setCellWidths(row, maxContentChars)
        }
    }

    /**
     * Removes all data from current row
     */
    fun clear() {
        if (mFrozenHeader != null) {
            mFrozenHeader!!.removeAllViews()
        }

        if (mFrozenTableHeaderRow != null) {
            mFrozenTableHeaderRow!!.removeAllViews()
        }

        if (mFrozenColumns != null) {
            mFrozenColumns!!.removeAllViews()
        }

        if (mContentTableHeaderRow != null) {
            mContentTableHeaderRow!!.removeAllViews()
        }

        if (mContentTable != null) {
            mContentTable!!.removeAllViews()
        }
    }

    /**
     * Dynamically set the widths of each cell
     *
     * @param row
     * @param widths
     */
    private fun setCellWidths(row: TableRow?, widths: IntArray) {
        if (row == null) {
            return
        }

        val widthFactor = Math.ceil(fontSize.toDouble() * mScale.toDouble()
            * if (fontSize < 10) 0.9 else 0.7).toInt()

        val padding = 10 // account for padding
        for (i in 0 until row.childCount) {

            // get cell/textviews
            val cell = row.getChildAt(i) as TextView

            var newWidth = 0
            if (widths[i] == 1) {
                newWidth = Math.ceil((widths[i] * widthFactor * 2).toDouble()).toInt()
            } else if (widths[i] < 3) {
                newWidth = Math.ceil(widths[i].toDouble() * widthFactor.toDouble() * 1.7).toInt()
            } else if (widths[i] < 5) {
                newWidth = Math.ceil(widths[i].toDouble() * widthFactor.toDouble() * 1.2).toInt()
            } else {
                newWidth = widths[i] * widthFactor
            }

            cell.minimumWidth = newWidth + padding
            cell.maxWidth = newWidth + padding
        }
    }

    /**
     * Resize all cells in frozen (first) column
     *
     * @param width
     * @return original width before resize
     */
    fun resizeFrozenColumn(width: Int): Int {

        val headerCell = mFrozenTableHeaderRow!!.getChildAt(0) as TextView

        val originalWidth = headerCell.width
        headerCell.width = width
        headerCell.maxWidth = width
        headerCell.minimumWidth = width

        for (i in 0 until mFrozenColumns!!.childCount) {
            val frozenRow = mFrozenColumns!!.getChildAt(i) as TableRow
            for (j in 0 until frozenRow.childCount) {
                val cell = frozenRow.getChildAt(j) as TextView
                cell.width = width
                cell.maxWidth = width
                cell.minimumWidth = width
            }
        }

        return originalWidth
    }

    /**
     * Keep scroll views in sync
     */
    override fun onScrollChanged(scrollView: ObservableHorizontalScrollView,
                                 x: Int, y: Int, oldX: Int, oldY: Int) {
        if (scrollView === mContentHeaderScrollView) {
            mContentTableScrollView!!.scrollTo(x, y)
        } else if (scrollView === mContentTableScrollView) {
            mContentHeaderScrollView!!.scrollTo(x, y)
        }
    }

    interface OnCellEventListener {
        /**
         * Called when a cell (TextView) is clicked
         *
         * @param cell
         * @param isHeader
         * @param isFrozenColumn
         */
        fun onCellClick(cell: View, isHeader: Boolean, isFrozenColumn: Boolean)

        /**
         * Called when a cell (TextView) is long clicked
         *
         * @param cell
         * @param isHeader
         * @param isFrozenColumn
         */
        fun onCellLongClick(cell: View, isHeader: Boolean, isFrozenColumn: Boolean)
    }
}
