package me.jromero.connect2sql.sql.driver;

import android.os.Build;

import java.sql.SQLException;
import java.sql.Statement;

import me.jromero.connect2sql.db.model.connection.ConnectionInfo;
import me.jromero.connect2sql.sql.driver.helper.DriverHelper;
import me.jromero.connect2sql.sql.driver.helper.MySqlDriverHelper;

@Deprecated
public class MySQLDriver extends BaseDriver {

    private static final DriverHelper sDriverHelper = new MySqlDriverHelper();

    public MySQLDriver() {
        setDriverPath(sDriverHelper.getDriverClass());
    }

    @Override
    public String getColumnsQuery(String table) {
        return sDriverHelper.getColumnsQuery(table);
    }

    @Override
    protected String getConnectionString(ConnectionInfo connectionInfo) {
        return sDriverHelper.getConnectionString(connectionInfo);
    }

    @Override
    public String getDatabasesQuery() {
        return sDriverHelper.getDatabasesQuery();
    }

    @Override
    public String getTablesQuery(String database) {
        return sDriverHelper.getTablesQuery(database);
    }

    @Override
    public String safeObject(String object) {
        return "`" + object + "`";
    }

    @Override
    public void useDatabase(String database) throws SQLException {
        Statement statement = getConnection().createStatement();
        if (statement.execute("USE " + safeObject(database) + ";")) {
            statement.close();
        }
    }

    @Override
    public int getTableNameIndex() {
        return 1;
    }

    @Override
    public int getTableTypeIndex() {
        return 2;
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