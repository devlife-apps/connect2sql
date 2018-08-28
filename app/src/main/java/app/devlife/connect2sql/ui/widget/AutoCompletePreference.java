package app.devlife.connect2sql.ui.widget;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class AutoCompletePreference extends EditTextPreference {

    private static AutoCompleteTextView mEditText = null;

    public AutoCompletePreference(Context context) {
        super(context);
        mEditText = new AutoCompleteTextView(context);
        init(context);
    }

    public AutoCompletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEditText = new AutoCompleteTextView(context);
        init(context);
    }

    public AutoCompletePreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        mEditText = new AutoCompleteTextView(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mEditText.setThreshold(0);
        //The adapter of your choice
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        mEditText.setAdapter(adapter);
    }

    private static final String[] COUNTRIES = new String[] { "Belgium",
            "France", "Italy", "Germany", "Spain" };

    @Override
    protected void onBindDialogView(View view) {
        mEditText.setText(getText());

        ViewParent oldParent = mEditText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(mEditText);
            }
            onAddEditTextToDialogView(view, mEditText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }
}