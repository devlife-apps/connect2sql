package app.devlife.connect2sql.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.gitlab.connect2sql.R;

import app.devlife.connect2sql.db.model.query.HistoryQuery;
import app.devlife.connect2sql.db.model.query.HistoryQuery;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class QueryHistoryAdapter extends BaseAdapter {
    private List<HistoryQuery> queryList;

    private LayoutInflater mInflater;

    public QueryHistoryAdapter(Context context, List<HistoryQuery> queryList) {
        this.queryList = queryList;
        notifyDataSetChanged();
        mInflater = LayoutInflater.from(context);
    }

    public void clear() {
        queryList.clear();
        notifyDataSetChanged();
    }

    public void add(HistoryQuery query) {
        queryList.add(query);
        notifyDataSetChanged();
    }

    public void removeAt(int index) {
        queryList.remove(index);
        notifyDataSetChanged();
    }

    public List<HistoryQuery> getQueries() {
        return queryList;
    }

    @Override
    public int getCount() {
        return queryList.size();
    }

    @Override
    public Object getItem(int position) {
        return queryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_simple_right_label, null);
            holder = new ViewHolder();
            holder.queryText = (TextView) convertView.findViewById(R.id.text1);
            holder.dateTime = (TextView) convertView.findViewById(R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HistoryQuery query = queryList.get(position);
        String queryText = query.getQuery();
        Date dateTime = new Date(query.getDateTime() * 1000);

        holder.queryText.setText(queryText);
        holder.dateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(dateTime));
        return convertView;
    }

    private static class ViewHolder {
        TextView queryText;
        TextView dateTime;
    }

}
