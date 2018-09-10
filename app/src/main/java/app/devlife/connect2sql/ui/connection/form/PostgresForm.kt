package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Switch

import com.mobsandgeeks.saripaar.Rule

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import com.gitlab.connect2sql.R
import app.devlife.connect2sql.sql.DriverType
import com.mobsandgeeks.saripaar.Validator

class PostgresForm(context: Context, view: View) : BaseForm(context, view) {

    private val mUseSslSwitch: Switch
    private val mTrustCertSwitch: Switch

    init {
        mUseSslSwitch = view.findViewById(R.id.form_swtch_use_ssl) as Switch
        mTrustCertSwitch = view.findViewById(R.id.form_swtch_trust_cert) as Switch
    }

    override fun compileConnectionInfo(): ConnectionInfo {
        val connectionInfo = super.compileConnectionInfo()

        val options = connectionInfo.options + hashMapOf(
                Pair(ConnectionInfo.OPTION_USE_SSL, mUseSslSwitch.isChecked.toString()),
                Pair(ConnectionInfo.OPTION_TRUST_CERT, mTrustCertSwitch.isChecked.toString())
        )

        return connectionInfo.copy(driverType = DriverType.POSTGRES, options = options)
    }

    override fun populate(connectionInfo: ConnectionInfo) {
        super.populate(connectionInfo)

        mUseSslSwitch.isChecked = connectionInfo.options.get(ConnectionInfo.OPTION_USE_SSL)?.toBoolean() ?: false
        mTrustCertSwitch.isChecked = connectionInfo.options.get(ConnectionInfo.OPTION_TRUST_CERT)?.toBoolean() ?: false
    }

    override fun getHelpMessageResource(view: View): Int {
        return when (view.id) {
            R.id.form_swtch_use_ssl -> R.string.help_use_ssl
            R.id.form_swtch_trust_cert -> R.string.help_trust_cert
            else -> super.getHelpMessageResource(view)
        }
    }

    override fun onPreValidate(validator: Validator) {
        super.onPreValidate(validator)

        val errorMessage = context.resources.getString(R.string.form_error_database_required)
        validator.put(databaseEditTextView, object : Rule<EditText>(errorMessage) {
            override fun isValid(databaseEditTextView: EditText): Boolean {
                return !TextUtils.isEmpty(databaseEditTextView.text)
            }
        })
    }
}