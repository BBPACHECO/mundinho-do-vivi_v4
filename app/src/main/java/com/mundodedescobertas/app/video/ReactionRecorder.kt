package com.mundodedescobertas.app.video

import android.annotation.SuppressLint
import android.content.Context
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.MirrorMode
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.io.File

/**
 * A private camera recording, silent by default. The caller must obtain explicit microphone
 * permission before requesting optional voice capture. No broad file permission is used.
 * All calls and CameraX event callbacks run on the main thread. The caller starts the base player
 * only after [onStarted], avoiding a playback head start while CameraX configures its encoder.
 */
internal class ReactionRecorder(
    private val context: Context,
    private val owner: LifecycleOwner,
    private val previewView: PreviewView,
    private val front: Boolean,
    private val durationMs: Long,
    private val onReady: () -> Unit,
    private val onStarted: () -> Unit,
    private val onProgress: (Long) -> Unit,
    private val onFinished: (File, Long, Boolean) -> Unit,
    private val onError: (String) -> Unit,
) : AutoCloseable {
    private val main = ContextCompat.getMainExecutor(context)
    private var provider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var video: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var output: File? = null
    private var closed = false
    private var stopping = false
    private var delivered = false
    private var recordedAudio = false

    fun bind() {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (closed || !owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@addListener
            try {
                val cameraProvider = future.get()
                val selector = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                if (!cameraProvider.hasCamera(selector)) {
                    onError("Esta câmera não está disponível. Tente trocar a câmera.")
                    return@addListener
                }
                val recorder = Recorder.Builder()
                    // No higher-resolution fallback: both choices stay at or below 720p.
                    .setQualitySelector(QualitySelector.fromOrderedList(listOf(Quality.HD, Quality.SD)))
                    .setTargetVideoEncodingBitRate(2_500_000)
                    .build()
                val videoCapture = VideoCapture.Builder(recorder)
                    // Match PreviewView: the front-facing image is mirrored in the saved clip too.
                    .setMirrorMode(MirrorMode.MIRROR_MODE_ON_FRONT_ONLY)
                    .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                    .build()
                val previewUseCase = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider = cameraProvider
                preview = previewUseCase
                video = videoCapture
                cameraProvider.bindToLifecycle(owner, selector, previewUseCase, videoCapture)
                if (!closed) onReady()
            } catch (_: SecurityException) {
                onError("A câmera precisa da sua autorização para gravar a janelinha.")
            } catch (_: Exception) {
                onError("Não conseguimos preparar a câmera. Feche outro aplicativo de câmera ou tente a outra câmera.")
            }
        }, main)
    }

    @SuppressLint("MissingPermission")
    fun start(includeAudio: Boolean) {
        val capture = video ?: return
        if (closed || recording != null || stopping) return
        if (!owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        try {
            val directory = File(context.cacheDir, "video-editor/reactions").apply { check(mkdirs() || isDirectory) }
            check(directory.usableSpace > 48L * 1024 * 1024)
            val file = File.createTempFile("reacao_", ".mp4", directory)
            output = file
            val options = FileOutputOptions.Builder(file)
                .setDurationLimitMillis(durationMs.coerceIn(250, 90_000))
                .setFileSizeLimit(64L * 1024 * 1024)
                .build()
            recordedAudio = includeAudio
            val pending = capture.output.prepareRecording(context, options)
            // Default remains silent. Only the explicit, permission-checked voice action enables it.
            val prepared = if (includeAudio) pending.withAudioEnabled() else pending
            recording = prepared.start(main) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> if (!closed) onStarted()
                    is VideoRecordEvent.Status -> if (!closed) {
                        onProgress(event.recordingStats.recordedDurationNanos / 1_000_000)
                    }
                    is VideoRecordEvent.Finalize -> finish(event, file)
                }
            }
        } catch (_: SecurityException) {
            discardOutput()
            onError("A autorização da câmera mudou. Ligue a câmera novamente para tentar.")
        } catch (_: Exception) {
            discardOutput()
            onError("Não foi possível começar a gravação. Confira o espaço livre e tente novamente.")
        }
    }

    fun stop() {
        if (closed || stopping) return
        stopping = true
        recording?.stop()
    }

    private fun finish(event: VideoRecordEvent.Finalize, file: File) {
        recording = null
        stopping = false
        val recordedMs = event.recordingStats.recordedDurationNanos / 1_000_000
        val usable = event.error == VideoRecordEvent.Finalize.ERROR_NONE ||
            event.error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED
        if (closed || !usable || recordedMs < 250 || file.length() == 0L) {
            file.delete()
            output = null
            if (!closed) onError("A gravação não ficou pronta. Você pode tentar outra vez; o vídeo original continua guardado.")
            return
        }
        delivered = true
        output = null
        onFinished(file, recordedMs.coerceAtMost(durationMs), recordedAudio)
    }

    private fun discardOutput() {
        if (recording == null && !delivered) output?.delete()
        output = null
    }

    override fun close() {
        if (closed) return
        closed = true
        runCatching { recording?.stop() }
        runCatching { recording?.close() }
        // Unbind only this screen's use cases; do not disturb other owners of CameraX.
        val useCases = listOfNotNull(preview, video).toTypedArray()
        if (useCases.isNotEmpty()) runCatching { provider?.unbind(*useCases) }
        preview?.setSurfaceProvider(null)
        preview = null
        video = null
        provider = null
        if (recording == null) discardOutput()
        // If CameraX is finalizing, finish() removes the incomplete file when its callback arrives.
    }
}
