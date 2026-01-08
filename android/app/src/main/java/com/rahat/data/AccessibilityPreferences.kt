package com.rahat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccessibilityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("accessibility_prefs", Context.MODE_PRIVATE)

    private val _isNarratorEnabled = MutableStateFlow(prefs.getBoolean(KEY_NARRATOR_ENABLED, false))
    val isNarratorEnabled: StateFlow<Boolean> = _isNarratorEnabled.asStateFlow()

    private val _narratorVolume = MutableStateFlow(prefs.getFloat(KEY_NARRATOR_VOLUME, 1.0f))
    val narratorVolume: StateFlow<Float> = _narratorVolume.asStateFlow()

    companion object {
        private const val KEY_NARRATOR_ENABLED = "narrator_enabled"
        private const val KEY_NARRATOR_VOLUME = "narrator_volume"
    }

    fun setNarratorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NARRATOR_ENABLED, enabled).apply()
        _isNarratorEnabled.value = enabled
    }

    fun setNarratorVolume(volume: Float) {
        prefs.edit().putFloat(KEY_NARRATOR_VOLUME, volume).apply()
        _narratorVolume.value = volume
    }
}
