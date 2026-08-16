package com.systematics.photocleaner.swipedelete.data.datasource

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.systematics.photocleaner.swipedelete.BuildConfig
import com.systematics.photocleaner.swipedelete.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SharedPreferencesDataSource(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    fun string(key: String, default: String) = preferences.getString(key, default) ?: default
    fun boolean(key: String, default: Boolean) = preferences.getBoolean(key, default)
    fun putString(key: String, value: String) = preferences.edit { putString(key, value) }
    fun putBoolean(key: String, value: Boolean) = preferences.edit { putBoolean(key, value) }

    private companion object { const val PREFERENCES_FILE = "sample_pref" }
}

class FirebaseConfigDataSource {
    private val remoteConfig: FirebaseRemoteConfig? by lazy { runCatching { FirebaseRemoteConfig.getInstance() }.getOrNull() }

    suspend fun fetchAndActivate() = withContext(Dispatchers.IO) {
        val config = remoteConfig ?: return@withContext
        withTimeoutOrNull(FETCH_TIMEOUT_MS) {
            config.setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else DEFAULT_FETCH_INTERVAL_SECONDS
            }).await()
            config.setDefaultsAsync(R.xml.remote_config_defaults).await()
            config.fetchAndActivate().await()
        }
    }

    fun long(key: String) = remoteConfig?.getLong(key)?.toInt() ?: 0
    fun rawLong(key: String) = remoteConfig?.getLong(key) ?: 0L
    fun string(key: String) = remoteConfig?.getString(key).orEmpty()
    fun double(key: String) = remoteConfig?.getDouble(key) ?: 0.0
    fun boolean(key: String, default: Boolean) = if (remoteConfig?.all?.containsKey(key) == true) {
        remoteConfig?.getBoolean(key) ?: default
    } else {
        default
    }
    fun containsKey(key: String) = remoteConfig?.all?.keys?.contains(key) == true

    private companion object {
        const val FETCH_TIMEOUT_MS = 6_000L
        const val DEFAULT_FETCH_INTERVAL_SECONDS = 3_600L
    }
}
