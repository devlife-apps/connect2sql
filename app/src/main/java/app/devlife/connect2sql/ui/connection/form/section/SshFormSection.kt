package app.devlife.connect2sql.ui.connection.form.section

import android.content.Context
import android.support.v7.widget.AppCompatSpinner
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.Address
import app.devlife.connect2sql.db.model.connection.BasicAuth
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.connection.None
import app.devlife.connect2sql.db.model.connection.PrivateKey
import app.devlife.connect2sql.db.model.connection.SshConfig
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.util.ext.nonBlankStringValue
import app.devlife.connect2sql.util.ext.setSelectionByResource
import app.devlife.connect2sql.util.ext.stringValue
import com.gitlab.connect2sql.R

class SshFormSection(private val context: Context, view: View) :
    FormSection {
    private val sshHostEditText: EditText = view.findViewById(R.id.form_txt_ssh_host)
    private val sshPortEditText: EditText = view.findViewById(R.id.form_txt_ssh_port)
    private val sshUsernameEditText: EditText = view.findViewById(R.id.form_txt_ssh_username)
    private val sshAuthTypeSpinner: AppCompatSpinner = view.findViewById(R.id.form_spn_ssh_auth_type)
    private val sshPasswordEditText: EditText = view.findViewById(R.id.form_txt_ssh_password)
    private val sshPassphraseEditText: EditText = view.findViewById(R.id.form_txt_ssh_passphrase)
    private val sshPrivateKeyEditText: EditText = view.findViewById(R.id.form_txt_ssh_private_key)

    private var sshPasswordField: ViewGroup = view.findViewById(R.id.form_field_ssh_password)
    private var sshPassphraseField: ViewGroup = view.findViewById(R.id.form_field_ssh_passphrase)
    private var sshPrivateKeyField: ViewGroup = view.findViewById(R.id.form_field_ssh_private_key)

    init {
        sshAuthTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                EzLogger.d("Position: $id")
                val selectedType = context.resources.getStringArray(R.array.form_ssh_auth_types)[position]

                EzLogger.d("selectedType: $selectedType")
                when (selectedType) {
                    context.getString(R.string.form_ssh_auth_type_password) -> {
                        sshPasswordField.visibility = View.VISIBLE
                        sshPassphraseField.visibility = View.GONE
                        sshPrivateKeyField.visibility = View.GONE
                    }
                    context.getString(R.string.form_ssh_auth_type_private_key) -> {
                        sshPasswordField.visibility = View.GONE
                        sshPassphraseField.visibility = View.VISIBLE
                        sshPrivateKeyField.visibility = View.VISIBLE
                    }
                    else -> {
                        sshPasswordField.visibility = View.GONE
                        sshPassphraseField.visibility = View.GONE
                        sshPrivateKeyField.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun compileConnectionInfo(connectionInfo: ConnectionInfo): ConnectionInfo {
        return connectionInfo.copy(
            sshConfig = sshHostEditText.nonBlankStringValue?.let { host ->
                sshPortEditText.nonBlankStringValue?.toInt()?.let { port ->
                    sshUsernameEditText.nonBlankStringValue?.let { username ->
                        val selectedPos = sshAuthTypeSpinner.selectedItemPosition
                        val selectedVal = context.resources.getStringArray(R.array.form_ssh_auth_types)[selectedPos]

                        when (selectedVal) {
                            context.getString(R.string.form_ssh_auth_type_none) ->
                                SshConfig(Address(host, port), None(username))
                            context.getString(R.string.form_ssh_auth_type_password) ->
                                SshConfig(Address(host, port),
                                    BasicAuth(username, sshPasswordEditText.nonBlankStringValue))
                            context.getString(R.string.form_ssh_auth_type_private_key) ->
                                SshConfig(Address(host, port),
                                    PrivateKey(username,
                                        sshPassphraseEditText.nonBlankStringValue,
                                        sshPrivateKeyEditText.stringValue))
                            else -> throw UnsupportedOperationException("Unknown ssh auth type: $selectedVal")
                        }
                    }
                }
            }
        )
    }

    override fun populate(connectionInfo: ConnectionInfo) {
        sshHostEditText.setText(connectionInfo.sshConfig?.address?.host)
        connectionInfo.sshConfig?.address?.port?.let { sshPort ->
            sshPortEditText.setText("$sshPort")
        }

        val authentication = connectionInfo.sshConfig?.authentication
        when (authentication) {
            is BasicAuth -> {
                sshUsernameEditText.setText(authentication.username)
                sshAuthTypeSpinner.setSelectionByResource(R.string.form_ssh_auth_type_password)
                sshPasswordEditText.setText(authentication.password)
            }
            is PrivateKey -> {
                sshUsernameEditText.setText(authentication.username)
                sshAuthTypeSpinner.setSelectionByResource(R.string.form_ssh_auth_type_private_key)
                sshPrivateKeyEditText.setText(authentication.privateKeyContents)
            }
            is None -> {
                sshUsernameEditText.setText(authentication.username)
                sshAuthTypeSpinner.setSelectionByResource(R.string.form_ssh_auth_type_none)
            }
        }
    }

    override fun populate(driverDefaults: DriverDefaults) {
        // ignore
    }
}