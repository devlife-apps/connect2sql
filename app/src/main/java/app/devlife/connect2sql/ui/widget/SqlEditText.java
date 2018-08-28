package app.devlife.connect2sql.ui.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

public class SqlEditText extends EditText {
    public interface OnTextChangedListener {
        public void onTextChanged(String text);
    }

    public OnTextChangedListener onTextChangedListener = null;
    public int updateDelay = 1000;
    public int errorLine = 0;
    public boolean dirty = false;

    private static final int COLOR_ERROR = 0xFFFF2E00;
    private static final int COLOR_NUMBER = 0xFFA4BD0A;
    private static final int COLOR_KEYWORD = 0xFF0599B0;
    private static final int COLOR_OBJECTS = 0xFFd79e39;
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

    public void setTextHighlighted(CharSequence text) {
        cancelUpdate();

        errorLine = 0;
        dirty = false;

        modified = false;
        setText(highlight(new SpannableStringBuilder(text)));
        modified = true;

        if (onTextChangedListener != null)
            onTextChangedListener.onTextChanged(text.toString());
    }

    public String getCleanText() {
        return trailingWhiteSpace.matcher(getText()).replaceAll("");
    }

    public void refresh() {
        highlightWithoutChange(getText());
    }

    private void init() {
        setHorizontallyScrolling(true);

        setFilters(new InputFilter[] { new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                    Spanned dest, int dstart, int dend) {
                if (modified && end - start == 1 && start < source.length()
                        && dstart < dest.length()) {
                    char c = source.charAt(start);

                    if (c == '\n')
                        return autoIndent(source, start, end, dest, dstart,
                                dend);
                }

                return source;
            }
        } });

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

            if (errorLine > 0) {
                Matcher m = line.matcher(e);

                for (int n = errorLine; n-- > 0 && m.find();)
                    ;

                e.setSpan(new BackgroundColorSpan(COLOR_ERROR), m.start(),
                        m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            for (Matcher m = numbers.matcher(e); m.find();)
                e.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(),
                        m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = keywords.matcher(e); m.find();)
                e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(),
                        m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = strings.matcher(e); m.find();)
                e.setSpan(new ForegroundColorSpan(COLOR_ERROR), m.start(),
                        m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = objects.matcher(e); m.find();)
                e.setSpan(new ForegroundColorSpan(COLOR_OBJECTS), m.start(),
                        m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            for (Matcher m = comments.matcher(e); m.find();)
                e.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(),
                        m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception ex) {
        }

        return e;
    }

    private void clearSpans(Editable e) {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(0, e.length(),
                    ForegroundColorSpan.class);

            for (int n = spans.length; n-- > 0;)
                e.removeSpan(spans[n]);
        }

        // remove background color spans
        {
            BackgroundColorSpan spans[] = e.getSpans(0, e.length(),
                    BackgroundColorSpan.class);

            for (int n = spans.length; n-- > 0;)
                e.removeSpan(spans[n]);
        }
    }

    private CharSequence autoIndent(CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend) {
        String indent = "";
        int istart = dstart - 1;
        int iend = -1;

        // find start of this line
        boolean dataBefore = false;
        int pt = 0;

        for (; istart > -1; --istart) {
            char c = dest.charAt(istart);

            if (c == '\n')
                break;

            if (c != ' ' && c != '\t') {
                if (!dataBefore) {
                    // indent always after those characters
                    if (c == '{' || c == '+' || c == '-' || c == '*'
                            || c == '/' || c == '%' || c == '^' || c == '=')
                        --pt;

                    dataBefore = true;
                }

                // parenthesis counter
                if (c == '(')
                    --pt;
                else if (c == ')')
                    ++pt;
            }
        }

        // copy indent of this line into the next
        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);

            for (iend = ++istart; iend < dend; ++iend) {
                char c = dest.charAt(iend);

                // auto expand comments
                if (charAtCursor != '\n' && c == '/' && iend + 1 < dend
                        && dest.charAt(iend) == c) {
                    iend += 2;
                    break;
                }

                if (c != ' ' && c != '\t')
                    break;
            }

            indent += dest.subSequence(istart, iend);
        }

        // add new indent
        if (pt < 0)
            indent += "\t";

        // append white space of previous line and new indent
        return source + indent;
    }
}