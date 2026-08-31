package com.rohit.jobtracker.android.network

import android.content.Context
import android.content.SharedPreferences
import com.rohit.jobtracker.android.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServerPreset(
    val label: String,
    val url: String
)

class ServerConfig(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("server_config", Context.MODE_PRIVATE)

    private val _baseUrl = MutableStateFlow(
        prefs.getString("base_url", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.API_BASE_URL
    )
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _apiKey = MutableStateFlow(
        prefs.getString("api_key", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.API_KEY
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    fun getBaseUrl(): String = _baseUrl.value

    fun getApiKey(): String = _apiKey.value

    fun updateConfig(url: String, key: String? = null) {
        val trimmedUrl = url.trim().removeSuffix("/")
        prefs.edit().putString("base_url", trimmedUrl).apply()
        _baseUrl.value = trimmedUrl

        if (key != null) {
            val trimmedKey = key.trim()
            prefs.edit().putString("api_key", trimmedKey).apply()
            _apiKey.value = trimmedKey
        }
    }

    companion object {
        val PRESETS = listOf(
            ServerPreset("Wi-Fi IP", "http://192.168.0.164:8080"),
            ServerPreset("USB (127.0.0.1)", "http://127.0.0.1:8080"),
            ServerPreset("Emulator", "http://10.0.2.2:8080"),
            ServerPreset("Cloud (Railway)", "https://job-tracker-api.up.railway.app")
        )
    }
}
