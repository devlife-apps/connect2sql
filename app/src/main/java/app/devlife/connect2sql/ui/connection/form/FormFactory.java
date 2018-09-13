package app.devlife.connect2sql.ui.connection.form;

import android.content.Context;
import android.view.LayoutInflater;

import com.gitlab.connect2sql.R;

import app.devlife.connect2sql.sql.DriverType;

public class FormFactory {
    public static Form get(Context context, LayoutInflater layoutInflater, DriverType driverType) throws FormConfigNotFound {
        switch (driverType) {
            case MYSQL:
                return new Form(context, layoutInflater.inflate(R.layout.form_mysql, null), driverType);
            case MSSQL:
                return new Form(context, layoutInflater.inflate(R.layout.form_mssql, null), driverType);
            case SYBASE:
                return new Form(context, layoutInflater.inflate(R.layout.form_sybase, null), driverType);
            case POSTGRES:
                return new Form(context, layoutInflater.inflate(R.layout.form_postgres, null), driverType);
            default:
                throw new FormConfigNotFound(driverType);
        }
    }

    public static class FormConfigNotFound extends Exception {
        FormConfigNotFound(DriverType driverType) {
            super("No FormConfig for driver " + driverType.name() + " found!");
        }
    }
}
