package app.devlife.connect2sql.ui.quickkeys

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.fragment.BaseFragment
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.util.ext.disableParentInterception
import app.devlife.connect2sql.viewmodel.ConnectionViewModel
import app.devlife.connect2sql.viewmodel.ViewModelFactory
import com.gitlab.connect2sql.R
import kotlinx.android.synthetic.main.fragment_quick_keys.qk_btn_clear
import kotlinx.android.synthetic.main.fragment_quick_keys.recycler_view
import javax.inject.Inject


class QuickKeysFragment : BaseFragment() {

    private lateinit var quickKeysAdapter: QuickKeysAdapter

    private val columnQuickKeys = mutableListOf<QuickKeysAdapter.QuickKey.OfTypeSystemObject>()
    private val tableQuickKeys = mutableListOf<QuickKeysAdapter.QuickKey.OfTypeSystemObject>()
    private val databaseQuickKeys = mutableListOf<QuickKeysAdapter.QuickKey.OfTypeSystemObject>()

    private val connectionViewModel: ConnectionViewModel by lazy {
        ViewModelProviders.of(activity!!, viewModelFactory).get(ConnectionViewModel::class.java)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    var onQuickKeyClickListener: (QuickKeysAdapter.QuickKey<*>) -> Unit = {}
    var onClearClickListener: () -> Unit = {}

    override fun onAttach(context: Context) {
        super.onAttach(context)

        ApplicationUtils.getApplication(context).applicationComponent.inject(this)

        quickKeysAdapter = QuickKeysAdapter(
            context,
            mutableListOf(
                QuickKeysAdapter.Section(
                    getString(R.string.qk_section_snippets),
                    resources.getStringArray(R.array.snippets)
                        .map { QuickKeysAdapter.QuickKey.OfTypeString(it, it) }
                        .toMutableList()
                ),
                QuickKeysAdapter.Section(
                    getString(R.string.qk_section_operators),
                    resources.getStringArray(R.array.operators)
                        .map { QuickKeysAdapter.QuickKey.OfTypeString(it, it) }
                        .toMutableList()
                ),
                QuickKeysAdapter.Section(
                    getString(R.string.qk_section_column),
                    columnQuickKeys
                ),
                QuickKeysAdapter.Section(
                    getString(R.string.qk_section_tables),
                    tableQuickKeys
                ),
                QuickKeysAdapter.Section(
                    getString(R.string.qk_section_databases),
                    databaseQuickKeys
                )
            )
        )

        activity?.also { fragmentActivity ->
            EzLogger.i("Observing connectionViewModel...")

            connectionViewModel.apply {
                tables.observe(fragmentActivity, Observer { tables ->
                    EzLogger.i("connectionViewModel:tables...")
                    tableQuickKeys.clear()
                    tables?.map { table ->
                        tableQuickKeys.add(QuickKeysAdapter.QuickKey.OfTypeSystemObject(
                            table.name,
                            table))
                    }
                    quickKeysAdapter.notifyDataSetChanged()
                })

                databases.observe(fragmentActivity, Observer { databases ->
                    EzLogger.i("connectionViewModel:databases...")
                    databaseQuickKeys.clear()
                    databases?.map { database ->
                        databaseQuickKeys.add(QuickKeysAdapter.QuickKey.OfTypeSystemObject(
                            database.name,
                            database
                        ))
                    }
                    quickKeysAdapter.notifyDataSetChanged()
                })
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_quick_keys, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        qk_btn_clear.setOnClickListener { onClearClickListener.invoke() }
        recycler_view.disableParentInterception()
        recycler_view.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler_view.adapter = quickKeysAdapter.apply {
            onQuickKeyClickedListener = this@QuickKeysFragment.onQuickKeyClickListener
        }
    }

    companion object {
        private const val EXTRA_CONNECTION_INFO_ID = "EXTRA_CONNECTION_INFO_ID"

        fun newInstance(connectionInfoId: Long): QuickKeysFragment {
            return QuickKeysFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_CONNECTION_INFO_ID, connectionInfoId)
                }
            }
        }
    }
}