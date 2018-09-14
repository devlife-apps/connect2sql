package app.devlife.connect2sql.ui.connection.form.section

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import com.gitlab.connect2sql.R
import com.mobsandgeeks.saripaar.annotation.Required

class PostgresFormSection(
    private val context: Context,
    private val view: View,
    @Required(order = 9, messageResId = R.string.form_error_database_required)
    private val databaseEditTextView: EditText
) :
    FormSection {

    private val useSslSwitch: CompoundButton = view.findViewById(R.id.form_switch_use_ssl)
    private val trustCertSwitch: CompoundButton = view.findViewById(R.id.form_switch_trust_cert)

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
}