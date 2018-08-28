package app.devlife.connect2sql.ui.connection.form;

import android.widget.EditText;

/**
 * Created by javier.romero on 5/4/14.
 */
public class FormUtils {

    public static boolean hasInputType(EditText editText, int inputType) {
        return hasInputType(editText.getInputType(), inputType);
    }

    public static boolean hasInputType(int currentInputType, int againstInputType) {
        return (currentInputType & againstInputType) == againstInputType;
    }

    public static int addInputType(int currentInputType, int inputTypeToAdd) {
        return (currentInputType |= inputTypeToAdd);
    }

    public static int removeInputType(int currentInputType, int inputTypeToRemove) {
        return (currentInputType &= ~inputTypeToRemove);
    }
}
