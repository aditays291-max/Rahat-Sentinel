package com.rahat.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class Narrator(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("Narrator", "Language not supported")
            } else {
                isReady = true
            }
        } else {
            Log.e("Narrator", "Initialization failed")
        }
    }

    fun setVolume(volume: Float) {
        // TTS volume typically ranges from 0.0 to 1.0
        // We can pass it in params or just store it for future speak calls
    }

    fun speak(text: String, volume: Float = 1.0f) {
        if (isReady) {
            val params = android.os.Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
        }
    }

    fun speakIfEnabled(text: String, isEnabled: Boolean, volume: Float) {
        if (isEnabled && isReady) {
            speak(text, volume)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
