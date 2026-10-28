package com.mundodedescobertas.app.video

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.OverlaySettings
import androidx.media3.common.VideoCompositorSettings
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.mundodedescobertas.app.dj.DjCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.min
import kotlin.math.roundToInt

/** Native decode/composite/encode: the saved MP4 contains the effects, not just a preview. */
@OptIn(UnstableApi::class)
class VideoExporter(context: Context) {
    private val context = context.applicationContext

    suspend fun export(project: VideoProject, onProgress: (Int) -> Unit): File {
        val output = withContext(Dispatchers.IO) {
            require(project.source.file.isFile) { "Escolha um vídeo antes de editar." }
            require(project.source.durationMs in 1..VideoLimits.MAX_DURATION_MS) { "Escolha um vídeo de até 90 segundos." }
            require(project.source.file.length() <= VideoLimits.MAX_SOURCE_BYTES) { "Escolha um vídeo de até 500 MB." }
            require(project.source.width > 0 && project.source.height > 0) { "Este arquivo não possui um vídeo compatível." }
            val directory = File(context.cacheDir, "video-editor/exports").apply { check(mkdirs() || isDirectory) }
            if (directory.usableSpace < 120L * 1024 * 1024) throw IOException("Libere pelo menos 120 MB para preparar seu vídeo.")
            File(directory, "Vivi_${UUID.randomUUID()}.mp4")
        }
        try {
            return withContext(Dispatchers.Main.immediate) {
                ensureActive()
                val composition = buildComposition(project)
                runExport(composition, output, onProgress)
            }
        } catch (error: Throwable) {
            output.delete()
            throw error
        }
    }

    private suspend fun runExport(composition: Composition, output: File, onProgress: (Int) -> Unit): File =
        suspendCancellableCoroutine { continuation ->
            val handler = Handler(Looper.getMainLooper())
            val progress = ProgressHolder()
            var finished = false
            lateinit var transformer: Transformer
            lateinit var poll: Runnable
            fun finish() {
                finished = true
                handler.removeCallbacks(poll)
            }
            transformer = Transformer.Builder(context)
                .setLooper(Looper.getMainLooper())
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .setEncoderFactory(DefaultEncoderFactory.Builder(context)
                    .setRequestedVideoEncoderSettings(VideoEncoderSettings.Builder().setBitrate(4_000_000).build())
                    .build())
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        finish()
                        if (continuation.isActive) {
                            if (output.isFile && output.length() > 0) {
                                onProgress(100)
                                continuation.resume(output)
                            } else continuation.resumeWithException(IOException("O vídeo não ficou pronto. Vamos tentar de novo?"))
                        } else output.delete()
                    }

                    override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                        finish()
                        output.delete()
                        if (continuation.isActive) continuation.resumeWithException(IOException(
                            "Não foi possível preparar este vídeo neste aparelho. Tente um vídeo MP4 menor e sem HDR.",
                            exportException,
                        ))
                    }
                }).build()
            poll = Runnable {
                if (!finished && continuation.isActive) {
                    if (transformer.getProgress(progress) == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress(progress.progress.coerceIn(0, 99))
                    }
                    handler.postDelayed(poll, 200)
                }
            }
            continuation.invokeOnCancellation {
                val cleanup = Runnable {
                    finish()
                    transformer.cancel()
                    output.delete()
                }
                if (Looper.myLooper() == Looper.getMainLooper()) cleanup.run() else handler.post(cleanup)
            }
            if (continuation.isActive) {
                try {
                    onProgress(0)
                    transformer.start(composition, output.absolutePath)
                    handler.postDelayed(poll, 200)
                } catch (error: Exception) {
                    finish()
                    transformer.cancel()
                    output.delete()
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            }
        }

    private fun buildComposition(project: VideoProject): Composition {
        val duration = project.source.durationMs
        val (width, height) = VideoExportGeometry.outputSize(project.source.width, project.source.height)
        val presentation = Presentation.createForWidthAndHeight(width, height, Presentation.LAYOUT_SCALE_TO_FIT)
        val source = EditedMediaItem.Builder(clippedMedia(project.source.uri, duration))
            .setRemoveAudio(!project.source.hasAudio || project.originalVolume <= 0f)
            .setEffects(Effects(
                VoiceAudioEffects.audioProcessors(project.voice, project.originalVolume),
                listOf<Effect>(presentation) + VideoRenderEffects.colorEffects(project.color),
            )).build()
        val sequences = mutableListOf<EditedMediaItemSequence>()
        val reaction = project.reaction?.takeIf {
            it.file.isFile && it.durationMs > 0 && it.startMs >= 0 && it.startMs < duration
        }
        if (reaction != null) {
            // Media3 draws the first video sequence on top and uses its frame timestamps.
            // Transparent images pad that primary sequence so a short reaction neither freezes
            // the background nor truncates the exported movie. Video gaps are not supported.
            val start = reaction.startMs
            val length = min(reaction.durationMs, duration - start)
            val transparent = transparentImage()
            val foreground = EditedMediaItemSequence.Builder()
            if (start > 0) foreground.addItem(transparentItem(transparent, start))
            foreground.addItem(EditedMediaItem.Builder(clippedMedia(reaction.uri, length)).setRemoveAudio(true).build())
            val tail = duration - start - length
            if (tail > 0) foreground.addItem(transparentItem(transparent, tail))
            sequences += foreground.build()
            sequences += EditedMediaItemSequence.Builder(source).build()
            if (reaction.hasAudio && project.reactionVolume > 0f) {
                val audio = EditedMediaItem.Builder(clippedMedia(reaction.uri, length))
                    .setRemoveVideo(true)
                    .setEffects(Effects(VoiceAudioEffects.audioProcessors(project.voice, project.reactionVolume), emptyList()))
                    .build()
                sequences += audioSequence(audio, start)
            }
        } else sequences += EditedMediaItemSequence.Builder(source).build()

        project.sounds.take(VideoLimits.MAX_SOUNDS).forEach { sound ->
            if (sound.startMs !in 0 until duration || sound.volume <= 0f || DjCatalog.pads.none { it.source == sound.padSource }) return@forEach
            val item = EditedMediaItem.Builder(clippedMedia(Uri.parse("asset:///dj/${sound.padSource}.wav"), duration - sound.startMs))
                .setRemoveVideo(true)
                .setEffects(Effects(VoiceAudioEffects.audioProcessors(VoiceEffect.ORIGINAL, sound.volume), emptyList()))
                .build()
            sequences += audioSequence(item, sound.startMs)
        }
        // Stickers are applied after compositing, so they remain above the reaction window.
        val finalEffects = listOf<Effect>(
            Presentation.createForWidthAndHeight(width, height, Presentation.LAYOUT_SCALE_TO_FIT),
            FrameDropEffect.createDefaultFrameDropEffect(30f),
        ) +
            VideoRenderEffects.videoEffects(project.copy(color = VideoColor.ORIGINAL))
        return Composition.Builder(sequences)
            .setEffects(Effects(emptyList(), finalEffects))
            .setHdrMode(Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL)
            .experimentalSetForceAudioTrack(true)
            .apply { if (reaction != null) setVideoCompositorSettings(reactionCompositor(reaction, width, height)) }
            .build()
    }

    private fun clippedMedia(uri: Uri, endMs: Long): MediaItem = MediaItem.Builder().setUri(uri)
        .setClippingConfiguration(MediaItem.ClippingConfiguration.Builder().setEndPositionMs(endMs.coerceAtLeast(1)).build())
        .build()

    private fun audioSequence(item: EditedMediaItem, startMs: Long): EditedMediaItemSequence =
        EditedMediaItemSequence.Builder().apply {
            if (startMs > 0) addGap(startMs * 1_000)
            addItem(item)
        }.build()

    private fun transparentItem(file: File, durationMs: Long): EditedMediaItem = EditedMediaItem.Builder(
        MediaItem.Builder().setUri(Uri.fromFile(file)).setImageDurationMs(durationMs).build(),
    ).setRemoveAudio(true).setFrameRate(30).build()

    private fun transparentImage(): File {
        val file = File(context.cacheDir, "video-editor/transparent.png")
        if (!file.isFile || file.length() == 0L) {
            file.parentFile?.mkdirs()
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            try { file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } }
            finally { bitmap.recycle() }
        }
        return file
    }

    private fun reactionCompositor(reaction: VideoReaction, width: Int, height: Int): VideoCompositorSettings =
        object : VideoCompositorSettings {
            private var foregroundWidth = width
            override fun getOutputSize(inputSizes: MutableList<Size>): Size {
                foregroundWidth = inputSizes.first().width.coerceAtLeast(1)
                return Size(width, height)
            }
            override fun getOverlaySettings(inputId: Int, presentationTimeUs: Long): OverlaySettings {
                if (inputId != 0) return StaticOverlaySettings.Builder().build()
                val visible = VideoExportGeometry.reactionVisibleAt(reaction, presentationTimeUs / 1_000)
                val left = reaction.corner == OverlayCorner.TOP_LEFT || reaction.corner == OverlayCorner.BOTTOM_LEFT
                val top = reaction.corner == OverlayCorner.TOP_LEFT || reaction.corner == OverlayCorner.TOP_RIGHT
                val x = if (left) -.94f else .94f
                val y = if (top) .94f else -.94f
                val scale = reaction.widthFraction.coerceIn(.2f, .45f) * width / foregroundWidth
                return StaticOverlaySettings.Builder().setAlphaScale(if (visible) 1f else 0f)
                    .setBackgroundFrameAnchor(x, y)
                    .setOverlayFrameAnchor(if (left) -1f else 1f, if (top) 1f else -1f)
                    .setScale(scale, scale)
                    .build()
            }
        }
}

internal object VideoExportGeometry {
    /** Even encoder dimensions, no upscaling, bounded by a 1280x720 (or portrait 720x1280) box. */
    fun outputSize(width: Int, height: Int): Pair<Int, Int> {
        require(width > 0 && height > 0)
        val scale = min(1f, min(720f / min(width, height), 1280f / maxOf(width, height)))
        return ((width * scale).roundToInt() / 2 * 2).coerceAtLeast(2) to
            ((height * scale).roundToInt() / 2 * 2).coerceAtLeast(2)
    }

    fun reactionVisibleAt(reaction: VideoReaction, timeMs: Long): Boolean =
        timeMs >= reaction.startMs && timeMs - reaction.startMs < reaction.durationMs
}
