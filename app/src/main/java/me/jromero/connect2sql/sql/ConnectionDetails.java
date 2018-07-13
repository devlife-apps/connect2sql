package me.jromero.connect2sql.sql;

import java.util.ArrayList;
import java.util.List;

import me.jromero.connect2sql.log.EzLogger;

public class ConnectionDetails {

    private Database mCurrentDatabase;
    private Table mCurrentTable;

    private List<Database> mDatabases = new ArrayList<Database>();

    public ConnectionDetails() {
    }

    public List<Database> getDatabases() {
        return mDatabases;
    }

    public void setDatabases(List<Database> databases) {
        mDatabases.clear();
        mDatabases.addAll(databases);
    }

    public Database getCurrentDatabase() {
        return mCurrentDatabase;
    }

    public void setCurrentDatabase(Database database) throws Exception {
        if (!mDatabases.contains(database)) {
            throw new Exception("Database object is not in our known list of databases!");
        }

        mCurrentDatabase = database;

        // clear table selection
        mCurrentTable = null;
    }

    public Table getCurrentTable() {
        return mCurrentTable;
    }

    public void setCurrentTable(Table table) throws Exception {
        if (mCurrentDatabase == null) {
            throw new Exception("No database is currently selected.");
        }

        if (!mCurrentDatabase.getTables().contains(table)) {
            throw new Exception("Table " + table.getName() + " does not exist "
                    + "in current database: " + getCurrentDatabase().getName());
        }

        mCurrentTable = table;
    }

    public void setCurrentDatabaseByName(String databaseName) throws Exception {
        int i = 0;
        for (Database d : mDatabases) {
            EzLogger.i("setCurrentDatabaseByName: " + i + " of " + getDatabases().size());
            if (d.getName().equals(databaseName)) {
                setCurrentDatabase(d);
                break;
            }
            i++;
        }
    }
}
