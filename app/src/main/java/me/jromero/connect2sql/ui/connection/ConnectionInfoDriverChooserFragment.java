package me.jromero.connect2sql.ui.connection;

import com.gitlab.connect2sql.R;
import me.jromero.connect2sql.fragment.BaseFragment;
import me.jromero.connect2sql.sql.DriverType;
import me.jromero.connect2sql.ui.widget.BlockItem;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.apps.iosched.ui.widget.DashboardLayout;

public class ConnectionInfoDriverChooserFragment extends BaseFragment implements
        OnClickListener {

    private static final String TAG = ConnectionInfoDriverChooserFragment.class.getCanonicalName();
    private Host mHost;

    public static ConnectionInfoDriverChooserFragment newInstance() {
        return new ConnectionInfoDriverChooserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_connection_drivers, null);

        // get dashboard
        DashboardLayout mDashboardLayout = (DashboardLayout) v.findViewById(R.id.drivers_dashboard);

        // loop through children in dash board to set listeners
        int totalChildren = mDashboardLayout.getChildCount();
        for (int i = 0; i < totalChildren; i++) {
            if (mDashboardLayout.getChildAt(i) instanceof BlockItem) {
                ((BlockItem) mDashboardLayout.getChildAt(i))
                        .setOnClickListener(this);
            }
        }

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mHost = (Host) activity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // remove previous options
        // the activity will reset them once taken back
        menu.clear();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bi_mysql) {
            mHost.onDriverSelected(DriverType.MYSQL);
        } else if (id == R.id.bi_mssql) {
            mHost.onDriverSelected(DriverType.MSSQL);
        } else if (id == R.id.bi_postgre) {
            mHost.onDriverSelected(DriverType.POSTGRES);
        } else if (id == R.id.bi_sybase) {
            mHost.onDriverSelected(DriverType.SYBASE);
        } else {
            Log.w(TAG, "Unknown DriverType for id: " + getId());
            return;
        }
    }

    public interface Host {
        public void onDriverSelected(DriverType driverType);
    }
}
