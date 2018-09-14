package app.devlife.connect2sql.ui.connection.form.section

import android.view.View
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.util.ext.stringValue
import com.gitlab.connect2sql.R

class MsSqlFormSection(view: View) :
    FormSection {

    private val domainEditTextView: EditText = view.findViewById(R.id.form_txt_domain)
    private val instanceEditTextView: EditText = view.findViewById(R.id.form_txt_instance)

    override fun compileConnectionInfo(connectionInfo: ConnectionInfo): ConnectionInfo {
        return connectionInfo.copy(
            options = connectionInfo.options + hashMapOf(
                Pair(ConnectionInfo.OPTION_DOMAIN, domainEditTextView.stringValue),
                Pair(ConnectionInfo.OPTION_INSTANCE, instanceEditTextView.stringValue)
            )
        )
    }

    override fun populate(connectionInfo: ConnectionInfo) {
        domainEditTextView.setText(connectionInfo.options[ConnectionInfo.OPTION_DOMAIN])
        instanceEditTextView.setText(connectionInfo.options[ConnectionInfo.OPTION_INSTANCE])
    }

    override fun populate(driverDefaults: DriverDefaults) {
        // ignore
    }
}