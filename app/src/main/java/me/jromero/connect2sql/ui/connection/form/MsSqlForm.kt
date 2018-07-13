package me.jromero.connect2sql.ui.connection.form

import android.content.Context
import android.view.View

import me.jromero.connect2sql.db.model.connection.ConnectionInfo
import me.jromero.connect2sql.sql.DriverType

/**
 * Created by javier.romero on 5/3/14.
 */
class MsSqlForm(context: Context, view: View) : BaseMsSqlForm(context, view) {


    override fun generateConnectionInfo(): ConnectionInfo {
        return super.generateConnectionInfo().copy(driverType = DriverType.MSSQL)
    }
}
