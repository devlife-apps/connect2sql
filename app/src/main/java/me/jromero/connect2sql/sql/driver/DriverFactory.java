package me.jromero.connect2sql.sql.driver;

import me.jromero.connect2sql.sql.DriverType;

@Deprecated
public class DriverFactory {
    public static BaseDriver newDriverInstance(DriverType driver) throws DriverNotFoundException {
        if (driver.equals(DriverType.MYSQL)) {
            return new MySQLDriver();
        } else if (driver.equals(DriverType.MSSQL)) {
            return new MsSQLDriver();
        } else if (driver.equals(DriverType.SYBASE)) {
            return new SybaseDriver();
        } else if (driver.equals(DriverType.POSTGRES)) {
            return new PostgreSQLDriver();
        } else {
            throw new DriverNotFoundException("Driver (" + driver + ") undefined!");
        }
    }

    public static class DriverNotFoundException extends Exception {

        private static final long serialVersionUID = 6418061287426150525L;

        public DriverNotFoundException(String string) {
            super(string);
        }
    }
}
