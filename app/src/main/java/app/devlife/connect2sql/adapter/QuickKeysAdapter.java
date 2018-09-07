package app.devlife.connect2sql.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.gitlab.connect2sql.R;

public class QuickKeysAdapter extends BaseExpandableListAdapter {

    public static final int SECTION_SNIPPETS = 0;
    public static final int SECTION_OPERATORS = 1;
    public static final int SECTION_DATABASES = 2;
    public static final int SECTION_TABLES = 3;
    public static final int SECTION_COLUMNS = 4;

    private List<List<String>> mData = new ArrayList<List<String>>();
    private Context mContext;

    private int mListItemLayout = android.R.layout.simple_expandable_list_item_1;

    public int getListItemLayout() {
        return mListItemLayout;
    }

    public void setListItemLayout(int mListItemLayout) {
        this.mListItemLayout = mListItemLayout;
    }

    public QuickKeysAdapter(Context context) {
        mContext = context;

        for (int i = 0; i < 5; i++) {
            mData.add(new ArrayList<String>());
        }
    }

    public void clearSection(int section) {
        mData.get(section).clear();
    }

    public void addChild(String value, int section) {
        mData.get(section).add(value);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return ((List<?>) getGroup(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            // if no view present build it
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mListItemLayout, null);

            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        TextView textView = (TextView) convertView
                .findViewById(android.R.id.text1);

        //textView.setPadding(10, 30, 10, 30);

        textView.setText(getChild(groupPosition, childPosition).toString());

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ((List<?>) getGroup(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        if (convertView == null) {
            // if no view present build it
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mListItemLayout, null);
        }

        TextView textView = (TextView) convertView
                .findViewById(android.R.id.text1);

        //textView.setPadding(10, 30, 10, 30);

        switch (groupPosition) {
        case SECTION_COLUMNS:
            textView.setText(R.string.qk_section_column);
            break;
        case SECTION_DATABASES:
            textView.setText(R.string.qk_section_databases);
            break;
        case SECTION_TABLES:
            textView.setText(R.string.qk_section_tables);
            break;
        case SECTION_SNIPPETS:
            textView.setText(R.string.qk_section_snippets);
            break;
        case SECTION_OPERATORS:
            textView.setText(R.string.qk_section_operators);
            break;
        default:
            textView.setText("");
            break;
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
