package me.jromero.connect2sql.sql.driver;

import java.sql.SQLException;
import java.sql.Statement;

@Deprecated
public class SybaseDriver extends BaseJtdsDriver {

    /* (non-Javadoc)
     * @see me.jromero.connect2sql.sql.driver.BaseJtdsDriver#getServerType()
     */
    @Override
    public String getServerType() {
    	return "sybase";
    }

    @Override
    public String getColumnsQuery(String table) {
        return "SELECT sc.name, st.name AS usertypename, sc.* "
                + "FROM syscolumns sc "
                + "INNER JOIN sysobjects so ON sc.id = so.id "
                + "INNER JOIN systypes st ON sc.usertype = st.usertype "
                + "WHERE so.name = '" + table + "'";
    }

    @Override
    public String getDatabasesQuery() {
        return "SELECT name FROM master..sysdatabases ORDER BY name ASC";
    }

    @Override
    public String getTablesQuery(String database) {
        return "SELECT * FROM sysobjects WHERE type = 'U' ORDER BY name ASC";
    }

    @Override
    public String safeObject(String object) {
        return "[" + object + "]";
    }

    @Override
    public void useDatabase(String database) throws SQLException {
        Statement statement = getConnection().createStatement();
        if (statement.execute("USE " + database)) {
            statement.close();
        }
    }

    @Override
    public int getTableNameIndex() {
        return 1;
    }

    @Override
    public int getTableTypeIndex() {
        return 7;
    }

    @Override
    public int getColumnNameIndex() {
        return 1;
    }

    @Override
    public int getColumnTypeIndex() {
        return 2;
    }
}