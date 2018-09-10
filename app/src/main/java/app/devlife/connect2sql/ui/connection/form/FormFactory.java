package app.devlife.connect2sql.ui.connection.form;

import android.content.Context;
import android.view.LayoutInflater;

import com.mobsandgeeks.saripaar.Validator;

import com.gitlab.connect2sql.R;

import app.devlife.connect2sql.sql.DriverType;
import app.devlife.connect2sql.sql.DriverType;

/**
 * Created by javier.romero on 5/3/14.
 */
public class FormFactory {
    public static BaseForm get(Context context, LayoutInflater layoutInflater, DriverType driverType) throws FormConfigNotFound {
        switch (driverType) {
            case MYSQL:
                return new MySqlForm(context, layoutInflater.inflate(R.layout.form_mysql, null));
            case MSSQL:
                return new MsSqlForm(context, layoutInflater.inflate(R.layout.form_mssql, null));
            case SYBASE:
                return new SybaseForm(context, layoutInflater.inflate(R.layout.form_sybase, null));
            case POSTGRES:
                return new PostgresForm(context, layoutInflater.inflate(R.layout.form_postgres, null));
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
