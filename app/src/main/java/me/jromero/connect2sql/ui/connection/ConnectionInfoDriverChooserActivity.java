package me.jromero.connect2sql.ui.connection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import me.jromero.connect2sql.activity.BaseActivity;
import com.gitlab.connect2sql.R;
import me.jromero.connect2sql.sql.DriverType;

public class ConnectionInfoDriverChooserActivity extends BaseActivity implements ConnectionInfoDriverChooserFragment.Host {

    public static Intent newIntent(Context context) {
        return new Intent(context, ConnectionInfoDriverChooserActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, ConnectionInfoDriverChooserFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void onDriverSelected(DriverType driverType) {
        Intent intent = ConnectionInfoEditorActivity.Companion.newIntent(this, new ConnectionInfoEditorRequest(driverType));
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            finish();
        }
    }
}
