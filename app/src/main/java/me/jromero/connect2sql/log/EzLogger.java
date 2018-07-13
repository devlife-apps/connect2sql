package me.jromero.connect2sql.log;

public class EzLogger {

    public static void d(String msg) {
        Log.d("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg);
    }

    public static void w(String msg) {
        Log.w("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg);
    }

    public static void w(String msg, Throwable e) {
        Log.w("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg, e);
    }

    public static void e(String msg) {
        Log.e("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg);
    }

    public static void e(String msg, Throwable e) {
        Log.e("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg, e);
    }

    public static void i(String msg) {
        Log.i("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg);
    }

    public static void v(String msg) {
        Log.v("*" + getCallingClass() + "*", getCallingMethod() + "("
                + getCallingLine() + "):" + msg);
    }

    private static String getCallingClass() {
        String className = new Exception().getStackTrace()[2].getClassName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

    private static int getCallingLine() {
        return new Exception().getStackTrace()[2].getLineNumber();
    }

    private static String getCallingMethod() {
        return new Exception().getStackTrace()[2].getMethodName();
    }
}
