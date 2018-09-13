package app.devlife.connect2sql.ui.connection.form.section

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Switch
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.driver.DriverDefaults
import com.gitlab.connect2sql.R
import com.mobsandgeeks.saripaar.Rule
import com.mobsandgeeks.saripaar.Validator

class PostgresFormSection(
    private val context: Context,
    private val view: View,
    private val databaseEditTextView: EditText
) :
    FormSection {

    private val useSslSwitch: Switch = view.findViewById(R.id.form_swtch_use_ssl)
    private val trustCertSwitch: Switch = view.findViewById(R.id.form_swtch_trust_cert)

    override fun compileConnectionInfo(connectionInfo: ConnectionInfo): ConnectionInfo {
        return connectionInfo.copy(
            options = connectionInfo.options + hashMapOf(
                Pair(ConnectionInfo.OPTION_USE_SSL, useSslSwitch.isChecked.toString()),
                Pair(ConnectionInfo.OPTION_TRUST_CERT, trustCertSwitch.isChecked.toString())
            )
        )
    }

    override fun populate(connectionInfo: ConnectionInfo) {
        useSslSwitch.isChecked = connectionInfo.options[ConnectionInfo.OPTION_USE_SSL]?.toBoolean() ?: false
        trustCertSwitch.isChecked = connectionInfo.options[ConnectionInfo.OPTION_TRUST_CERT]?.toBoolean() ?: false
    }

    override fun populate(driverDefaults: DriverDefaults) {
        // ignore
    }

    override fun onPreValidate(validator: Validator) {
        val errorMessage = context.resources.getString(R.string.form_error_database_required)
        validator.put(databaseEditTextView,
            object : Rule<EditText>(errorMessage) {
                override fun isValid(databaseEditTextView: EditText): Boolean {
                    return !TextUtils.isEmpty(databaseEditTextView.text)
                }
            })
    }
}