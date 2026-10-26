package com.mundodedescobertas.app.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Não aceita vozes que exijam rede; não baixa pacotes de voz nem pede Internet. */
class LocalNarrator(context: Context) {
    private val _available = MutableStateFlow(false)
    val available = _available.asStateFlow()
    private var closed = false
    private var tts: TextToSpeech? = null
    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (!closed && status == TextToSpeech.SUCCESS) {
                val engine = tts
                val voice = engine?.voices?.filter {
                    it.locale.language == "pt" && it.locale.country == "BR" && !it.isNetworkConnectionRequired
                }?.maxByOrNull { it.quality }
                if (voice != null) {
                    engine.voice = voice
                    engine.setSpeechRate(.88f)
                    engine.setPitch(1.03f)
                    _available.value = true
                }
            }
        }
    }
    fun speak(text: String, settings: ChildPreferences) {
        if (!_available.value || !settings.narrationEnabled || settings.effectiveNarrationVolume <= 0f) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, settings.effectiveNarrationVolume)
        }, "vivi-greeting")
    }
    fun stop() { tts?.stop() }
    fun close() { closed = true; _available.value = false; tts?.stop(); tts?.shutdown(); tts = null }
}
