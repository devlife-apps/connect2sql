package me.jromero.connect2sql.sql.driver;

import java.sql.SQLException;
import java.sql.Statement;

@Deprecated
public class MsSQLDriver extends BaseJtdsDriver {

	/* (non-Javadoc)
	 * @see me.jromero.connect2sql.sql.driver.BaseJtdsDriver#getServerType()
	 */
	@Override
	public String getServerType() {
		return "sqlserver";
	}

    @Override
    public String getColumnsQuery(String table) {
        return "EXEC sp_columns @table_name = " + safeObject(table);
    }

    @Override
    public String getDatabasesQuery() {
        return "EXEC sp_databases;";
    }

    @Override
    public String getTablesQuery(String database) {
        return "EXEC sp_tables @table_qualifier = " + safeObject(database);
    }

    @Override
    public String safeObject(String object) {
        return "[" + object + "]";
    }

    @Override
    public void useDatabase(String database) throws SQLException {
        Statement statement = getConnection().createStatement();
        if (statement.execute("USE " + safeObject(database))) {
            statement.close();
        }
    }

    @Override
    public int getTableNameIndex() {
        return 3;
    }

    @Override
    public int getTableTypeIndex() {
        return 4;
    }

    @Override
    public int getColumnNameIndex() {
        return 4;
    }

    @Override
    public int getColumnTypeIndex() {
        return 6;
    }
}