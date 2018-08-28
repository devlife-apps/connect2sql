package app.devlife.connect2sql.ui.widget;

import com.gitlab.connect2sql.R;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

public class Toast extends android.widget.Toast {

    public Toast(Context context) {
        super(context);
    }

    public static Toast makeText(Context context, CharSequence s, int duration) {
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        TextView view = new TextView(context);
        view.setText(s);
        view.setTextColor(0xFFFFFFFF);
        view.setGravity(Gravity.CENTER);
        view.setBackgroundResource(R.drawable.toast_frame);
        toast.setView(view);
        //toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 40);
        return toast;
    }
}
