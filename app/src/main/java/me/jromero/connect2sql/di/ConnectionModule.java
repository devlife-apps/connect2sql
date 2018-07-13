package me.jromero.connect2sql.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.jromero.connect2sql.connection.ConnectionAgent;
import me.jromero.connect2sql.sql.driver.agent.DriverAgent;

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
