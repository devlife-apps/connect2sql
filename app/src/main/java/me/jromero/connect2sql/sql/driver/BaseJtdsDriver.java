/**
 *
 */
package me.jromero.connect2sql.sql.driver;

import me.jromero.connect2sql.db.model.connection.ConnectionInfo;
import android.text.TextUtils;

/**
 * @author Javier Romero
 *
 */
@Deprecated
public abstract class BaseJtdsDriver extends BaseDriver {

	public BaseJtdsDriver() {
        super("net.sourceforge.jtds.jdbc.Driver");
	}

	public abstract String getServerType();

	@Override
    protected String getConnectionString(ConnectionInfo connectionInfo) {

        String connectionPath = "jdbc:jtds:" + getServerType() + "://" + connectionInfo.getHost();
        connectionPath += ":" + connectionInfo.getPort();

        String database = connectionInfo.getDatabase();
        if (!TextUtils.isEmpty(database)) {
        	connectionPath += "/" + database;
        }

    	connectionPath += ";";

        String instance = connectionInfo.getOptions().get(ConnectionInfo.OPTION_INSTANCE);
        if (!TextUtils.isEmpty(instance)) {
            connectionPath += "instance=" + instance + ";";
        }

        String domain = connectionInfo.getOptions().get(ConnectionInfo.OPTION_DOMAIN);
        if (!TextUtils.isEmpty(domain)) {
        	connectionPath += "domain=" + domain + ";";
        }

        return connectionPath;
    }
}
