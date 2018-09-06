package app.devlife.connect2sql.ui.results

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.ClipboardManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import app.devlife.connect2sql.fragment.BaseFragment
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.ui.widget.TableGrid
import app.devlife.connect2sql.ui.widget.TableGrid.OnCellEventListener
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.util.rx.ActivityAwareSubscriber
import com.gitlab.connect2sql.R
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.sql.ResultSet

class ResultsTableFragment : BaseFragment(), OnCellEventListener, android.view.View.OnClickListener {

    private var resultsTable: TableGrid? = null
    private val data = mutableListOf<List<String>>()
    private var normalFrozenColumnWidth = 0
    private var pagingTextView: TextView? = null
    private var resultSet: ResultSet? = null

    private var pagingPrevButton: ImageButton? = null
    private var pagingNextButton: ImageButton? = null

    private var startIndex = 0
    private var totalRows = 0

    private val previousStartIndex: Int
        get() {
            var previousStartIndex = startIndex - DISPLAY_LIMIT
            if (previousStartIndex < 0) {
                previousStartIndex = 0
            }

            return previousStartIndex
        }

    private val nextStartIndex: Int
        get() = startIndex + DISPLAY_LIMIT

    private lateinit var driverAgent: DriverAgent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        EzLogger.d("Creating fragment...")
        if (container == null) {
            return null
        }

        EzLogger.d("Setting main content view...")
        val v = inflater.inflate(R.layout.fragment_results_table, container, false)

        resultsTable = v.findViewById<View>(R.id.tg_results) as TableGrid

        val scale = activity!!.resources.displayMetrics.density
        EzLogger.i("Scale: $scale")
        resultsTable!!.setScale(scale)
        resultsTable!!.setOnCellEventListener(this)

        val paginationBar = v.findViewById<View>(R.id.pagination_bar) as LinearLayout
        pagingTextView = paginationBar.findViewById<View>(R.id.text1) as TextView

        pagingPrevButton = paginationBar.findViewById<View>(R.id.button1) as ImageButton
        pagingNextButton = paginationBar.findViewById<View>(R.id.button2) as ImageButton

        pagingPrevButton!!.setOnClickListener(this)
        pagingNextButton!!.setOnClickListener(this)

        setPaginationText(0, 0, 0)
        return v
    }

    override fun onStart() {
        super.onStart()
        runResultExtraction()
    }

    override fun onCellClick(cell: View, isHeader: Boolean, isFrozenColumn: Boolean) {
        if (isFrozenColumn) {
            if (normalFrozenColumnWidth == 0) {
                EzLogger.d("Resizing frozen column...")
                normalFrozenColumnWidth = resultsTable!!.resizeFrozenColumn(100)
            } else {
                EzLogger.d("Resizing (to original) frozen column...")
                resultsTable!!.resizeFrozenColumn(normalFrozenColumnWidth)
                normalFrozenColumnWidth = 0
            }
        }
    }

    override fun onCellLongClick(cell: View, isHeader: Boolean, isFrozenColumn: Boolean) {
        val text = (cell as TextView).text.toString()

        val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.text = text

        Toast.makeText(activity, "'$text' has been copied to clipboard.", Toast.LENGTH_LONG).show()
    }

    private fun runResultExtraction() {
        with(resultSet) {
            when {
                this == null -> activity?.finish()
                else -> {
                    driverAgent
                        .extract(this, startIndex, DISPLAY_LIMIT)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ActivityAwareSubscriber(activity, object : Subscriber<DriverAgent.DisplayResults>() {
                            override fun onNext(results: DriverAgent.DisplayResults) {
                                Log.d(TAG, "columnsNames count: ${results.columnNames.size}")
                                Log.d(TAG, "data count: ${results.data.size}")
                                Log.d(TAG, "totalRecords: ${results.totalCount}")
                                this@ResultsTableFragment.data.clear()
                                this@ResultsTableFragment.data.add(0, results.columnNames)
                                this@ResultsTableFragment.data.addAll(results.data)
                                redrawTable()
                                totalRows = results.totalCount

                                updatePaging(startIndex, results.data.size, totalRows)
                            }

                            override fun onCompleted() {

                            }

                            override fun onError(e: Throwable) {
                                Log.e(TAG, "Could not extract results!", e)

                                with(activity) {
                                    AlertDialog.Builder(this!!)
                                        .setTitle("Error")
                                        .setMessage(e.message)
                                        .setPositiveButton("OK") { dialog, _ ->
                                            dialog.dismiss()
                                            this.finish()
                                        }
                                        .create()
                                        .show()
                                }
                            }
                        }))
                }
            }
        }
    }

    fun increaseFontSize(i: Int): Float {
        val fontSize = resultsTable!!.fontSize + i
        resultsTable!!.fontSize = fontSize
        return fontSize
    }

    fun decreaseFontSize(i: Int): Float {
        val fontSize = resultsTable!!.fontSize - i
        resultsTable!!.fontSize = fontSize
        return fontSize
    }

    fun redrawTable() {
        resultsTable!!.clear()
        resultsTable!!.draw(data)
    }


    protected fun setPaginationText(from: Int, to: Int, total: Int) {
        pagingTextView!!.text = getString(R.string.results_showing_records_to, from, to, total)
    }

    private fun updatePaging(startIndex: Int, displayedRows: Int, totalRows: Int) {
        val startRow = startIndex + 1
        val endRow = startIndex + displayedRows

        pagingPrevButton!!.isEnabled = startRow > 1
        pagingNextButton!!.isEnabled = endRow < totalRows

        setPaginationText(startRow, endRow, totalRows)
    }

    override fun onClick(button: View) {
        val id = button.id
        when (button.id) {
            R.id.button1 -> {
                // previous button
                startIndex = previousStartIndex
                resultsTable!!.clear()
                data.clear()
                runResultExtraction()
            }
            R.id.button2 -> {
                startIndex = nextStartIndex
                resultsTable!!.clear()
                data.clear()
                runResultExtraction()
            }
            else -> Log.wtf(TAG, "What does this button do? $id")
        }
    }

    companion object {

        private val TAG = ResultsTableFragment::class.java.simpleName

        fun newInstance(driverAgent: DriverAgent, rs: ResultSet, startIndex: Int): ResultsTableFragment {
            val frag = ResultsTableFragment()
            //FIXME: We need this to rely on a host instead otherwise it WILL crash on orientation change.
            frag.resultSet = rs
            frag.driverAgent = driverAgent
            frag.startIndex = startIndex
            return frag
        }

        private val DISPLAY_LIMIT = 30
    }
}