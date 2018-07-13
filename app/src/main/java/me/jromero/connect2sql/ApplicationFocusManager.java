package me.jromero.connect2sql;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import me.jromero.connect2sql.log.EzLogger;

/**
 *
 */
public class ApplicationFocusManager {

    private boolean mFocused = true;
    private Set<OnFocusChangeListener> mOnFocusChangeListeners = new HashSet<>();
    private WeakReference<Activity> mLastFocusedActivity;

    public ApplicationFocusManager(Application application) {
        application.registerComponentCallbacks(mComponentCallbacks2);
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    }

    private synchronized void onFocusUpdate(boolean focused) {
        if (mFocused != focused) {
            EzLogger.i("Application focus changed to: " + focused);
            for (OnFocusChangeListener onFocusChangeListener : mOnFocusChangeListeners) {
                onFocusChangeListener.onApplicationFocusChange(focused);
            }

            mFocused = focused;
        }
    }

    @Nullable
    public WeakReference<Activity> getLastFocusedActivity() {
        return mLastFocusedActivity;
    }

    public void addOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        mOnFocusChangeListeners.add(onFocusChangeListener);
    }

    public void removeFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        mOnFocusChangeListeners.remove(onFocusChangeListener);
    }

    public interface OnFocusChangeListener {
        void onApplicationFocusChange(boolean focused);
    }

    private ComponentCallbacks2 mComponentCallbacks2 = new ComponentCallbacks2() {
        @Override
        public void onTrimMemory(int level) {
            EzLogger.d("[onTrimMemory] level=" + level);
            if (level == TRIM_MEMORY_UI_HIDDEN) {
                onFocusUpdate(false);
            }
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {}

        @Override
        public void onLowMemory() {}
    };

    private ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            EzLogger.d("[onActivityResumed] activity=" + activity);
            mLastFocusedActivity = new WeakReference<Activity>(activity);
            onFocusUpdate(true);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}
