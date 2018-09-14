package app.devlife.connect2sql.ui.hostkeys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.devlife.connect2sql.ApplicationUtils
import com.gitlab.connect2sql.R
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSch
import kotlinx.android.synthetic.main.activity_host_keys.recycler_view
import javax.inject.Inject

class HostKeysActivity : AppCompatActivity() {

    private val hostKeys = mutableListOf<HostKey>()
    private val selectedItems = mutableListOf<Int>()
    private var actionMode: ActionMode? = null

    @Inject
    lateinit var jSch: JSch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        setContentView(R.layout.activity_host_keys)

        hostKeys.addAll(jSch.hostKeyRepository.hostKey)

        recycler_view.adapter = Adapter()
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.host_keys, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.delete_all -> {
                hostKeys.removeAll { key ->
                    jSch.hostKeyRepository.remove(key.host, key.type)
                    true
                }

                recycler_view.adapter?.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val actionModeCallBack: ActionMode.Callback = object : ActionMode.Callback {
        override fun onActionItemClicked(actionMode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.delete -> {
                    selectedItems
                        .mapNotNull { hostKeys.getOrNull(it) }
                        .forEach {
                            jSch.hostKeyRepository.remove(it.host, it.type)
                            hostKeys.remove(it)
                        }

                    recycler_view.adapter?.notifyDataSetChanged()
                    actionMode?.finish()
                    return true
                }
            }

            return false
        }

        override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
            this@HostKeysActivity.actionMode = actionMode
            actionMode?.menuInflater?.inflate(R.menu.host_keys_selected, menu)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(actionMode: ActionMode?) {
            selectedItems.removeAll { true }
            recycler_view.adapter?.notifyDataSetChanged()
            this@HostKeysActivity.actionMode = null
        }
    }

    private fun toggleSelection(viewHolder: ViewHolder, index: Int) {
        when (viewHolder.root.isActivated) {
            true -> {
                selectedItems.remove(index)
                viewHolder.selected = false

                if (selectedItems.isEmpty()) actionMode?.finish()
            }
            false -> {
                selectedItems.add(index)
                viewHolder.selected = true

                if (actionMode == null) startSupportActionMode(actionModeCallBack)
            }
        }
    }

    private inner class Adapter :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onBindViewHolder(viewHolder: ViewHolder, index: Int) {
            val hostKey = hostKeys[index]

            viewHolder.selected = selectedItems.contains(index)
            viewHolder.root.setOnLongClickListener {
                toggleSelection(viewHolder, index)
                true
            }
            viewHolder.root.setOnClickListener {
                if (actionMode != null) toggleSelection(viewHolder, index)
            }

            viewHolder.iconTextView.text = hostKey.host.take(1)
            viewHolder.iconTextView.setOnClickListener {
                toggleSelection(viewHolder, index)
            }
            viewHolder.hostTextView.text = hostKey.host
            viewHolder.fingerprintTextView.text = hostKey.getFingerPrint(jSch)
        }

        override fun getItemCount(): Int {
            return hostKeys.size
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater
                    .from(viewGroup.context)
                    .inflate(R.layout.item_host_key,
                        viewGroup,
                        false)
            )
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView
        val iconTextView: TextView = itemView.findViewById(R.id.item_icon)
        val hostTextView: TextView = itemView.findViewById(R.id.item_host)
        val fingerprintTextView: TextView = itemView.findViewById(R.id.item_fingerprint)

        var selected: Boolean
            get() = root.isActivated
            set(value) {
                root.isActivated = value
                root.setBackgroundResource(if (value) R.color.greyDarker else 0)
            }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HostKeysActivity::class.java)
        }
    }
}
