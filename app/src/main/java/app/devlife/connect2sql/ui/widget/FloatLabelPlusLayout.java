package app.devlife.connect2sql.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import app.devlife.connect2sql.ui.connection.form.Field;
import app.devlife.connect2sql.ui.connection.form.Field;

/**
 * Created by javier.romero on 5/3/14.
 */
public class FloatLabelPlusLayout extends FloatLabelLayout implements Field {

    private static final long ANIMATION_DURATION = 300;

    private View mInputView;
    private List<ImageView> mImageViews = new ArrayList<ImageView>();
    private OnActionClickListener mOnActionClickListener;
    private OnActionLongClickListener mOnActionLongClickListner;

    public FloatLabelPlusLayout(Context context) {
        super(context);
    }

    public FloatLabelPlusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatLabelPlusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {

        if (child instanceof ImageView) {
            mImageViews.add((ImageView) child);
            child.setOnClickListener(mOnClickListener);
            child.setOnLongClickListener(mOnLongClickListener);

            // do not hide icon if edittext is already focused
            if (getEditText() != null && !getEditText().isFocused()) {
                child.setVisibility(View.GONE);
            }
        } else if (child instanceof Switch) {
            mInputView = child;
        }

        super.addView(child, index, params);
    }

    @Override
    protected void setEditText(EditText editText) {
        super.setEditText(editText);
        mInputView = editText;
    }

    @Override
    protected void showLabel() {
        super.showLabel();
        if (getEditText().isFocused()) {
            showActions();
        }
    }

    @Override
    public void onFocusChange(View view, boolean focused) {
        super.onFocusChange(view, focused);
        if (!focused) {
            hideActions();
        }
    }

    protected void showActions() {
        for(final ImageView imageView : mImageViews) {
            if (imageView.getVisibility() != View.VISIBLE) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setAlpha(0f);
                imageView.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(null).start();
            }
        }
    }

    protected void hideActions() {
        for(final ImageView imageView : mImageViews) {
            imageView.setAlpha(1f);
            imageView.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            imageView.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        mOnActionClickListener = listener;
    }

    public void setOnActionLongClickListener(OnActionLongClickListener listener) {
        mOnActionLongClickListner = listener;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnActionClickListener != null) {
                Action action = Action.fromTag((String) v.getTag());
                mOnActionClickListener.onActionClick(action, v, mInputView);
            }
        }
    };

    private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnActionLongClickListner != null) {
                Action action = Action.fromTag((String) v.getTag());
                mOnActionLongClickListner.onActionLongClick(action, v, mInputView);
                return true;
            } else if (!TextUtils.isEmpty(v.getContentDescription())) {
                Toast.makeText(getContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
            }

            return false;
        }
    };
}
