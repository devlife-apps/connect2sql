package app.devlife.connect2sql.ui.connection.form.section

import android.view.View
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.util.ext.intValue
import app.devlife.connect2sql.util.ext.nonBlankStringValue
import app.devlife.connect2sql.util.ext.stringValue
import com.gitlab.connect2sql.R
import com.mobsandgeeks.saripaar.annotation.NumberRule
import com.mobsandgeeks.saripaar.annotation.Required

class ConnectionFormSection(view: View) :
    FormSection {

    @Required(order = 2, messageResId = R.string.form_error_host_required)
    val hostEditText: EditText = view.findViewById(R.id.form_txt_host)
    @Required(order = 3, messageResId = R.string.form_error_port_required)
    @NumberRule(
        order = 4,
        messageResId = R.string.form_error_port_required,
        type = NumberRule.NumberType.INTEGER,
        gt = 0.0
    )
    val portEditText: EditText = view.findViewById(R.id.form_txt_port)
    @Required(order = 5, messageResId = R.string.form_error_username_required)
    val usernameEditText: EditText = view.findViewById(R.id.form_txt_username)
    val passwordEditText: EditText = view.findViewById(R.id.form_txt_password)
    val databaseEditTextView: EditText = view.findViewById(R.id.form_txt_database)

    override fun compileConnectionInfo(connectionInfo: ConnectionInfo): ConnectionInfo {
        return connectionInfo.copy(
            host = hostEditText.stringValue,
            port = portEditText.intValue,
            username = usernameEditText.stringValue,
            password = passwordEditText.stringValue,
            database = databaseEditTextView.nonBlankStringValue)
    }

    override fun populate(connectionInfo: ConnectionInfo) {
        hostEditText.setText(connectionInfo.host)
        portEditText.setText(connectionInfo.port.toString())
        usernameEditText.setText(connectionInfo.username)
        passwordEditText.setText(connectionInfo.password)
        databaseEditTextView.setText(connectionInfo.database)
    }

    override fun populate(driverDefaults: DriverDefaults) {
        portEditText.setText(driverDefaults.port.toString())
    }
}