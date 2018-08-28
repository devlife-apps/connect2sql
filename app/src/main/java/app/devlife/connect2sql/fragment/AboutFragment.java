package app.devlife.connect2sql.fragment;

import com.gitlab.connect2sql.R;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        TextView devNull = ((TextView) v.findViewById(R.id.dev_null));
        devNull.setPaintFlags(devNull.getPaintFlags()
                | Paint.STRIKE_THRU_TEXT_FLAG);
        return v;
    }
}
