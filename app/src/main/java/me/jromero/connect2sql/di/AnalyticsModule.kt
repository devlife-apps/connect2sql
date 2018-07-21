package me.jromero.connect2sql.di

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.gitlab.connect2sql.BuildConfig
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.Kit
import org.mockito.Mockito.mock
import javax.inject.Singleton

/**
 *
 */
@Module
class AnalyticsModule(val context: Context) {

    companion object {
        private val shouldMock = BuildConfig.DEBUG
    }

    @Provides
    @Singleton
    fun provideAnswers(fabric: Fabric): Answers {
        return if (shouldMock) mock(Answers::class.java) else fabric.getKit(Answers::class.java)
    }

    @Provides
    @Singleton
    fun provideCrashlytics(fabric: Fabric): Crashlytics {
        return if (shouldMock) mock(Crashlytics::class.java) else fabric.getKit(Crashlytics::class.java)
    }

    @Provides
    @Singleton
    fun provideFabric(): Fabric {
        return if (shouldMock) {
            mock(Fabric::class.java)
        } else {
            Fabric.with(Fabric.Builder(context.applicationContext)
                    .kits(Crashlytics(), Answers())
                    .debuggable(true)
                    .build())
        }
    }

    private fun <T : Kit<*>> Fabric.getKit(clazz: Class<T>): T {
        return this.kits.first { clazz.isInstance(it) } as T
    }
}