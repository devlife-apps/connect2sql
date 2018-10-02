package app.devlife.connect2sql.di;

import javax.inject.Singleton;

import app.devlife.connect2sql.Connect2SqlApplication;
import app.devlife.connect2sql.activity.DashboardActivity;
import app.devlife.connect2sql.activity.LaunchActivity;
import app.devlife.connect2sql.ui.browse.BrowseFragment;
import app.devlife.connect2sql.ui.connection.ConnectionInfoEditorActivity;
import app.devlife.connect2sql.ui.history.HistoryFragment;
import app.devlife.connect2sql.ui.hostkeys.HostKeysActivity;
import app.devlife.connect2sql.ui.lock.SetLockActivity;
import app.devlife.connect2sql.ui.lock.UnlockActivity;
import app.devlife.connect2sql.ui.query.QueryActivity;
import app.devlife.connect2sql.ui.quickkeys.QuickKeysFragment;
import app.devlife.connect2sql.ui.results.ResultsActivity;
import app.devlife.connect2sql.ui.savedqueries.SavedQueryFragment;
import dagger.Component;

@Singleton
@Component(modules = {
    AnalyticsModule.class,
    ApplicationModule.class,
    ConnectionModule.class,
    DatabaseModule.class,
    PreferencesModule.class,
    SecurityModule.class,
    ViewModelModule.class
})
public interface ApplicationComponent {

    // application
    void inject(Connect2SqlApplication application);

    // activities / fragments
    void inject(BrowseFragment fragment);

    void inject(ConnectionInfoEditorActivity activity);

    void inject(DashboardActivity activity);

    void inject(HistoryFragment fragment);

    void inject(HostKeysActivity activity);

    void inject(LaunchActivity activity);

    void inject(QueryActivity activity);

    void inject(QuickKeysFragment fragment);

    void inject(ResultsActivity activity);

    void inject(SavedQueryFragment fragment);

    void inject(SetLockActivity activity);

    void inject(UnlockActivity activity);
}
