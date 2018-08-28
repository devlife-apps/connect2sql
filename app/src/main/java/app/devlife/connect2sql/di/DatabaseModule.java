package app.devlife.connect2sql.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import app.devlife.connect2sql.data.LockManager;
import app.devlife.connect2sql.db.AppDatabaseHelperV3;
import app.devlife.connect2sql.db.model.connection.ConnectionInfoSqlModel;
import app.devlife.connect2sql.db.model.query.BuiltInQuery.BuiltInQuerySqlModel;
import app.devlife.connect2sql.db.model.query.HistoryQuery.HistoryQuerySqlModel;
import app.devlife.connect2sql.db.model.query.SavedQuery.SavedQuerySqlModel;
import app.devlife.connect2sql.db.provider.ContentUriHelper;
import app.devlife.connect2sql.db.repo.ConnectionInfoRepository;
import app.devlife.connect2sql.db.repo.HistoryQueryRepository;
import app.devlife.connect2sql.db.repo.SavedQueryRepository;
import app.devlife.connect2sql.log.EzLogger;


@Module
public class DatabaseModule {

    private final Context mApplicationContext;

    public DatabaseModule(Context context) {
        mApplicationContext = context.getApplicationContext();
    }

    @Provides
    @Singleton
    public ConnectionInfoSqlModel providesConnectionInfoSqlModel() {
        return new ConnectionInfoSqlModel();
    }

    @Provides
    @Singleton
    public BuiltInQuerySqlModel providesBuiltInQuerySqlModel() {
        return new BuiltInQuerySqlModel();
    }

    @Provides
    @Singleton
    public SavedQuerySqlModel providesSavedQuerySqlModel() {
        return new SavedQuerySqlModel();
    }

    @Provides
    @Singleton
    public HistoryQuerySqlModel providesHistoryQuerySqlModel() {
        return new HistoryQuerySqlModel();
    }

    @Provides
    @Singleton
    public AppDatabaseHelperV3 providesAppDatabaseHelper(LockManager lockManager,
                                                         BuiltInQuerySqlModel builtInQuerySqlModel,
                                                         ConnectionInfoSqlModel connectionInfoSqlModel,
                                                         HistoryQuerySqlModel historyQuerySqlModel,
                                                         SavedQuerySqlModel savedQuerySqlModel
    ) {
        return new AppDatabaseHelperV3(mApplicationContext, lockManager, builtInQuerySqlModel,
                connectionInfoSqlModel,
                historyQuerySqlModel,
                savedQuerySqlModel);
    }

    @Provides
    public ConnectionInfoRepository provideConnectionInfoRepository(ConnectionInfoSqlModel connectionInfoSqlModel) {
        try {
            return new ConnectionInfoRepository(mApplicationContext.getContentResolver(), connectionInfoSqlModel);
        } catch (ContentUriHelper.BaseUriNotFoundException e) {
            EzLogger.e(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Provides
    public HistoryQueryRepository providesHistoryQueryRepository() {
        try {
            return new HistoryQueryRepository(mApplicationContext.getContentResolver());
        } catch (ContentUriHelper.BaseUriNotFoundException e) {
            EzLogger.e(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Provides
    public SavedQueryRepository providesSavedQueryRepository(SavedQuerySqlModel savedQuerySqlModel) {
        try {
            return new SavedQueryRepository(mApplicationContext.getContentResolver(), savedQuerySqlModel);
        } catch (ContentUriHelper.BaseUriNotFoundException e) {
            EzLogger.e(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
