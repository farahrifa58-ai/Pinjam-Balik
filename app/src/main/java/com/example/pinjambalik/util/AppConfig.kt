package com.example.pinjambalik.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pinjambalik_config")

object AppConfig {
    const val CONTACT_EMAIL = "rendykurniawan0311@gmail.com"
    const val TRIAL_DURATION_SECONDS = 60 * 60L // 60 minutes

    // Build configuration flag (default is false for Full, or true when compiled for Trial)
    var isTrialEdition: Boolean = false

    private val KEY_TRIAL_USED_SECONDS = longPreferencesKey("trial_used_seconds")
    private val KEY_TRIAL_EXPIRED = booleanPreferencesKey("trial_expired")
    private val KEY_RETENTION_DAYS = longPreferencesKey("retention_days")
    private val KEY_FORCE_TRIAL_MODE = booleanPreferencesKey("force_trial_mode")

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
        return context.dataStore.data.map { preferences ->
            preferences[KEY_TRIAL_USED_SECONDS] ?: 0L
        }
    }

    suspend fun incrementTrialTime(context: Context, deltaSeconds: Long = 1L): Long {
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

    fun isTrialModeActive(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            isTrialEdition || (preferences[KEY_FORCE_TRIAL_MODE] ?: false)
        }
    }

    suspend fun setForceTrialMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FORCE_TRIAL_MODE] = enabled
        }
    }

    suspend fun resetTrialTimer(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TRIAL_USED_SECONDS] = 0L
            preferences[KEY_TRIAL_EXPIRED] = false
        }
    }
}
