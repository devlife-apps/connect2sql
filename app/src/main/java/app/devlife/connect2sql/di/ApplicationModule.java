package app.devlife.connect2sql.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import app.devlife.connect2sql.ApplicationFocusManager;

/**
 *
 */
@Module
public class ApplicationModule {

    private Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    public ApplicationFocusManager provideApplicationFocusManager() {
        return new ApplicationFocusManager(mApplication);
    }
}
