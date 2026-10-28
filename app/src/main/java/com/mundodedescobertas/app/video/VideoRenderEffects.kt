package com.mundodedescobertas.app.video

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.OverlaySettings
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.StaticOverlaySettings

/** The preview and exported file use the same color transforms and sticker geometry. */
@OptIn(UnstableApi::class)
object VideoRenderEffects {
    private const val EMOJI_SIZE = 256
    private val emojiBitmaps = mutableMapOf<VideoEmoji, Bitmap>()

    fun colorEffects(color: VideoColor): List<Effect> = when (color) {
        VideoColor.ORIGINAL -> emptyList()
        VideoColor.MONO -> listOf(RgbFilter.createGrayscaleFilter())
        VideoColor.WARM -> listOf(rgb(1.14f, 1.02f, .82f))
        VideoColor.COOL -> listOf(rgb(.82f, 1.02f, 1.14f))
        VideoColor.PINK -> listOf(rgb(1.12f, .84f, 1.08f))
    }

    fun videoEffects(project: VideoProject): List<Effect> = buildList {
        addAll(colorEffects(project.color))
        if (project.stickers.isNotEmpty()) {
            add(OverlayEffect(project.stickers.take(VideoLimits.MAX_STICKERS).map { sticker ->
                object : BitmapOverlay() {
                    private var videoWidth = project.source.width.coerceAtLeast(1)
                    override fun configure(videoSize: Size) { videoWidth = videoSize.width }
                    override fun getBitmap(presentationTimeUs: Long): Bitmap = stickerBitmap(sticker.emoji)
                    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
                        val timeMs = presentationTimeUs / 1_000
                        val visible = timeMs >= sticker.startMs && timeMs < sticker.endMs
                        val fraction = sticker.sizeFraction.coerceIn(.08f, .4f)
                        val scale = fraction * videoWidth / EMOJI_SIZE
                        return StaticOverlaySettings.Builder()
                            .setAlphaScale(if (visible) 1f else 0f)
                            .setBackgroundFrameAnchor(
                                sticker.centerX.coerceIn(0f, 1f) * 2 - 1,
                                1 - sticker.centerY.coerceIn(0f, 1f) * 2,
                            )
                            .setScale(scale, scale)
                            .build()
                    }
                }
            }))
        }
    }

    @Synchronized
    fun stickerBitmap(emoji: VideoEmoji): Bitmap = emojiBitmaps.getOrPut(emoji) {
        Bitmap.createBitmap(EMOJI_SIZE, EMOJI_SIZE, Bitmap.Config.ARGB_8888).also { bitmap ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = EMOJI_SIZE * .78f
                typeface = Typeface.DEFAULT
            }
            val baseline = EMOJI_SIZE / 2f - (paint.descent() + paint.ascent()) / 2f
            Canvas(bitmap).drawText(emoji.glyph, EMOJI_SIZE / 2f, baseline, paint)
        }
    }

    private fun rgb(red: Float, green: Float, blue: Float): RgbAdjustment =
        RgbAdjustment.Builder().setRedScale(red).setGreenScale(green).setBlueScale(blue).build()
}
