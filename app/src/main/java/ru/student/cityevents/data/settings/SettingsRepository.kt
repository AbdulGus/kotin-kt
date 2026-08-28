package ru.student.cityevents.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(val city: String = "Berlin", val darkTheme: Boolean = false)

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val cityKey = stringPreferencesKey("city")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            city = preferences[cityKey] ?: "Berlin",
            darkTheme = preferences[darkThemeKey] ?: false
        )
    }

    suspend fun saveCity(city: String) {
        context.dataStore.edit { it[cityKey] = city.trim().ifBlank { "Berlin" } }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[darkThemeKey] = enabled }
    }
}
