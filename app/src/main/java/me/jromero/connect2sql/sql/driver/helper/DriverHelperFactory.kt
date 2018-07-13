package me.jromero.connect2sql.sql.driver.helper

import me.jromero.connect2sql.sql.DriverType

/**

 */
object DriverHelperFactory {
    private val driverHelpers = hashMapOf<DriverType, DriverHelper>()

    init {
        driverHelpers.put(DriverType.MSSQL, MsSqlDriverHelper())
        driverHelpers.put(DriverType.MYSQL, MySqlDriverHelper())
        driverHelpers.put(DriverType.POSTGRES, PostgresDriverHelper())
        driverHelpers.put(DriverType.SYBASE, SybaseDriverHelper())
    }

    fun create(driverType: DriverType): DriverHelper? {
        return driverHelpers[driverType]
    }
}