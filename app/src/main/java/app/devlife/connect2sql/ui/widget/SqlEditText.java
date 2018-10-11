package app.devlife.connect2sql.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlEditText extends AppCompatEditText {
    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    public OnTextChangedListener onTextChangedListener = null;
    public int updateDelay = 100;
    public boolean dirty = false;

    private static final int COLOR_STRING = 0xFFFF2E00;
    private static final int COLOR_NUMBER = 0xFFA4BD0A;
    private static final int COLOR_KEYWORD = 0xFF0599B0;
    private static final int COLOR_OBJECT = 0xFFd79e39;
    private static final int COLOR_COMMENT = 0xFF808080;

    private static final Pattern line = Pattern.compile(".*\\n");
    private static final Pattern numbers = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private static final Pattern keywords = Pattern.compile(
        "\\b(select|count|from|where|in|and|or|group|"
            + "by|order|limit|top|insert|into|"
            + "inner|right|join|left|outer|on|update|set|values)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern strings = Pattern.compile(
        "\"([^\"]+)\"|\'([^\']+)\'", Pattern.CASE_INSENSITIVE);
    private static final Pattern objects = Pattern.compile(
        "`([^`]+)`", Pattern.CASE_INSENSITIVE);
    private static final Pattern comments = Pattern.compile(
        "/\\*(?:.|[\\n\\r])*?\\*/|//.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern trailingWhiteSpace = Pattern.compile(
        "[\\t ]+$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable e = getText();

            if (onTextChangedListener != null)
                onTextChangedListener.onTextChanged(e.toString());

            highlightWithoutChange(e);
        }
    };
    private boolean modified = true;

    public SqlEditText(Context context) {
        super(context);
        init();
    }

    public SqlEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public String getCleanText() {
        return trailingWhiteSpace.matcher(getText()).replaceAll("");
    }

    public void refresh() {
        highlightWithoutChange(getText());
    }

    private void init() {
        setHorizontallyScrolling(true);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                cancelUpdate();

                if (!modified)
                    return;

                dirty = true;
                updateHandler.postDelayed(updateRunnable, updateDelay);
            }
        });
    }

    private void cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }

    private void highlightWithoutChange(Editable e) {
        modified = false;
        highlight(e);
        modified = true;
    }

    private Editable highlight(Editable e) {
        try {
            // don't use e.clearSpans() because it will remove
            // too much
            clearSpans(e);

            if (e.length() == 0)
                return e;

            for (Matcher m = numbers.matcher(e); m.find(); )
                e.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(),
                    m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = keywords.matcher(e); m.find(); )
                e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(),
                    m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = strings.matcher(e); m.find(); )
                e.setSpan(new ForegroundColorSpan(COLOR_STRING), m.start(),
                    m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = objects.matcher(e); m.find(); )
                e.setSpan(new ForegroundColorSpan(COLOR_OBJECT), m.start(),
                    m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = comments.matcher(e); m.find(); )
                e.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(),
                    m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception ignore) {
        }

        return e;
    }

    private void clearSpans(Editable e) {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(0, e.length(),
                ForegroundColorSpan.class);

            for (int n = spans.length; n-- > 0; )
                e.removeSpan(spans[n]);
        }

        // remove background color spans
        {
            BackgroundColorSpan spans[] = e.getSpans(0, e.length(),
                BackgroundColorSpan.class);

            for (int n = spans.length; n-- > 0; )
                e.removeSpan(spans[n]);
        }
    }
}