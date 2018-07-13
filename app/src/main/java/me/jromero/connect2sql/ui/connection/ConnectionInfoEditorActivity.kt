package me.jromero.connect2sql.ui.connection

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.mobsandgeeks.saripaar.Rule
import com.mobsandgeeks.saripaar.Validator
import me.jromero.connect2sql.ApplicationUtils
import me.jromero.connect2sql.activity.BaseActivity
import me.jromero.connect2sql.connection.ConnectionAgent
import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.db.repo.ConnectionInfoRepository
import com.gitlab.connect2sql.R
import me.jromero.connect2sql.log.EzLogger
import me.jromero.connect2sql.sql.driver.DriverDefaults
import me.jromero.connect2sql.ui.connection.form.*
import me.jromero.connect2sql.ui.widget.NotifyingScrollView
import me.jromero.connect2sql.ui.widget.Toast
import me.jromero.connect2sql.ui.widget.dialog.ProgressDialog
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class ConnectionInfoEditorActivity : BaseActivity() {
    private val request: ConnectionInfoEditorRequest by lazy {
        intent?.extras?.getParcelable<ConnectionInfoEditorRequest>(EXTRA_CONNECTION_INFO_REQUEST)!!
    }

    private val nameBarBackgroundDrawable: Drawable by lazy {
        val drawable = resources.getDrawable(R.drawable.action_bar_background)
        drawable.alpha = 0
        drawable
    }

    private var doOnValidationSuccess: ValidationAction? = null
    lateinit var nameBarContainer: ActionBarContainer
    lateinit var form: BaseForm

    @Inject
    lateinit var connectionInfoRepository: ConnectionInfoRepository

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val activity = this

        var connectionInfo: ConnectionInfo? = null
        if (request.action == ConnectionInfoEditorRequest.Action.EDIT) {
            connectionInfo = connectionInfoRepository.getConnectionInfo(request.connectionInfoId)
            request.driverType = connectionInfo!!.driverType
        }

        form = FormFactory.get(activity, layoutInflater, request.driverType)

        // ensure we bring up the keyboard at startup
        form.nameEditText.post { inputMethodManager.showSoftInput(form.nameEditText, InputMethodManager.SHOW_IMPLICIT) }

        // setup actionbar
        nameBarContainer = form.actionBarContainer
        nameBarContainer.background = nameBarBackgroundDrawable
        nameBarContainer.titleView.alpha = 0f
        nameBarContainer.logoView.setImageResource(DriverLogo.fromDriverType(request.driverType).resource)

        // set listeners
        form.nameEditText.addTextChangedListener(nameTextWatcher)
        form.testButton.setOnClickListener(onTestButtonClickListener)
        form.saveButton.setOnClickListener(onSaveButtonClickListener)
        form.scrollView.setOnScrollChangedListener(onScrollChangedListener)
        form.setOnActionOnClickListener(onActionOnClickListener)

        // populate form
        if (connectionInfo != null) {
            form.populate(connectionInfo)
        } else {
            form.populate(DriverDefaults.fromDriver(request.driverType))
        }

        setContentView(form.view)
    }

    private fun saveConnection(): ConnectionInfo {
        /**
         * Save connection
         */
        val connectionInfo = form.generateConnectionInfo()
        when (request.action) {
            ConnectionInfoEditorRequest.Action.EDIT -> {
                val id = connectionInfoRepository.save(connectionInfo.copy(id = request.connectionInfoId))
                return connectionInfo.copy(id = id)
            }
            ConnectionInfoEditorRequest.Action.NEW-> {
                val id = connectionInfoRepository.save(connectionInfo)
                return connectionInfo.copy(id = id)
            }
        }
    }

    private fun testConnection(): Boolean {

        val connectionInfo = form.generateConnectionInfo()

        /**
         * Ask for password if empty
         */
        if (TextUtils.isEmpty(connectionInfo.password)) {
            val promptDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_prompt, null)

            (promptDialogView.findViewById(R.id.textView1) as TextView).visibility = View.GONE

            val passwordText = promptDialogView.findViewById(R.id.editView1) as EditText

            passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle("Password?")
            alertBuilder.setView(promptDialogView)
            alertBuilder.setPositiveButton("OK") { dialog, which ->
                // execute testing of connection
                executeTestConnection(connectionInfo.copy(password = passwordText.text.toString()))
            }
            alertBuilder.create().show()
        } else {
            executeTestConnection(connectionInfo)
        }

        return true
    }

    private fun executeTestConnection(connectionInfo: ConnectionInfo) {
        val progressDialog = ProgressDialog(this, "Testing", "Testing configuration...")
        progressDialog.show()

        val connectionAgent = ConnectionAgent()
        connectionAgent
                .connect(connectionInfo)
                .switchMap { connection -> connectionAgent.disconnect(connection) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Unit>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                        EzLogger.w(e.message, e)
                        progressDialog.dismiss()

                        val builder = AlertDialog.Builder(this@ConnectionInfoEditorActivity)
                        builder.setTitle("Error")
                        builder.setMessage(e.message)
                        builder.setNeutralButton("OK", null)
                        builder.create().show()
                    }

                    override fun onNext(nothing: Unit) {
                        progressDialog.dismiss()

                        val builder = AlertDialog.Builder(this@ConnectionInfoEditorActivity)
                        builder.setTitle("Success")
                        builder.setMessage("Connected to server successfully!")
                        builder.setNeutralButton("OK", null)
                        builder.create().show()
                    }
                })
    }

    private enum class ValidationAction {
        TEST,
        SAVE
    }

    private val validationListener = object : Validator.ValidationListener {
        override fun onValidationSucceeded() {
            when (doOnValidationSuccess) {
                ValidationAction.TEST -> testConnection()
                ValidationAction.SAVE -> onSaved(saveConnection())
            }
        }

        override fun onValidationFailed(failedView: View, failedRule: Rule<*>) {
            Toast.makeText(this@ConnectionInfoEditorActivity, failedRule.failureMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private val onActionOnClickListener = Field.OnActionClickListener { action, actionView, inputView ->
        when (action) {
            Field.Action.VISIBLE -> if (inputView.id == R.id.form_txt_password) {
                val editText = inputView as EditText
                togglePasswordVisibility(editText)
                editText.setSelection(editText.text.length)
            }
            Field.Action.KEYBOARD_INPUT -> if (inputView.id == R.id.form_txt_host) {
                toggleAlphaToNumeric(inputView as EditText, false, false)
            }
            Field.Action.HELP -> {
                val helpMessageResource = form.getHelpMessageResource(inputView)
                if (helpMessageResource > 0) {
                    showHelp(helpMessageResource)
                }
            }
            else -> EzLogger.w("Unknown action: " + action)
        }
    }

    private fun showHelp(resourceHelpMessage: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.form_action_help)
        builder.setMessage(resourceHelpMessage)
        builder.setPositiveButton(R.string.help_positive_btn_label) { dialog, which -> dialog.dismiss() }
        builder.show()
    }

    private val onScrollChangedListener = NotifyingScrollView.OnScrollChangedListener { who, l, t, oldl, oldt ->
        val headerHeight = nameBarContainer.height / 2
        val ratio = Math.min(Math.max(t, 0), headerHeight).toFloat() / headerHeight
        val newAlpha = (ratio * 255).toInt()
        if (newAlpha >= 150) {
            nameBarContainer.titleView.alpha = ratio
        } else {
            nameBarContainer.titleView.alpha = 0f
        }
        nameBarBackgroundDrawable.alpha = newAlpha
    }

    private val nameTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            nameBarContainer.titleView.text = s.toString()
        }

        override fun afterTextChanged(s: Editable) {

        }
    }

    private val onTestButtonClickListener = View.OnClickListener {
        doOnValidationSuccess = ValidationAction.TEST
        form.validator.validationListener = validationListener
        form.validator.validate()
    }

    private val onSaveButtonClickListener = View.OnClickListener {
        doOnValidationSuccess = ValidationAction.SAVE
        form.validator.validationListener = validationListener
        form.validator.validate()
    }

    private fun toggleAlphaToNumeric(editText: EditText, strict: Boolean, signed: Boolean) {
        val inputType = editText.inputType
        if (FormUtils.hasInputType(inputType, InputType.TYPE_CLASS_NUMBER)) {
            var inputType1 = FormUtils.addInputType(inputType, InputType.TYPE_CLASS_TEXT)
            inputType1 = FormUtils.removeInputType(inputType1, InputType.TYPE_CLASS_NUMBER)
            if (signed) {
                inputType1 = FormUtils.removeInputType(inputType1, InputType.TYPE_NUMBER_FLAG_SIGNED)
            }
            editText.inputType = inputType1
        } else {
            var inputType2 = FormUtils.addInputType(inputType, InputType.TYPE_CLASS_NUMBER)
            if (strict) {
                inputType2 = FormUtils.removeInputType(inputType2, InputType.TYPE_CLASS_TEXT)
            }
            if (signed) {
                inputType2 = FormUtils.addInputType(inputType2, InputType.TYPE_NUMBER_FLAG_SIGNED)
            }
            editText.inputType = inputType2
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        val inputType = editText.inputType
        if (FormUtils.hasInputType(inputType, InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = FormUtils.removeInputType(inputType, InputType.TYPE_TEXT_VARIATION_PASSWORD)
        } else {
            editText.inputType = FormUtils.addInputType(inputType, InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
    }


    fun onSaved(connectionInfo: ConnectionInfo) {
        setResult(RESULT_OK)
        finish()
    }

    companion object {

        private val EXTRA_CONNECTION_INFO_REQUEST = "EXTRA_CONNECTION_INFO_REQUEST"

        fun newIntent(context: Context, request: ConnectionInfoEditorRequest): Intent {
            val intent = Intent(context, ConnectionInfoEditorActivity::class.java)
            intent.putExtra(EXTRA_CONNECTION_INFO_REQUEST, request)
            return intent
        }
    }
}