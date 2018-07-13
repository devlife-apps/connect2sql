package me.jromero.connect2sql.adapter;

import java.util.List;

import com.gitlab.connect2sql.R;
import me.jromero.connect2sql.sql.Table;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TableListAdapter extends BaseAdapter {
    private final List<Table> tableList;

    private final LayoutInflater mInflater;

    public TableListAdapter(Context context, List<Table> tableList) {
        this.tableList = tableList;
        notifyDataSetChanged();
        mInflater = LayoutInflater.from(context);
    }

    public void clear() {
        tableList.clear();
        notifyDataSetChanged();
    }

    public void add(Table table) {
        tableList.add(table);
        notifyDataSetChanged();
    }

    public List<Table> getTables() {
        return tableList;
    }

    @Override
    public int getCount() {
        return tableList.size();
    }

    @Override
    public Object getItem(int position) {
        return tableList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_table, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.imageView1);
            holder.txtName = (TextView) convertView.findViewById(R.id.text1);
            holder.txtType = (TextView) convertView.findViewById(R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Table table = tableList.get(position);
        String name = table.getName();
        String typeString = "";
        if (table.getType() == Table.TYPE_VIEW) {
            typeString = "View";
            holder.image.setImageResource(R.drawable.icon_view);
        } else {
            typeString = "Table";
            holder.image.setImageResource(R.drawable.icon_table);
        }

        holder.txtName.setText(name);
        holder.txtType.setText("(" + typeString + ")");
        return convertView;
    }

    private static class ViewHolder {
        ImageView image;
        TextView txtName;
        TextView txtType;
    }

}
