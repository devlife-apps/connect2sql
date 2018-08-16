package me.jromero.connect2sql.sql;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public static final int TYPE_TABLE = 1;
    public static final int TYPE_VIEW = 2;

    private String mName = "";
    private int mType;
    private List<Column> mColumns = new ArrayList<Column>();

    public Table(String name, int type) {
        mName = name;
        mType = type;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public List<Column> getColumns() {
        return mColumns;
    }

    public void setColumns(List<Column> mColumns) {
        this.mColumns = mColumns;
    }
}
