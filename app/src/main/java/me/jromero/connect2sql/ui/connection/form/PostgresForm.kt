package me.jromero.connect2sql.ui.connection.form

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView

import com.mobsandgeeks.saripaar.Rule

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import com.gitlab.connect2sql.R
import me.jromero.connect2sql.sql.DriverType

/**
 * Created by javier.romero on 5/3/14.
 */
class PostgresForm(context: Context, view: View) : BaseForm(context, view) {

    private val mUseSslSwitch: Switch
    private val mTrustCertSwitch: Switch

    init {

        mUseSslSwitch = view.findViewById(R.id.form_swtch_use_ssl) as Switch
        mTrustCertSwitch = view.findViewById(R.id.form_swtch_trust_cert) as Switch

        // add rule to make database required
        val errorMessage = context.resources.getString(R.string.form_error_database_required)
        validator.put(databaseEditTextView, object : Rule<EditText>(errorMessage) {
            override fun isValid(databaseEditTextView: EditText): Boolean {
                if (TextUtils.isEmpty(databaseEditTextView.text)) {
                    return false
                } else {
                    return true
                }
            }
        })
    }

    override fun generateConnectionInfo(): ConnectionInfo {
        val connectionInfo = super.generateConnectionInfo()

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
        when (view.id) {
            R.id.form_swtch_use_ssl -> return R.string.help_use_ssl
            R.id.form_swtch_trust_cert -> return R.string.help_trust_cert
            else -> return super.getHelpMessageResource(view)
        }
    }
}