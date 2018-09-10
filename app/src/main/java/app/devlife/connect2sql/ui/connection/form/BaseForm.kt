package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.support.v7.widget.AppCompatSpinner
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.Address
import app.devlife.connect2sql.db.model.connection.BasicAuth
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.model.connection.None
import app.devlife.connect2sql.db.model.connection.PrivateKey
import app.devlife.connect2sql.db.model.connection.SshConfig
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.DriverType
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.ui.widget.NotifyingScrollView
import app.devlife.connect2sql.util.ext.nonBlankStringValue
import app.devlife.connect2sql.util.ext.setSelectionByResource
import app.devlife.connect2sql.util.ext.stringValue
import com.gitlab.connect2sql.R
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NumberRule
import com.mobsandgeeks.saripaar.annotation.Required

abstract class BaseForm(val context: Context, view: View) {

    private val viewGroup: ViewGroup = view as ViewGroup
    private val validator: Validator by lazy { Validator(this@BaseForm) }
    val actionBarContainer: ActionBarContainer
    val scrollView: NotifyingScrollView

    @Required(order = 1, messageResId = R.string.form_error_name_required)
    val nameEditText: EditText
    @Required(order = 2, messageResId = R.string.form_error_host_required)
    val hostEditText: EditText
    @Required(order = 3, messageResId = R.string.form_error_port_required)
    @NumberRule(
        order = 4,
        messageResId = R.string.form_error_port_required,
        type = NumberRule.NumberType.INTEGER,
        gt = 0.0
    )
    val portEditText: EditText
    @Required(order = 5, messageResId = R.string.form_error_username_required)
    val usernameEditText: EditText
    val passwordEditText: EditText
    val databaseEditTextView: EditText

    private val sshHostEditText: EditText
    private val sshPortEditText: EditText
    private val sshUsernameEditText: EditText
    private val sshAuthTypeSpinner: AppCompatSpinner
    private val sshPasswordEditText: EditText
    private val sshPrivateKeyEditText: EditText
    private var sshPasswordField: ViewGroup
    private var sshPrivateKeyField: ViewGroup

    val testButton: Button
    val saveButton: Button


    init {
        actionBarContainer = viewGroup.findViewById(R.id.form_actionbar)
        scrollView = viewGroup.findViewById(R.id.scroll_view)

        nameEditText = viewGroup.findViewById(R.id.form_txt_name)
        hostEditText = view.findViewById(R.id.form_txt_host)
        portEditText = view.findViewById(R.id.form_txt_port)
        usernameEditText = view.findViewById(R.id.form_txt_username)
        passwordEditText = view.findViewById(R.id.form_txt_password)
        databaseEditTextView = view.findViewById(R.id.form_txt_database)


        sshHostEditText = view.findViewById(R.id.form_txt_ssh_host)
        sshPortEditText = view.findViewById(R.id.form_txt_ssh_port)
        sshUsernameEditText = view.findViewById(R.id.form_txt_ssh_username)
        sshPasswordEditText = view.findViewById(R.id.form_txt_ssh_password)
        sshPrivateKeyEditText = view.findViewById(R.id.form_txt_ssh_private_key)
        sshPasswordField = view.findViewById(R.id.form_field_ssh_password)
        sshPrivateKeyField = view.findViewById(R.id.form_field_ssh_private_key)
        sshAuthTypeSpinner = view.findViewById<AppCompatSpinner>(R.id.form_spn_ssh_auth_type).also {
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?,
                                            view: View?,
                                            position: Int,
                                            id: Long) {
                    EzLogger.d("Position: $id")
                    val selectedType = context.resources.getStringArray(R.array.form_ssh_auth_types)[position]

                    EzLogger.d("selectedType: $selectedType")
                    when (selectedType) {
                        context.getString(R.string.form_ssh_auth_type_password) -> {
                            sshPasswordField.visibility = View.VISIBLE
                            sshPrivateKeyField.visibility = View.GONE
                        }
                        context.getString(R.string.form_ssh_auth_type_private_key) -> {
                            sshPasswordField.visibility = View.GONE
                            sshPrivateKeyField.visibility = View.VISIBLE
                        }
                        else -> {
                            sshPasswordField.visibility = View.GONE
                            sshPrivateKeyField.visibility = View.GONE
                        }
                    }
                }
            }
        }

        testButton = viewGroup.findViewById(R.id.form_btn_test)
        saveButton = viewGroup.findViewById(R.id.form_btn_save)
    }

    val view: View
        get() = viewGroup

    fun setOnActionOnClickListener(onActionOnClickListener: Field.OnActionClickListener) {
        setOnActionOnClickListenerRecursive(viewGroup, onActionOnClickListener)
    }

    private fun setOnActionOnClickListenerRecursive(viewGroup: ViewGroup,
                                                    onActionOnClickListener: Field.OnActionClickListener) {
        for (i in 0 until viewGroup.childCount) {
            val v = viewGroup.getChildAt(i)
            if (v is Field) {
                v.setOnActionClickListener(onActionOnClickListener)
            } else if (v is ViewGroup) {
                setOnActionOnClickListenerRecursive(v, onActionOnClickListener)
            }
        }
    }

    /**
     * Gets the associated help message resource for a given view
     * @param view
     * *
     * @return
     */
    open fun getHelpMessageResource(view: View): Int {
        return when (view.id) {
            R.id.form_txt_name -> R.string.help_name
            R.id.form_txt_host -> R.string.help_host
            R.id.form_txt_port -> R.string.help_port
            R.id.form_txt_username -> R.string.help_username
            R.id.form_txt_password -> R.string.help_password
            R.id.form_txt_database -> R.string.help_database
            else -> {
                EzLogger.e("No help message associated with view.id = ${view.id}")
                0
            }
        }
    }

    open fun compileConnectionInfo(): ConnectionInfo {

        val sshConfig = sshHostEditText.nonBlankStringValue?.let { host ->
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
                                PrivateKey(username, sshPrivateKeyEditText.stringValue))
                        else -> throw UnsupportedOperationException("Unknown ssh auth type: $selectedVal")
                    }
                }
            }
        }

        return ConnectionInfo(
            -1,
            nameEditText.text.toString(),
            DriverType.MYSQL,
            hostEditText.text.toString(),
            Integer.parseInt(portEditText.text.toString()),
            usernameEditText.text.toString(),
            passwordEditText.text.toString(),
            databaseEditTextView.text.toString(),
            sshConfig,
            hashMapOf())
    }

    open fun populate(connectionInfo: ConnectionInfo) {
        nameEditText.setText(connectionInfo.name)
        hostEditText.setText(connectionInfo.host)
        portEditText.setText("${connectionInfo.port}")
        usernameEditText.setText(connectionInfo.username)
        passwordEditText.setText(connectionInfo.password)
        databaseEditTextView.setText(connectionInfo.database)

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

    fun populate(driverDefaults: DriverDefaults) {
        portEditText.setText("${driverDefaults.port}")
    }

    open fun onPreValidate(validator: Validator) {
        // allow subclasses to alter validator
    }

    fun validate(listener: Validator.ValidationListener) {
        validator.validationListener = listener
        onPreValidate(validator)
        validator.validate()
    }
}
