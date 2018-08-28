package app.devlife.connect2sql.di;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import app.devlife.connect2sql.data.LockManager;

/**
 *
 */
@Module
public class SecurityModule {

    private final SharedPreferences mSharedPreferences;

    public SecurityModule(Context context) {
        mSharedPreferences = context.getApplicationContext().getSharedPreferences("lock.prefs", Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    public LockManager provideLockManager() {
        return new LockManager(mSharedPreferences);
    }
}
