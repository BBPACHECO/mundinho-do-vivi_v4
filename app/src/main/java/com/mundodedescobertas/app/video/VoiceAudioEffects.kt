package com.mundodedescobertas.app.video

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.audio.ToInt16PcmAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Voice processing preserves playback speed and applies to the entire selected audio track. */
@OptIn(UnstableApi::class)
object VoiceAudioEffects {
    fun audioProcessors(effect: VoiceEffect, volume: Float): List<AudioProcessor> = buildList {
        add(ToInt16PcmAudioProcessor())
        when (effect) {
            VoiceEffect.BABY, VoiceEffect.DEEP -> add(SonicAudioProcessor().apply {
                setSpeed(1f)
                setPitch(effect.pitch)
            })
            VoiceEffect.ROBOT -> add(VideoPcmEffect(volume = 1f, robot = true))
            VoiceEffect.ORIGINAL -> Unit
        }
        if (volume < 1f) add(VideoPcmEffect(volume.coerceIn(0f, 1f), robot = false))
    }
}

/** A bounded PCM16 effect. The modulation phase advances per frame, not per channel or buffer. */
@OptIn(UnstableApi::class)
internal class VideoPcmEffect(private val volume: Float, private val robot: Boolean) : BaseAudioProcessor() {
    private var frame = 0L
    private var channel = 0

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        return inputAudioFormat
    }

    override fun onFlush() { frame = 0; channel = 0 }

    override fun queueInput(inputBuffer: ByteBuffer) {
        inputBuffer.order(ByteOrder.nativeOrder())
        val output = replaceOutputBuffer(inputBuffer.remaining())
        while (inputBuffer.remaining() >= 2) {
            val value = inputBuffer.short.toInt()
            val robotic = if (robot) {
                val polarity = if ((frame * 70 / inputAudioFormat.sampleRate) % 2 == 0L) 1 else -1
                (value / 512 * 512) * polarity
            } else value
            output.putShort((robotic * volume).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
            channel++
            if (channel == inputAudioFormat.channelCount) { channel = 0; frame++ }
        }
        output.flip()
    }
}
