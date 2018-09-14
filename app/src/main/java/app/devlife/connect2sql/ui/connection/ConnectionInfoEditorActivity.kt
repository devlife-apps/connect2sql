package app.devlife.connect2sql.ui.connection

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
import app.devlife.connect2sql.ApplicationUtils
import app.devlife.connect2sql.activity.BaseActivity
import app.devlife.connect2sql.connection.ConnectionAgent
import app.devlife.connect2sql.connection.SshTunnelAgent
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository
import app.devlife.connect2sql.log.EzLogger
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.ui.connection.form.ActionBarContainer
import app.devlife.connect2sql.ui.connection.form.Form
import app.devlife.connect2sql.ui.connection.form.FormFactory
import app.devlife.connect2sql.ui.widget.NotifyingScrollView
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.ui.widget.dialog.ProgressDialog
import app.devlife.connect2sql.util.rx.ActivityAwareSubscriber
import com.gitlab.connect2sql.R
import com.jcraft.jsch.JSch
import com.mobsandgeeks.saripaar.Rule
import com.mobsandgeeks.saripaar.Validator
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class ConnectionInfoEditorActivity : BaseActivity() {
    private val request: ConnectionInfoEditorRequest by lazy {
        intent?.extras?.getParcelable<ConnectionInfoEditorRequest>(EXTRA_CONNECTION_INFO_REQUEST)!!
    }

    private val actionBarBackgroundDrawable: Drawable by lazy {
        val drawable = resources.getDrawable(R.drawable.action_bar_background)
        drawable.alpha = 0
        drawable
    }

    private lateinit var nameBarContainer: ActionBarContainer
    private lateinit var form: Form

    private var doOnValidationSuccess: ValidationAction? = null
    private var progressDialog: ProgressDialog? = null
    private var subscription: Subscription? = null

    @Inject
    lateinit var connectionAgent: ConnectionAgent
    @Inject
    lateinit var connectionInfoRepository: ConnectionInfoRepository
    @Inject
    lateinit var jSch: JSch

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApplicationUtils.getApplication(this).applicationComponent.inject(this)

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var connectionInfo: ConnectionInfo? = null
        if (request.action == ConnectionInfoEditorRequest.Action.EDIT) {
            connectionInfo = connectionInfoRepository.getConnectionInfo(request.connectionInfoId)
            request.driverType = connectionInfo!!.driverType
        }

        form = FormFactory.get(this, layoutInflater, request.driverType)

        // ensure we bring up the keyboard at startup
        form.nameEditText.post {
            inputMethodManager.showSoftInput(form.nameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        // setup actionbar
        nameBarContainer = form.actionBarContainer
        nameBarContainer.background = actionBarBackgroundDrawable
        nameBarContainer.titleView.alpha = 0f
        nameBarContainer.logoView.setImageResource(DriverLogo.fromDriverType(request.driverType).resource)

        // set listeners
        form.nameEditText.addTextChangedListener(nameTextWatcher)
        form.testButton.setOnClickListener(onTestButtonClickListener)
        form.saveButton.setOnClickListener(onSaveButtonClickListener)
        form.scrollView.setOnScrollChangedListener(onScrollChangedListener)

        // populate form
        if (connectionInfo != null) {
            form.populate(connectionInfo)
        } else {
            form.populate(DriverDefaults.fromDriver(request.driverType))
        }

        setContentView(form.view)
    }

    override fun onPause() {
        super.onPause()
        cancelTest()
    }

    private fun saveConnection(): ConnectionInfo {
        val connectionInfo = form.compileConnectionInfo()
        return when (request.action) {
            ConnectionInfoEditorRequest.Action.EDIT -> {
                val id = connectionInfoRepository.save(connectionInfo.copy(id = request.connectionInfoId))
                connectionInfo.copy(id = id)
            }
            ConnectionInfoEditorRequest.Action.NEW -> {
                val id = connectionInfoRepository.save(connectionInfo)
                connectionInfo.copy(id = id)
            }
        }
    }

    private fun testConnection(): Boolean {

        val connectionInfo = form.compileConnectionInfo()

        /**
         * Ask for password if empty
         */
        if (TextUtils.isEmpty(connectionInfo.password)) {
            val promptDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_prompt, null)
            val passwordText = promptDialogView.findViewById<EditText>(R.id.editView1)

            promptDialogView.findViewById<TextView>(R.id.textView1).visibility = View.GONE

            passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_password)
                .setView(promptDialogView)
                .setPositiveButton(R.string.dialog_ok) { _, _ ->
                    executeTestConnection(connectionInfo.copy(password = passwordText.text.toString()))
                }
                .create()
                .show()
        } else {
            executeTestConnection(connectionInfo)
        }

        return true
    }

    private fun executeTestConnection(connectionInfo: ConnectionInfo) {
        progressDialog = ProgressDialog(this, "Testing", "Testing configuration...")
        progressDialog?.setOnCancelListener { cancelTest() }
        progressDialog?.show()

        subscription = connectionAgent
            .connect(connectionInfo)
            .switchMap { connection -> connectionAgent.disconnect(connection) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ActivityAwareSubscriber(this@ConnectionInfoEditorActivity,
                object : Subscriber<Unit>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                        EzLogger.w(e.message, e)
                        progressDialog?.dismiss()

                        when (e) {
                            is SshTunnelAgent.UnknownHostException ->
                                AlertDialog.Builder(this@ConnectionInfoEditorActivity)
                                    .setTitle(R.string.dialog_add_host_key)
                                    .setMessage(getString(
                                        R.string.dialog_host_fingerprint,
                                        e.hostKey.host,
                                        e.hostKey.getFingerPrint(jSch)
                                    ))
                                    .setNegativeButton(R.string.dialog_no, null)
                                    .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                                        dialog.dismiss()
                                        jSch.hostKeyRepository.add(e.hostKey, null)
                                        executeTestConnection(connectionInfo)
                                    }
                                    .create()
                                    .show()
                            else ->
                                AlertDialog.Builder(this@ConnectionInfoEditorActivity)
                                    .setTitle(R.string.dialog_error)
                                    .setMessage("Couldn't connect:\n\n${e?.message}")
                                    .setNeutralButton(R.string.dialog_ok, null)
                                    .create()
                                    .show()
                        }
                    }

                    override fun onNext(nothing: Unit) {
                        progressDialog?.dismiss()

                        AlertDialog.Builder(this@ConnectionInfoEditorActivity)
                            .setTitle("Success")
                            .setMessage("Connected to server successfully!")
                            .setNeutralButton("OK", null)
                            .create()
                            .show()
                    }
                }))
    }

    private fun cancelTest() {
        progressDialog?.dismiss()
        subscription?.unsubscribe()
    }

    private enum class ValidationAction {
        TEST,
        SAVE
    }

    private val validationListener = object : Validator.ValidationListener {
        override fun onValidationSucceeded() {
            when (doOnValidationSuccess) {
                ValidationAction.TEST -> testConnection()
                ValidationAction.SAVE -> {
                    saveConnection()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        override fun onValidationFailed(failedView: View, failedRule: Rule<*>) {
            Toast.makeText(this@ConnectionInfoEditorActivity,
                failedRule.failureMessage,
                Toast.LENGTH_SHORT).show()
        }
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
        actionBarBackgroundDrawable.alpha = newAlpha
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
        form.validate(validationListener)
    }

    private val onSaveButtonClickListener = View.OnClickListener {
        doOnValidationSuccess = ValidationAction.SAVE
        form.validate(validationListener)
    }

    companion object {

        private const val EXTRA_CONNECTION_INFO_REQUEST = "EXTRA_CONNECTION_INFO_REQUEST"

        fun newIntent(context: Context, request: ConnectionInfoEditorRequest): Intent {
            val intent = Intent(context, ConnectionInfoEditorActivity::class.java)
            intent.putExtra(EXTRA_CONNECTION_INFO_REQUEST, request)
            return intent
        }
    }
}