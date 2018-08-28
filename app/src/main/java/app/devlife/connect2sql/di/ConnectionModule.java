package app.devlife.connect2sql.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import app.devlife.connect2sql.connection.ConnectionAgent;
import app.devlife.connect2sql.sql.driver.agent.DriverAgent;

/**
 *
 */
@Module
public class ConnectionModule {

    @Provides
    @Singleton
    ConnectionAgent provideConnectionAgent() {
        return new ConnectionAgent();
    }
}
