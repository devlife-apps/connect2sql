package app.devlife.connect2sql;

import android.content.Context;
import android.content.Intent;

/**
 *
 */
public class ApplicationUtils {
    public static Connect2SqlApplication getApplication(Context context) {
        return ((Connect2SqlApplication) context.getApplicationContext());
    }

    public static void backgroundApp(Context context) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(startMain);
    }
}
