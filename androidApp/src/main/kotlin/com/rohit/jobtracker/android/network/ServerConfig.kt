package com.rohit.jobtracker.android.network

import android.content.Context
import android.content.SharedPreferences
import com.rohit.jobtracker.android.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServerConfig(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("server_config", Context.MODE_PRIVATE)

    private val _baseUrl = MutableStateFlow(
        prefs.getString("base_url", null) ?: BuildConfig.API_BASE_URL
    )
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    fun getBaseUrl(): String = _baseUrl.value

    fun setBaseUrl(url: String) {
        val trimmed = url.trim().removeSuffix("/")
        prefs.edit().putString("base_url", trimmed).apply()
        _baseUrl.value = trimmed
    }
}
