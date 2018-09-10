package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.view.View
import android.widget.EditText

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import com.gitlab.connect2sql.R

/**
 * Created by javier.romero on 5/5/14.
 */
abstract class BaseMsSqlForm(context: Context, view: View) : BaseForm(context, view) {

    val domainEditTextView: EditText
    val instanceEditTextView: EditText

    init {
        domainEditTextView = view.findViewById(R.id.form_txt_domain) as EditText
        instanceEditTextView = view.findViewById(R.id.form_txt_instance) as EditText
    }

    override fun compileConnectionInfo(): ConnectionInfo {
        val connectionInfo = super.compileConnectionInfo()

        val options = connectionInfo.options + hashMapOf(
                Pair(ConnectionInfo.OPTION_DOMAIN, domainEditTextView.text.toString()),
                Pair(ConnectionInfo.OPTION_INSTANCE, instanceEditTextView.text.toString())
        )

        return connectionInfo.copy(options = options)
    }

    override fun populate(connectionInfo: ConnectionInfo) {
        super.populate(connectionInfo)

        val domain = connectionInfo.options.get(ConnectionInfo.OPTION_DOMAIN) ?: ""
        domainEditTextView.setText(domain)

        val instance = connectionInfo.options.get(ConnectionInfo.OPTION_INSTANCE) ?: ""
        instanceEditTextView.setText(instance)
    }

    override fun getHelpMessageResource(view: View): Int {
        when (view.id) {
            R.id.form_txt_domain -> return R.string.help_domain
            R.id.form_txt_instance -> return R.string.help_instance
            else -> return super.getHelpMessageResource(view)
        }
    }
}
