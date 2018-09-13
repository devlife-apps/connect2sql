package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.DriverType
import app.devlife.connect2sql.sql.driver.DriverDefaults
import app.devlife.connect2sql.ui.connection.form.section.ConnectionFormSection
import app.devlife.connect2sql.ui.connection.form.section.MsSqlFormSection
import app.devlife.connect2sql.ui.connection.form.section.PostgresFormSection
import app.devlife.connect2sql.ui.connection.form.section.SshFormSection
import app.devlife.connect2sql.ui.widget.NotifyingScrollView
import com.gitlab.connect2sql.R
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Required

open class Form(val context: Context, val view: View, private val driverType: DriverType) {

    private val viewGroup: ViewGroup = view as ViewGroup
    private val validator: Validator by lazy { Validator(this@Form) }
    val actionBarContainer: ActionBarContainer = view.findViewById(R.id.form_actionbar)
    val scrollView: NotifyingScrollView = view.findViewById(R.id.scroll_view)

    @Required(order = 1, messageResId = R.string.form_error_name_required)
    val nameEditText: EditText = view.findViewById(R.id.form_txt_name)

    val testButton: Button = view.findViewById(R.id.form_btn_test)
    val saveButton: Button = view.findViewById(R.id.form_btn_save)

    val connectionFormSection = ConnectionFormSection(view)
    val sshFormSection = SshFormSection(context, view)
    val msSqlFormSection = if (driverType in listOf(DriverType.MSSQL, DriverType.SYBASE)) {
        MsSqlFormSection(view)
    } else null
    val postgresFormSection = if (driverType == DriverType.POSTGRES) {
        PostgresFormSection(context,
            view,
            connectionFormSection.databaseEditTextView)
    } else null

    private val sections = listOfNotNull(
        connectionFormSection,
        sshFormSection,
        msSqlFormSection,
        postgresFormSection
    )

    open fun compileConnectionInfo(): ConnectionInfo {
        return ConnectionInfo(
            -1,
            "",
            driverType,
            "",
            0,
            "",
            null,
            null,
            null,
            hashMapOf())
            .let { connectionInfo ->
                sections.fold(connectionInfo) { c, s -> s.compileConnectionInfo(c) }
            }
    }

    open fun populate(connectionInfo: ConnectionInfo) {
        nameEditText.setText(connectionInfo.name)
        sections.forEach { it.populate(connectionInfo) }
    }

    fun populate(driverDefaults: DriverDefaults) {
        sections.forEach { it.populate(driverDefaults) }
    }

    open fun onPreValidate(validator: Validator) {
        sections.forEach { it.onPreValidate(validator) }
    }

    fun validate(listener: Validator.ValidationListener) {
        validator.validationListener = listener
        onPreValidate(validator)
        validator.validate()
    }
}
