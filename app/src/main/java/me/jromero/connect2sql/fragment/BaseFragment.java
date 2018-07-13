package me.jromero.connect2sql.fragment;

import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class BaseFragment extends Fragment implements OnTouchListener {

    @Override
    public void onResume() {
        super.onResume();

        // prevent touches to interact with lower level fragment
        if (getView() != null) {
            getView().setOnTouchListener(this);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
