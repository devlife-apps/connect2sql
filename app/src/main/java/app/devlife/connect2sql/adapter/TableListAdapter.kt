package app.devlife.connect2sql.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import app.devlife.connect2sql.sql.Table
import com.gitlab.connect2sql.R

class TableListAdapter(private val context: Context, private val tableList: MutableList<Table>) : BaseAdapter() {

    private val mInflater: LayoutInflater

    val tables: List<Table>
        get() = tableList

    init {
        notifyDataSetChanged()
        mInflater = LayoutInflater.from(context)
    }

    fun clear() {
        tableList.clear()
        notifyDataSetChanged()
    }

    fun add(table: Table) {
        tableList.add(table)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return tableList.size
    }

    override fun getItem(position: Int): Any {
        return tableList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: mInflater.inflate(R.layout.item_table, parent).apply {
            this.tag = ViewHolder(
                this.findViewById(R.id.imageView1),
                this.findViewById(R.id.text1),
                this.findViewById(R.id.text2)
            )
        }

        val holder: ViewHolder = view.tag as ViewHolder

        with(tableList[position]) {
            holder.txtName.text = this.name
            when (this.type) {
                Table.TYPE_TABLE -> {
                    holder.image.setImageResource(R.drawable.icon_table)
                    holder.txtType.text = context.getString(R.string.table_list_type_table)
                }
                Table.TYPE_VIEW -> {
                    holder.image.setImageResource(R.drawable.icon_view)
                    holder.txtType.text = context.getString(R.string.table_list_type_view)
                }
            }
        }

        return view
    }

    private data class ViewHolder(
        val image: ImageView,
        val txtName: TextView,
        val txtType: TextView
    )
}
