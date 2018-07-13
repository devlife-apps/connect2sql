package me.jromero.connect2sql.ui.connection.form;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gitlab.connect2sql.R;

/**
 * Created by javier.romero on 5/5/14.
 */
public class ActionBarContainer extends FrameLayout {
    private ImageView mLogoView;
    private TextView mTitleView;
    private View mBackground;

    public ActionBarContainer(Context context) {
        this(context, null);
    }

    public ActionBarContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBarContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.form_actionbar, this);
        mBackground = view.findViewById(R.id.form_actionbar_background);
        mLogoView = (ImageView) view.findViewById(R.id.form_actionbar_logo);
        mTitleView = (TextView) view.findViewById(R.id.form_actionbar_title);
    }

    public ImageView getLogoView() {
        return mLogoView;
    }

    public TextView getTitleView() {
        return mTitleView;
    }

    @Override
    public void setBackgroundResource(int resid) {
        setBackground(getResources().getDrawable(resid));
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        setBackground(background);
    }

    @Override
    public void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBackground.setBackground(background);
        } else {
            mBackground.setBackgroundDrawable(background);
        }
    }
}
