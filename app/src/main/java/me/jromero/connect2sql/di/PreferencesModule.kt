package me.jromero.connect2sql.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import me.jromero.connect2sql.prefs.UserPreferences

@Module
class PreferencesModule(val context: Context) {

    @Provides
    fun providePreferences(): UserPreferences {
        return UserPreferences(context)
    }
}