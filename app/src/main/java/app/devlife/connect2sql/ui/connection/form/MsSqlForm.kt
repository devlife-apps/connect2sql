package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.view.View

import app.devlife.connect2sql.db.model.connection.ConnectionInfo
import app.devlife.connect2sql.sql.DriverType

/**
 * Created by javier.romero on 5/3/14.
 */
class MsSqlForm(context: Context, view: View) : BaseMsSqlForm(context, view) {

    override fun compileConnectionInfo(): ConnectionInfo {
        return super.compileConnectionInfo().copy(driverType = DriverType.MSSQL)
    }
}
