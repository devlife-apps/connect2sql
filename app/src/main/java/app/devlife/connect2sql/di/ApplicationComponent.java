package app.devlife.connect2sql.di;

import javax.inject.Singleton;

import app.devlife.connect2sql.ui.connection.ConnectionInfoEditorActivity;
import app.devlife.connect2sql.ui.history.QueryHistoryActivity;
import app.devlife.connect2sql.ui.lock.SetLockActivity;
import app.devlife.connect2sql.ui.lock.UnlockActivity;
import app.devlife.connect2sql.ui.query.QueryActivity;
import app.devlife.connect2sql.ui.results.ResultsActivity;
import dagger.Component;
import app.devlife.connect2sql.Connect2SqlApplication;
import app.devlife.connect2sql.activity.DashboardActivity;
import app.devlife.connect2sql.activity.LaunchActivity;
import app.devlife.connect2sql.ui.connection.ConnectionInfoEditorActivity;
import app.devlife.connect2sql.ui.history.QueryHistoryActivity;
import app.devlife.connect2sql.ui.lock.SetLockActivity;
import app.devlife.connect2sql.ui.lock.UnlockActivity;
import app.devlife.connect2sql.ui.query.QueryActivity;
import app.devlife.connect2sql.ui.results.ResultsActivity;
import app.devlife.connect2sql.ui.savedqueries.SavedQueriesActivity;

/**
 *
 */
@Singleton
@Component(modules = {AnalyticsModule.class, ApplicationModule.class, ConnectionModule.class, DatabaseModule.class, PreferencesModule.class, SecurityModule.class})
public interface ApplicationComponent {

    // application
    void inject(Connect2SqlApplication application);

    // activities
    void inject(LaunchActivity activity);
    void inject(DashboardActivity activity);
    void inject(ConnectionInfoEditorActivity activity);
    void inject(QueryActivity activity);
    void inject(QueryHistoryActivity activity);
    void inject(ResultsActivity activity);
    void inject(UnlockActivity activity);
    void inject(SetLockActivity activity);
    void inject(SavedQueriesActivity activity);
}
