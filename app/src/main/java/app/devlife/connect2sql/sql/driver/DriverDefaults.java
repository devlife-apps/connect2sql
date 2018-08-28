package app.devlife.connect2sql.sql.driver;

import app.devlife.connect2sql.sql.DriverType;

/**
 * Created by javier.romero on 5/6/14.
 */
public enum DriverDefaults {
    MYSQL(DriverType.MYSQL, 3306),
    MSSQL(DriverType.MSSQL, 1433),
    SYBASE(DriverType.SYBASE, 5000),
    POSTGRES(DriverType.POSTGRES, 5432);
    private final DriverType mDriverType;
    private final int mPort;

    DriverDefaults(DriverType driverType, int port) {
        mDriverType = driverType;
        mPort = port;
    }

    public DriverType getDriverType() {
        return mDriverType;
    }

    public int getPort() {
        return mPort;
    }

    public static DriverDefaults fromDriver(DriverType driverType) {
        DriverDefaults[] driverDefaultses = values();
        for (int i = 0; i < driverDefaultses.length; i++) {
            DriverDefaults driverDefaults = driverDefaultses[i];
            if (driverDefaults.getDriverType().equals(driverType)) {
                return driverDefaults;
            }
        }

        throw new IllegalArgumentException("No defaults for driver: " + driverType);
    }
}
