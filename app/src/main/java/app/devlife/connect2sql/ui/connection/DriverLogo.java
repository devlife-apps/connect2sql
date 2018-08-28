package app.devlife.connect2sql.ui.connection;

import com.gitlab.connect2sql.R;
import app.devlife.connect2sql.sql.DriverType;

/**
 * Created by javier.romero on 5/6/14.
 */
public enum DriverLogo {
    MYSQL(DriverType.MYSQL, R.drawable.db_mysql),
    MSSQL(DriverType.MSSQL, R.drawable.db_mssql),
    POSTGRES(DriverType.POSTGRES, R.drawable.db_postgre),
    SYBASE(DriverType.SYBASE, R.drawable.db_sybase);

    private final DriverType mDriverType;
    private final int mResource;

    DriverLogo(DriverType driverType, int resource) {

        mDriverType = driverType;
        mResource = resource;
    }

    public DriverType getDriverType() {
        return mDriverType;
    }

    public int getResource() {
        return mResource;
    }

    public static DriverLogo fromDriverType(DriverType driverType) {
        DriverLogo[] driverLogos = values();
        for (int i = 0; i < driverLogos.length; i++) {
            DriverLogo driverLogo = driverLogos[i];
            if (driverLogo.mDriverType.equals(driverType)) {
                return driverLogo;
            }
        }

        throw new IllegalArgumentException("No mapping for driver: " + driverType);
    }
}
