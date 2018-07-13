package me.jromero.connect2sql.di

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import javax.inject.Singleton

/**
 *
 */
@Module
class AnalyticsModule(val context: Context) {

    @Provides
    @Singleton
    public fun provideCrashlytics(): Crashlytics {
        return Crashlytics()
    }

    @Provides
    @Singleton
    public fun provideFabric(crashlytics: Crashlytics): Fabric {
        return Fabric.with(context.applicationContext, crashlytics)
    }

    @Provides
    @Singleton
    public fun provideMixPanel(): MixpanelAPI {
        return MixpanelAPI.getInstance(context.applicationContext, "DUMMY_VALUE")
    }
}