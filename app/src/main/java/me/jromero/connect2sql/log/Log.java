package me.jromero.connect2sql.log;

import com.gitlab.connect2sql.BuildConfig;

public class Log {

    public static void v(String tag, String message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        android.util.Log.i(tag, message);
    }

    public static void w(String tag, String message) {
        android.util.Log.w(tag, message);
    }

    public static void w(String tag, String message, Throwable e) {
        android.util.Log.w(tag, message, e);
    }

    public static void e(String tag, Throwable e) {
        android.util.Log.e(tag, e.getMessage(), e);
    }

    public static void e(String tag, String message) {
        android.util.Log.e(tag, message);
    }

    public static void e(String tag, String message, Throwable e) {
        android.util.Log.e(tag, message, e);
    }
}
