package app.devlife.connect2sql;

import android.app.Activity;
import android.app.Application;

import app.devlife.connect2sql.di.AnalyticsModule;
import app.devlife.connect2sql.di.ApplicationComponent;
import app.devlife.connect2sql.di.ApplicationModule;
import app.devlife.connect2sql.di.DaggerApplicationComponent;
import app.devlife.connect2sql.di.DatabaseModule;
import app.devlife.connect2sql.di.PreferencesModule;
import app.devlife.connect2sql.di.SecurityModule;
import io.fabric.sdk.android.Fabric;
import app.devlife.connect2sql.activity.LaunchActivity;
import app.devlife.connect2sql.data.LockManager;
import app.devlife.connect2sql.di.*;
import app.devlife.connect2sql.log.EzLogger;

import javax.inject.Inject;
import java.lang.ref.WeakReference;

/**
 *
 */
public class Connect2SqlApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Inject ApplicationFocusManager mApplicationFocusManager;
    @Inject LockManager mLockManager;
    @Inject Fabric mFabric;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .analyticsModule(new AnalyticsModule(this))
                .applicationModule(new ApplicationModule(this))
                .databaseModule(new DatabaseModule(this))
                .preferencesModule(new PreferencesModule(this))
                .securityModule(new SecurityModule(this))
                .build();

        mApplicationComponent.inject(this);
        mApplicationFocusManager.addOnFocusChangeListener(mOnFocusChangeListener);

        EzLogger.i("Fabric version: " + mFabric.getVersion());
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    private ApplicationFocusManager.OnFocusChangeListener mOnFocusChangeListener = new ApplicationFocusManager.OnFocusChangeListener() {
        @Override
        public void onApplicationFocusChange(boolean focused) {
            if (focused) {
                WeakReference<Activity> lastFocusedActivity = mApplicationFocusManager.getLastFocusedActivity();
                if (lastFocusedActivity != null) {
                    if (lastFocusedActivity.get() != null) {
                        Activity activity = lastFocusedActivity.get();
                        EzLogger.d("Last focused activity: " + activity);
                        if (!activity.getClass().equals(LaunchActivity.class)) {
                            if (!mLockManager.isSetLockActivity(activity) &&
                                    !mLockManager.isUnlockActivity(activity) &&
                                    !mLockManager.isForgotLockActivity(activity)) {
                                mLockManager.startUnlockActivity(activity, 0);
                            } else {
                                EzLogger.d("Activity is a lock specific activity.");
                            }
                        } else {
                            EzLogger.d("Last focused activity was the Launch activity.");
                        }
                    } else {
                        EzLogger.d("Last focused activity has gone away.");
                    }
                } else {
                    EzLogger.d("No reference to last focused activity");
                }
            }
        }
    };
}
