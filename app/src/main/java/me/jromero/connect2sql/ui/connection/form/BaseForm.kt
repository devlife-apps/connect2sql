package me.jromero.connect2sql.ui.connection.form

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NumberRule
import com.mobsandgeeks.saripaar.annotation.Required

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import com.gitlab.connect2sql.R
import me.jromero.connect2sql.sql.DriverType
import me.jromero.connect2sql.sql.driver.DriverDefaults
import me.jromero.connect2sql.ui.widget.NotifyingScrollView

/**
 * Created by javier.romero on 4/29/14.
 */
abstract class BaseForm(val context: Context, view: View) {

    @Required(order = 1, messageResId = R.string.form_error_name_required)
    val nameEditText: EditText
    @Required(order = 2, messageResId = R.string.form_error_host_required)
    val hostEditText: EditText
    @Required(order = 3, messageResId = R.string.form_error_port_required)
    @NumberRule(order = 4, messageResId = R.string.form_error_port_required, type = NumberRule.NumberType.INTEGER, gt = 0.0)
    val portEditText: EditText
    @Required(order = 5, messageResId = R.string.form_error_username_required)
    val usernameEditText: EditText
    val passwordEditText: EditText
    val databaseEditTextView: EditText
    private val mView: ViewGroup
    val validator: Validator
    val testButton: Button
    val saveButton: Button
    val scrollView: NotifyingScrollView
    val actionBarContainer: ActionBarContainer

    init {
        mView = view as ViewGroup

        validator = Validator(this)

        actionBarContainer = mView.findViewById(R.id.form_actionbar) as ActionBarContainer
        scrollView = mView.findViewById(R.id.scroll_view) as NotifyingScrollView

        nameEditText = mView.findViewById(R.id.form_txt_name) as EditText
        hostEditText = view.findViewById(R.id.form_txt_host) as EditText
        portEditText = view.findViewById(R.id.form_txt_port) as EditText
        usernameEditText = view.findViewById(R.id.form_txt_username) as EditText
        passwordEditText = view.findViewById(R.id.form_txt_password) as EditText
        databaseEditTextView = view.findViewById(R.id.form_txt_database) as EditText

        testButton = mView.findViewById(R.id.form_btn_test) as Button
        saveButton = mView.findViewById(R.id.form_btn_save) as Button
    }

    val view: View
        get() = mView

    fun setOnActionOnClickListener(onActionOnClickListener: Field.OnActionClickListener) {
        setOnActionOnClickListenerRecursive(mView, onActionOnClickListener)
    }

    private fun setOnActionOnClickListenerRecursive(viewGroup: ViewGroup, onActionOnClickListener: Field.OnActionClickListener) {
        for (i in 0..viewGroup.childCount - 1) {
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
        when (view.id) {
            R.id.form_txt_name -> return R.string.help_name
            R.id.form_txt_host -> return R.string.help_host
            R.id.form_txt_port -> return R.string.help_port
            R.id.form_txt_username -> return R.string.help_username
            R.id.form_txt_password -> return R.string.help_password
            R.id.form_txt_database -> return R.string.help_database
            else -> return 0
        }
    }

    open fun generateConnectionInfo(): ConnectionInfo {
        return ConnectionInfo(
                -1,
                nameEditText.text.toString(),
                DriverType.MYSQL,
                hostEditText.text.toString(),
                Integer.parseInt(portEditText.text.toString()),
                usernameEditText.text.toString(),
                passwordEditText.text.toString(),
                databaseEditTextView.text.toString(),
                hashMapOf())
    }

    open fun populate(connectionInfo: ConnectionInfo) {
        nameEditText.setText(connectionInfo.name)
        hostEditText.setText(connectionInfo.host)
        portEditText.setText("" + connectionInfo.port)
        usernameEditText.setText(connectionInfo.username)
        passwordEditText.setText(connectionInfo.password)
        databaseEditTextView.setText(connectionInfo.database)
    }

    fun populate(driverDefaults: DriverDefaults) {
        portEditText.setText("" + driverDefaults.port)
    }
}
