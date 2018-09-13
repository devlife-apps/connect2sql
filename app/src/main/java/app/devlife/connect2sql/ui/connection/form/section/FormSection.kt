package app.devlife.connect2sql.ui.connection.form.section

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.driver.DriverDefaults
import com.mobsandgeeks.saripaar.Validator

interface FormSection {
    fun compileConnectionInfo(connectionInfo: ConnectionInfo): ConnectionInfo

    fun populate(connectionInfo: ConnectionInfo)

    fun populate(driverDefaults: DriverDefaults)

    fun onPreValidate(validator: Validator) {}
}