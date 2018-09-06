package app.devlife.connect2sql.sql.driver.helper

import app.devlife.connect2sql.sql.DriverType

/**

 */
object DriverHelperFactory {
    private val driverHelpers = hashMapOf<DriverType, DriverHelper>()

    init {
        driverHelpers[DriverType.MSSQL] = MsSqlDriverHelper()
        driverHelpers[DriverType.MYSQL] = MySqlDriverHelper()
        driverHelpers[DriverType.POSTGRES] = PostgresDriverHelper()
        driverHelpers[DriverType.SYBASE] = SybaseDriverHelper()
    }

    fun create(driverType: DriverType): DriverHelper? {
        return driverHelpers[driverType]
    }
}