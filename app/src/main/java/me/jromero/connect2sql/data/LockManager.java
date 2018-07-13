package me.jromero.connect2sql.data;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import me.jromero.connect2sql.ui.lock.ForgotLockActivity;
import me.jromero.connect2sql.ui.lock.SetLockActivity;
import me.jromero.connect2sql.ui.lock.UnlockActivity;

/**
 *
 */
public class LockManager {

    private static final String KEY = "LOCK_KEY";
    private static final Class<? extends Activity> ACTIVITY_UNLOCK = UnlockActivity.class;
    private static final Class<? extends Activity> ACTIVITY_SET_LOCK = SetLockActivity.class;
    private static final Class<? extends Activity> ACTIVITY_FORGOT = ForgotLockActivity.class;
    private SharedPreferences mSharedPreferences;

    public LockManager(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    @Nullable
    public String getPassphrase() {
        return mSharedPreferences.getString(KEY, null);
    }

    public boolean isPassphraseSet() {
        return getPassphrase() != null;
    }

    public void setPassphrase(@Nullable String passphrase) {
        mSharedPreferences.edit().putString(KEY, passphrase).apply();
    }

    public boolean isUnlockActivity(Activity activity) {
        return activity.getClass().equals(ACTIVITY_UNLOCK);
    }

    public boolean isForgotLockActivity(Activity activity) {
        return activity.getClass().equals(ACTIVITY_FORGOT);
    }

    public void startForgotLockActivity(Activity activity) {
        activity.startActivity(new Intent(activity, ACTIVITY_FORGOT));
    }

    /**
     * Start unlock activity. Should implement {@link Activity#onActivityResult(int, int, Intent)}
     * and use {@link #wasUnlockValid(int)} to validate results.
     *
     * @param activity
     * @param requestCode
     */
    public void startUnlockActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ACTIVITY_UNLOCK);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * @param resultCode provided from {@link Activity#onActivityResult(int, int, Intent)}
     * @return
     */
    public boolean wasUnlockValid(int resultCode) {
        return resultCode == Activity.RESULT_OK;
    }



    public boolean isSetLockActivity(Activity activity) {
        return activity.getClass().equals(ACTIVITY_SET_LOCK);
    }

    /**
     * @param activity
     * @param requestCode
     */
    public void startSetLockActivity(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, ACTIVITY_SET_LOCK), requestCode);
    }

    /**
     *
     * @param resultCode
     * @return
     */
    public boolean wasPatternSet(int resultCode) {
        return resultCode == Activity.RESULT_OK && getPassphrase() != null;
    }
}
