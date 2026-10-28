package com.mundodedescobertas.app.video

import android.net.Uri
import java.io.File
import java.util.UUID

object VideoLimits {
    const val MAX_DURATION_MS = 90_000L
    const val MAX_SOURCE_BYTES = 500L * 1024 * 1024
    const val MAX_STICKERS = 8
    const val MAX_SOUNDS = 12
}

data class VideoSource(
    val file: File,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val hasAudio: Boolean,
) {
    val uri: Uri get() = Uri.fromFile(file)
}

enum class VideoColor(val title: String) {
    ORIGINAL("Original"), WARM("Quentinho"), COOL("Azulzinho"), PINK("Rosinha"), MONO("Preto e branco")
}

enum class VoiceEffect(val title: String, val pitch: Float) {
    ORIGINAL("Original", 1f), BABY("Voz de neném", 1.6f), DEEP("Voz grave", .7f), ROBOT("Robô", 1f)
}

enum class VideoEmoji(val glyph: String, val title: String) {
    HAPPY("😊", "Sorriso"), HEART("💖", "Coração"), STAR("⭐", "Estrela"),
    LAUGH("😂", "Risada"), RAINBOW("🌈", "Arco-íris"), PARTY("🎉", "Festa")
}

data class VideoSticker(
    val emoji: VideoEmoji,
    val centerX: Float = .5f,
    val centerY: Float = .5f,
    val sizeFraction: Float = .18f,
    val startMs: Long = 0,
    val endMs: Long = VideoLimits.MAX_DURATION_MS,
    val id: String = UUID.randomUUID().toString(),
)

data class VideoSound(
    val padSource: String,
    val title: String,
    val startMs: Long,
    val volume: Float = .7f,
    val id: String = UUID.randomUUID().toString(),
)

enum class OverlayCorner(val title: String) {
    TOP_LEFT("Em cima à esquerda"), TOP_RIGHT("Em cima à direita"),
    BOTTOM_LEFT("Embaixo à esquerda"), BOTTOM_RIGHT("Embaixo à direita")
}

data class VideoReaction(
    val file: File,
    val durationMs: Long,
    val startMs: Long = 0,
    val corner: OverlayCorner = OverlayCorner.TOP_RIGHT,
    val widthFraction: Float = .3f,
    val hasAudio: Boolean = false,
) {
    val uri: Uri get() = Uri.fromFile(file)
}

data class VideoProject(
    val source: VideoSource,
    val color: VideoColor = VideoColor.ORIGINAL,
    val voice: VoiceEffect = VoiceEffect.ORIGINAL,
    val stickers: List<VideoSticker> = emptyList(),
    val sounds: List<VideoSound> = emptyList(),
    val reaction: VideoReaction? = null,
    val originalVolume: Float = 1f,
    val reactionVolume: Float = 1f,
)
