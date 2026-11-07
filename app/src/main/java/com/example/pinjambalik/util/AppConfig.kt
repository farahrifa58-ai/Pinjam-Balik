package com.example.pinjambalik.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pinjambalik_config")

object AppConfig {
    const val APP_NAME = "PinjamBalik"
    const val APP_VERSION = "1.0.0 (Build Stable)"
    const val DEVELOPER_NAME = "Rafline Dev"
    const val CONTACT_EMAIL = "rafline.dev@gmail.com"
    const val TRIAL_DURATION_SECONDS = 60 * 60L // 60 minutes

    // Build configuration flag from Gradle build parameter
    val isFullVersionEdition: Boolean = try {
        BuildConfig.IS_FULL_VERSION_EDITION
    } catch (_: Throwable) {
        false
    }

    private val KEY_TRIAL_USED_SECONDS = longPreferencesKey("trial_used_seconds")
    private val KEY_TRIAL_EXPIRED = booleanPreferencesKey("trial_expired")
    private val KEY_RETENTION_DAYS = longPreferencesKey("retention_days")
    private val KEY_IS_FULL_VERSION = booleanPreferencesKey("is_full_version")
    private val KEY_APP_THEME = stringPreferencesKey("app_theme")
    private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")

    fun getAppTheme(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_APP_THEME] ?: "LIGHT" // Default is Light (Terang)
        }
    }

    suspend fun setAppTheme(context: Context, theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = theme
        }
    }

    fun getAppLanguage(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_APP_LANGUAGE] ?: "id" // Default is Bahasa Indonesia
        }
    }

    suspend fun setAppLanguage(context: Context, language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language
        }
    }

    fun isFullVersion(context: Context): Flow<Boolean> {
        if (isFullVersionEdition) {
            return flowOf(true)
        }
        return context.dataStore.data.map { preferences ->
            preferences[KEY_IS_FULL_VERSION] ?: false
        }
    }

    suspend fun setFullVersion(context: Context, isFull: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_FULL_VERSION] = isFull
        }
    }

    fun getRetentionDays(context: Context): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_RETENTION_DAYS] ?: 60L // default 60 days
        }
    }

    suspend fun setRetentionDays(context: Context, days: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RETENTION_DAYS] = days
        }
    }

    fun getTrialUsedSeconds(context: Context): Flow<Long> {
        if (isFullVersionEdition) {
            return flowOf(0L)
        }
        return context.dataStore.data.map { preferences ->
            preferences[KEY_TRIAL_USED_SECONDS] ?: 0L
        }
    }

    suspend fun incrementTrialTime(context: Context, deltaSeconds: Long = 1L): Long {
        if (isFullVersionEdition) return 0L

        var current: Long = 0
        context.dataStore.edit { preferences ->
            val used = preferences[KEY_TRIAL_USED_SECONDS] ?: 0L
            val updated = used + deltaSeconds
            preferences[KEY_TRIAL_USED_SECONDS] = updated
            if (updated >= TRIAL_DURATION_SECONDS) {
                preferences[KEY_TRIAL_EXPIRED] = true
            }
            current = updated
        }
        return current
    }

    // Trial mode is active only on Trial edition and when not full version
    fun isTrialModeActive(context: Context): Flow<Boolean> {
        if (isFullVersionEdition) {
            return flowOf(false)
        }
        return context.dataStore.data.map { preferences ->
            val full = preferences[KEY_IS_FULL_VERSION] ?: false
            !full
        }
    }
}
