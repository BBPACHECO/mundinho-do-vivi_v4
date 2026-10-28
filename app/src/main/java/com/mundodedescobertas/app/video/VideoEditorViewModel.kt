package com.mundodedescobertas.app.video

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mundodedescobertas.app.dj.DjPad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.coroutines.coroutineContext

internal data class VideoEditorState(
    val project: VideoProject? = null,
    val preparing: Boolean = true,
    val importing: Boolean = false,
    val exportProgress: Int? = null,
    val saving: Boolean = false,
    val rendered: File? = null,
    val message: String? = null,
) {
    val busy get() = preparing || importing || exportProgress != null || saving
}

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val mutable = MutableStateFlow(VideoEditorState())
    internal val state = mutable.asStateFlow()
    private var exportJob: Job? = null
    private var importJob: Job? = null
    private var generation = 0
    private var exportGeneration = 0
    private val sessionId = VideoScratchSessions.register()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { runCatching { VideoScratchSessions.prunePreviousSessions(getApplication(), sessionId) } }
            mutable.value = mutable.value.copy(preparing = false)
        }
    }

    internal fun importVideo(uri: Uri) {
        if (mutable.value.busy) return
        val selected = ++generation
        mutable.value = mutable.value.copy(importing = true, message = null)
        importJob = viewModelScope.launch {
            try {
                val imported = VideoImports.copy(getApplication(), uri)
                if (selected != generation) { imported.file.delete(); return@launch }
                val source = VideoSource(imported.file, imported.durationMs, imported.width, imported.height, imported.hasAudio)
                disposeFiles(mutable.value)
                mutable.value = VideoEditorState(project = VideoProject(source), preparing = false)
            } catch (cancelled: CancellationException) {
                if (selected == generation) mutable.value = mutable.value.copy(importing = false)
                throw cancelled
            } catch (failure: Exception) {
                if (selected == generation) mutable.value = mutable.value.copy(importing = false, message = failure.friendlyImportMessage())
            }
        }
    }

    internal fun update(change: (VideoProject) -> VideoProject) {
        val current = mutable.value
        if (current.busy || exportJob?.isActive == true) return
        val project = current.project ?: return
        current.rendered?.delete()
        mutable.value = current.copy(project = change(project), rendered = null, message = null)
    }

    internal fun addSticker(emoji: VideoEmoji, positionMs: Long) = update { project ->
        if (project.stickers.size >= VideoLimits.MAX_STICKERS) project else project.copy(stickers = project.stickers + VideoSticker(
            emoji = emoji, startMs = positionMs.coerceIn(0, maxOf(0, project.source.durationMs - 100)), endMs = project.source.durationMs,
        ))
    }

    internal fun addSound(pad: DjPad, positionMs: Long, volume: Float) = update { project ->
        if (project.sounds.size >= VideoLimits.MAX_SOUNDS) project else project.copy(sounds = project.sounds + VideoSound(
            padSource = pad.source, title = pad.title, startMs = positionMs.coerceIn(0, maxOf(0, project.source.durationMs - 100)),
            volume = volume.coerceIn(0f, 1f),
        ))
    }

    internal fun acceptReaction(reaction: VideoReaction) = update { project ->
        project.reaction?.takeIf { it.file != reaction.file }?.file?.delete()
        project.copy(reaction = reaction.copy(startMs = 0, durationMs = reaction.durationMs.coerceAtMost(project.source.durationMs)))
    }

    internal fun removeReaction() = update { project -> project.reaction?.file?.delete(); project.copy(reaction = null) }

    internal fun render() {
        val current = mutable.value
        val project = current.project ?: return
        if (current.busy || exportJob?.isActive == true) return
        val request = ++exportGeneration
        mutable.value = current.copy(exportProgress = 0, message = null)
        exportJob = viewModelScope.launch {
            try {
                val file = VideoExporter(getApplication()).export(project) { progress ->
                    if (request == exportGeneration) mutable.value = mutable.value.copy(exportProgress = progress.coerceIn(0, 100))
                }
                if (request != exportGeneration) { file.delete(); return@launch }
                mutable.value.rendered?.takeIf { it != file }?.delete()
                mutable.value = mutable.value.copy(rendered = file, exportProgress = null, message = "Sua edição ficou pronta! Assista e escolha se quer salvar na galeria.")
            } catch (cancelled: CancellationException) {
                if (request == exportGeneration) mutable.value = mutable.value.copy(exportProgress = null, message = "Tudo bem! Sua edição continua aqui para tentar de novo.")
                throw cancelled
            } catch (_: Exception) {
                if (request == exportGeneration) mutable.value = mutable.value.copy(exportProgress = null, message = "Não conseguimos criar esse vídeo neste aparelho. Tente um vídeo menor ou retire a janelinha da câmera.")
            }
        }
    }

    internal fun cancelRender() { exportJob?.cancel() }

    internal fun saveGallery() {
        val current = mutable.value
        val file = current.rendered ?: return
        if (current.busy) return
        mutable.value = current.copy(saving = true, message = null)
        viewModelScope.launch {
            try {
                VideoMediaStore.saveToGallery(getApplication(), file)
                mutable.value = mutable.value.copy(saving = false, message = "Vídeo salvo na galeria, em Filmes/MundinhoDoVivi.")
            } catch (cancelled: CancellationException) {
                mutable.value = mutable.value.copy(saving = false)
                throw cancelled
            } catch (_: Exception) {
                mutable.value = mutable.value.copy(saving = false, message = "Não conseguimos salvar na galeria. O resultado continua aqui para tentar de novo.")
            }
        }
    }

    internal fun message(message: String?) { mutable.value = mutable.value.copy(message = message) }

    override fun onCleared() {
        generation++
        exportGeneration++
        importJob?.cancel()
        exportJob?.cancel()
        disposeFiles(mutable.value)
        VideoScratchSessions.unregister(sessionId)
        super.onCleared()
    }

    private fun disposeFiles(state: VideoEditorState) {
        state.project?.source?.file?.delete()
        state.project?.reaction?.file?.delete()
        state.rendered?.delete()
    }
}

/** Only the first live editor may discard scratch files left by a killed process.
 * Its controls stay disabled until pruning completes, so a new import cannot race deletion.
 */
private object VideoScratchSessions {
    private val active = mutableSetOf<String>()
    @Synchronized fun register(): String = UUID.randomUUID().toString().also { active.add(it) }
    @Synchronized fun unregister(id: String) { active.remove(id) }
    @Synchronized fun prunePreviousSessions(context: Context, id: String) {
        if (active != setOf(id)) return
        val directory = File(context.cacheDir, "video-editor")
        if (!directory.isDirectory) return
        directory.walkBottomUp().filter { it != directory }.forEach { it.delete() }
    }
}

private fun Exception.friendlyImportMessage(): String = when (this) {
    is IllegalArgumentException -> message?.takeIf { it.startsWith("Escolha") || it.startsWith("Precisamos") || it.startsWith("Esse arquivo") || it.startsWith("Esse vídeo") }
    else -> null
} ?: "Não conseguimos abrir esse vídeo. Escolha outro arquivo local e tente de novo."

/** A private copy keeps later document-provider revocation from breaking an edit/export. */
internal object VideoImports {
    const val MAX_BYTES = VideoLimits.MAX_SOURCE_BYTES
    const val MAX_DURATION_MS = VideoLimits.MAX_DURATION_MS

    data class Imported(val file: File, val durationMs: Long, val width: Int, val height: Int, val hasAudio: Boolean)

    suspend fun copy(context: Context, uri: Uri): Imported = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val reportedSize = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
            }
        }.getOrNull()
        require(reportedSize == null || reportedSize <= MAX_BYTES) { "Escolha um vídeo de até 500 MB." }
        val folder = File(context.cacheDir, "video-editor").apply { check(isDirectory || mkdirs()) }
        // Leave room for both the imported file and a bounded 720p export.
        require(folder.usableSpace > (reportedSize ?: 30L * 1024 * 1024) + 80L * 1024 * 1024) {
            "Precisamos de um pouco mais de espaço livre para editar este vídeo."
        }
        val destination = File(folder, "source_${UUID.randomUUID()}.mp4")
        try {
            resolver.openInputStream(uri)?.use { source ->
                destination.outputStream().use { output -> copyBounded(source, output, MAX_BYTES) }
            } ?: throw IOException("Não conseguimos abrir esse arquivo. Escolha outro vídeo.")
            coroutineContext.ensureActive()
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(destination.absolutePath)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == "yes"
                require(duration in 1L..MAX_DURATION_MS) { "Escolha um vídeo com até 1 minuto e 30 segundos." }
                require(width > 0 && height > 0) { "Esse arquivo não tem uma imagem de vídeo compatível." }
                if (rotation % 180 == 0) Imported(destination, duration, width, height, hasAudio)
                else Imported(destination, duration, height, width, hasAudio)
            } finally { retriever.release() }
        } catch (failure: Throwable) {
            destination.delete()
            throw failure
        }
    }

    internal suspend fun copyBounded(input: InputStream, output: OutputStream, maxBytes: Long): Long {
        var total = 0L
        val buffer = ByteArray(64 * 1024)
        while (true) {
            coroutineContext.ensureActive()
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= maxBytes) { "Escolha um vídeo de até 500 MB." }
            output.write(buffer, 0, count)
        }
        require(total > 0) { "Esse vídeo está vazio. Escolha outro arquivo." }
        return total
    }
}
