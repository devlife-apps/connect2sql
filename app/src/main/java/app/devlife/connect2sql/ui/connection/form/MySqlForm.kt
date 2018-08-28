package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.view.View

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.DriverType

/**
 * Created by javier.romero on 5/3/14.
 */
class MySqlForm(context: Context, view: View) : BaseForm(context, view) {

    override fun generateConnectionInfo(): ConnectionInfo {
        return super.generateConnectionInfo().copy(driverType = DriverType.MYSQL)
    }
}
