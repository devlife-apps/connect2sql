package me.jromero.connect2sql.sql;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private String mName = "";

    private List<Table> mTables = new ArrayList<Table>();

    public Database(String name) {
        mName = name;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public List<Table> getTables() {
        return mTables;
    }

    public void setTables(List<Table> mTables) {
        this.mTables = mTables;
    }
}
