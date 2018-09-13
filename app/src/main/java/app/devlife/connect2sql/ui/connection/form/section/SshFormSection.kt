package app.devlife.connect2sql.ui.connection.form.section

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v7.widget.AppCompatSpinner
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.Address
import app.devlife.connect2sql.db.model.connection.BasicAuth
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.connection.None
import app.devlife.connect2sql.db.model.connection.PrivateKey
import app.devlife.connect2sql.db.model.connection.SshConfig
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.util.ext.nonBlankStringValue
import app.devlife.connect2sql.util.ext.setSelectionByResource
import app.devlife.connect2sql.util.ext.stringValue
import com.gitlab.connect2sql.R
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Required

class SshFormSection(private val context: Context, view: View) :
    FormSection {

    private val sshEnableSwitch: CompoundButton = view.findViewById(R.id.form_switch_ssh_tunnel)

    private val sshFields: View = view.findViewById(R.id.form_fields_ssh)

    @Required(order = 10, messageResId = R.string.form_error_ssh_host_required)
    private val sshHostEditText: EditText = view.findViewById(R.id.form_txt_ssh_host)
    @Required(order = 11, messageResId = R.string.form_error_ssh_port_required)
    private val sshPortEditText: EditText = view.findViewById(R.id.form_txt_ssh_port)
    @Required(order = 12, messageResId = R.string.form_error_ssh_username_required)
    private val sshUsernameEditText: EditText = view.findViewById(R.id.form_txt_ssh_username)
    private val sshAuthTypeSpinner: AppCompatSpinner = view.findViewById(R.id.form_spn_ssh_auth_type)
    private val sshPasswordEditText: EditText = view.findViewById(R.id.form_txt_ssh_password)
    private val sshPassphraseEditText: EditText = view.findViewById(R.id.form_txt_ssh_passphrase)
    private val sshPrivateKeyEditText: EditText = view.findViewById(R.id.form_txt_ssh_private_key)

    private var sshPasswordField: ViewGroup = view.findViewById(R.id.form_field_ssh_password)
    private var sshPassphraseField: ViewGroup = view.findViewById(R.id.form_field_ssh_passphrase)
    private var sshPrivateKeyField: ViewGroup = view.findViewById(R.id.form_field_ssh_private_key)

    private val sshHelp: View = view.findViewById(R.id.form_help_ssh)
    private val sshHelpLink: View = view.findViewById(R.id.form_link_ssh)

    init {
        sshAuthTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = context.resources.getStringArray(R.array.form_ssh_auth_types)[position]
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

        sshEnableSwitch.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                true -> {
                    sshFields.visibility = View.VISIBLE
                    sshHelp.visibility = View.GONE
                }
                false -> {
                    sshFields.visibility = View.GONE
                    sshHelp.visibility = View.VISIBLE
                }
            }
        }

        sshHelpLink.setOnClickListener {
            CustomTabsIntent.Builder()
                .setToolbarColor(context.resources.getColor(R.color.blueBase, context.theme))
                .build()
                .launchUrl(context,
                    Uri.parse(context.getString(R.string.help_ssh_tunnel_learn_more_url)))
        }
    }

    override fun compileConnectionInfo(connectionInfo: ConnectionInfo): ConnectionInfo {
        return when (sshEnableSwitch.isChecked) {
            false -> connectionInfo
            true -> connectionInfo.copy(
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
                                        BasicAuth(
                                            username,
                                            sshPasswordEditText.nonBlankStringValue
                                        ))
                                context.getString(R.string.form_ssh_auth_type_private_key) ->
                                    SshConfig(Address(host, port),
                                        PrivateKey(
                                            username,
                                            sshPassphraseEditText.nonBlankStringValue,
                                            sshPrivateKeyEditText.stringValue
                                        ))
                                else -> throw UnsupportedOperationException("Unknown ssh auth type: $selectedVal")
                            }
                        }
                    }
                }
            )
        }

    }

    override fun populate(connectionInfo: ConnectionInfo) {
        sshHostEditText.setText(connectionInfo.sshConfig?.address?.host)
        connectionInfo.sshConfig?.address?.port?.let { sshPort ->
            sshPortEditText.setText("$sshPort")
        }

        sshEnableSwitch.isChecked = connectionInfo.sshConfig != null

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
        sshPortEditText.setText("$DEFAULT_SSH_PORT")
    }

    override fun validate(listener: Validator.ValidationListener) {
        if (sshEnableSwitch.isChecked) {
            Validator(this)
                .also { it.validationListener = listener }
                .validate()
        }
    }

    companion object {
        private const val DEFAULT_SSH_PORT = 22
    }
}