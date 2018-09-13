package app.devlife.connect2sql.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gitlab.connect2sql.R;

import app.devlife.connect2sql.log.EzLogger;

public class BlockItem extends FrameLayout {

    private static final String DEFAULT_TITLE = "Title";
    private static final String DEFAULT_SUBTITLE = "Subtitle";
    private static final int DEFAULT_IMAGE = android.R.drawable.sym_def_app_icon;

    private String mTitle;
    private String mSubtitle;
    private int mImageResource;
    private Bundle mData;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        if (mTitle == null) {
            mTitle = DEFAULT_TITLE;
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setVisibility(View.VISIBLE);
        }
        mTitleView.setText(mTitle);
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
        if (mSubtitle == null) {
            mSubtitle = DEFAULT_SUBTITLE;
            mSubtitleView.setVisibility(View.GONE);
        } else {
            mSubtitleView.setVisibility(View.VISIBLE);
        }
        mSubtitleView.setText(mSubtitle);
    }

    public int getImageResource() {
        return mImageResource;
    }

    public void setImageResource(int resource) {
        mImageResource = resource;
        mImageView.setImageResource(mImageResource);
    }

    public Bundle getData() {
        return mData;
    }

    public void setData(Bundle data) {
        mData = data;
    }

    private TextView mTitleView;
    private TextView mSubtitleView;
    private ImageView mImageView;
    private boolean mIsActive;
    private LinearLayout mRoot;

    // Constructor required for inflation from resource file
    public BlockItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
        readAttributes(context, attrs);
    }

    // Constructor required for in-code creation
    public BlockItem(Context context) {
        super(context);

        init(context);
    }

    private void init(Context c) {

        // Inflate the view from the layout resource.
        final LayoutInflater inflater = (LayoutInflater) c
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_block_item, this, true);

        // Get references to the child controls.
        // You have to get the view by tag, because if you have multiple
        // controls in your xml with same "id",
        // then when it saves state, and then restores state, all controls with
        // the same id get the same value
        mRoot = (LinearLayout) findViewWithTag("root");
        mTitleView = (TextView) findViewWithTag("textTitle");
        mSubtitleView = (TextView) findViewWithTag("textSubtitle");
        mImageView = (ImageView) findViewWithTag("imageMain");
    }

    // Read Attributes from the xml
    // custom attributes are defined in attrs.xml as declare-styleable resources
    private void readAttributes(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BlockItem);

        // get/set title
        setTitle(ta.getString(R.styleable.BlockItem_title));

        // get/set subtitle
        setSubtitle(ta.getString(R.styleable.BlockItem_subtitle));

        // get/set image resource
        setImageResource(ta.getResourceId(R.styleable.BlockItem_image, DEFAULT_IMAGE));

        // get/set background resource
        // setBackgroundResource(ta.getResourceId(
        // R.styleable.BlockItem_background, DEFAULT_BACKGROUND));

        // Don't forget this
        ta.recycle();
    }

    @Override
    /**
     *
     */
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putString("mTitleView_text", mTitleView.getText().toString());
        bundle.putString("mSubtitleView_text", mSubtitleView.getText()
            .toString());
        return bundle;
    }

    // Restores CompoundView state
    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mTitleView.setText(bundle.getString("mTitleView_text"));
            mSubtitleView.setText(bundle.getString("mSubtitleView_text"));
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    // Implement this to change child views reposition logic
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean isActivated() {
        return mIsActive;
    }

    @Override
    public void setActivated(boolean active) {
        mIsActive = active;
        // bug workaround
        // Save padding so we can reset it after
        // changing the background resource
        int padTop = mRoot.getPaddingTop();
        int padLeft = mRoot.getPaddingLeft();
        int padRight = mRoot.getPaddingRight();
        int padBottom = mRoot.getPaddingBottom();

        // change resource
        if (active) {
            EzLogger.d("Item selected!");
            mRoot.setBackgroundResource(R.drawable.widget_block_item_bg_active);
            mRoot.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else {
            EzLogger.d("Item deselected!");
            mRoot.setBackgroundResource(R.drawable.widget_block_item_bg);
        }

        // reset padding
        mRoot.setPadding(padLeft, padTop, padRight, padBottom);
    }
}