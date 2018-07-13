package me.jromero.connect2sql.sql.driver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.jromero.connect2sql.db.model.connection.ConnectionInfo;
import me.jromero.connect2sql.log.EzLogger;
import me.jromero.connect2sql.sql.ConnectionDetails;

@Deprecated
public abstract class BaseDriver {

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;

    public static final String SQL_STATE_COMMUNICATION_LINK_FAILURE = "08S01";
    public static final String SQL_STATE_NO_OPERATION_AFTER_CLOSED = "08003";

    /**
     * Keeps track of details such as tables, columns, etc
     */
    private ConnectionDetails mConnectionDetails;

    /**
     * This is the main connection (stream)
     */
    protected Connection mConnection;

    /**
     * Current connection information
     */
    protected ConnectionInfo mConnectionInfo;

    /**
     * Current status of driver
     */
    public int status = 0;

    /**
     * The driver path as required by JDBC
     */
    private String mDriverPath;
    private OnDisconnectListener mOnDisconnectListener;

    /**
     * Default constructor
     *
     * @param path
     *            - Driver path for class include
     */
    public BaseDriver(String path) {
        mDriverPath = path;
    }

    protected BaseDriver() {

    }

    /**
     * Should only be called from the constructor
     * @param driverPath
     */
    protected void setDriverPath(String driverPath) {
        mDriverPath = driverPath;
    }

    public String getDriverPath() {
        return mDriverPath;
    }

    /**
     * Close current connection
     */
    public void close() {
        try {
            if (!mConnection.isClosed()) {
                try {
                    mConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    status = STATUS_IDLE;
                    mConnection = null;
                    if (mOnDisconnectListener != null) {
                        mOnDisconnectListener.onDisconnect(mConnectionInfo);
                    }
                    mConnectionInfo = null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiate connection to SQL server
     *
     * @return Connection
     * @throws SQLException
     */
    public Connection connect(ConnectionInfo connectionInfo)
            throws SQLException {

        /**
         * Check via Socket because setLoginTimeout is not working...
         */
        try {
            Socket s = new Socket();
            InetSocketAddress address = new InetSocketAddress(connectionInfo
                    .getHost().split("\\\\")[0], connectionInfo.getPort());
            s.connect(address, 10000);
            if (s.isConnected()) {
                s.close();
            }
        } catch (IllegalArgumentException e1) {
            throw new SQLException(e1.getMessage());
        } catch (IOException e) {
            throw new SQLException(e.getMessage());
        }

        try {
            // import database driver
            EzLogger.d("Importing database driver: " + mDriverPath);
            Class.forName(mDriverPath);

            // build our connection path
            String connectionPath = getConnectionString(connectionInfo);

            // connect
            EzLogger.d("Connecting to: " + connectionPath);
            status = STATUS_CONNECTING;
            DriverManager.setLoginTimeout(30);
            mConnection = DriverManager.getConnection(connectionPath,
                    connectionInfo.getUsername(), connectionInfo.getPassword());
            if (mConnection == null) {
                status = STATUS_IDLE;
            } else {
                status = STATUS_CONNECTED;

                // save our current connection data
                mConnectionInfo = connectionInfo;

                // create new connection detail
                mConnectionDetails = new ConnectionDetails();
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Class not found: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }

        return mConnection;
    }

    /**
     * Wrapper method for connection.createStatement
     *
     * @return {@link Statement}
     * @throws SQLException
     */
    public Statement createStatement() throws SQLException {
        Statement stmt = null;

        stmt = mConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        return stmt;
    }

    /**
     * Current connection stream
     *
     * @return
     */
    public Connection getConnection() {
        return mConnection;
    }

    /**
     * Get the current connection's information
     *
     * @return {@link Connection}
     */
    public ConnectionInfo getConnectionInfo() {
        return mConnectionInfo;
    }

    public ConnectionDetails getConnectionDetails() {
        return mConnectionDetails;
    }

    protected abstract String getConnectionString(ConnectionInfo connectionInfo);

    /**************************************************************************
     * Info queries
     */

    /**
     * Get a list of all databases available
     *
     * @return
     */
    public abstract String getDatabasesQuery();

    /**
     * Get a list of tables in the current database connected
     *
     * @return tables in current database
     */
    public abstract String getTablesQuery(String database);

    /**
     * Get a list of columns for the given table in the current connection
     *
     * @return columns
     */
    public abstract String getColumnsQuery(String table);

    /**************************************************************************
     * Status queries
     */

    /**************************************************************************
     * Indexes
     */

    /**
     * Get the index of where the table name is located in the results from
     * getTablesQuery();
     *
     * @deprecated use {@link me.jromero.connect2sql.sql.driver.helper.DriverHelper} instead
     * @return table column index
     */
    public abstract int getTableNameIndex();

    /**
     * Get the index of where the table type is located in the results from
     * getTablesQuery();
     *
     * @return table column index
     */
    public abstract int getTableTypeIndex();

    /**
     * Get the index of where the column name is located in the results from
     * getColumnsQuery();
     *
     * @return column index
     */
    public abstract int getColumnNameIndex();

    /**
     * Get the index of where the column type is located in the results from
     * getColumnsQuery();
     *
     * @return column index
     */
    public abstract int getColumnTypeIndex();

    /**
     * Check if connection is still open
     *
     * @return Boolean
     * @throws SQLException
     */
    public boolean isConnected() {
        if (mConnection != null) {
            try {
                if (!mConnection.isClosed()) {
                    Statement stmt = mConnection.createStatement();
                    stmt.execute("SELECT 1");

                    // FIXME: Fix in API level 9
                    //            } else if (!mConnection.isValid(10)) {
                    //                return false;
                    //            }

                    // by now it should be connected
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }

        return false;
    }

    public static interface OnDisconnectListener {
        void onDisconnect(ConnectionInfo connectionInfo);
    }

    public void setOnDisconnectListener(OnDisconnectListener listener) {
        mOnDisconnectListener = listener;
    }

    /**
     * Manipulate string to make it safe to insert into a query string
     *
     * @param object
     * @return
     */
    public abstract String safeObject(String object);

    /**
     * Change database
     *
     * @param database
     * @throws SQLException
     */
    public abstract void useDatabase(String database) throws SQLException;
}
