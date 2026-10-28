package com.mundodedescobertas.app.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File

/** Real decoding: color and timed sticker effects are exactly the shared export effects. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
internal fun VideoPreview(
    project: VideoProject,
    settings: ChildPreferences,
    result: File?,
    enabled: Boolean,
    onPosition: (Long) -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val file = result ?: project.source.file
    val player = remember(file) {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
            setHandleAudioBecomingNoisy(true)
        }
    }
    var position by remember(file) { mutableLongStateOf(0L) }
    var duration by remember(file) { mutableLongStateOf(project.source.durationMs) }
    var playing by remember(file) { mutableStateOf(false) }
    var error by remember(file) { mutableStateOf<String?>(null) }
    var preparing by remember(file) { mutableStateOf(true) }
    val latestPosition by rememberUpdatedState(onPosition)
    LaunchedEffect(player) {
        player.setVideoEffects(if (result == null) VideoRenderEffects.videoEffects(project) else emptyList())
        player.setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(file)))
        player.prepare()
    }
    LaunchedEffect(player, project.color, project.stickers, result) {
        if (result == null) player.setVideoEffects(VideoRenderEffects.videoEffects(project))
    }
    LaunchedEffect(player, settings.effectiveEffectsVolume, project.originalVolume, result) {
        player.volume = settings.effectiveEffectsVolume * if (result == null) project.originalVolume else 1f
    }
    LaunchedEffect(player, enabled) { if (!enabled) player.pause() }
    LaunchedEffect(player, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                position = player.currentPosition.coerceAtLeast(0)
                if (player.duration > 0) duration = player.duration
                latestPosition(position)
                delay(120)
            }
        }
    }
    DisposableEffect(player, lifecycle) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
            override fun onPlaybackStateChanged(playbackState: Int) { preparing = playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE }
            override fun onPlayerError(failure: PlaybackException) {
                error = "Não conseguimos reproduzir esse formato neste aparelho. Tente outro vídeo."
                preparing = false
            }
        }
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) player.pause() }
        player.addListener(listener)
        lifecycle.addObserver(observer)
        onDispose {
            player.removeListener(listener)
            lifecycle.removeObserver(observer)
            player.release()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val ratio = (project.source.width.toFloat() / project.source.height.coerceAtLeast(1)).coerceIn(.4f, 2.5f)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val height = (maxWidth / ratio).coerceIn(180.dp, 340.dp)
            AndroidView(
                factory = { viewContext -> PlayerView(viewContext).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                } },
                update = { it.player = player },
                modifier = Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(22.dp)).background(Color(0xff30283c))
                    .semantics { contentDescription = if (result == null) "Prévia da imagem do vídeo" else "Vídeo com a edição aplicada" },
            )
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (preparing && error == null) Text("Preparando a imagem…", style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                if (playing) player.pause() else {
                    if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
                    player.play()
                }
            }, enabled = enabled && error == null, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (playing) "Pausar vídeo" else "Tocar vídeo") }
            TextButton(onClick = { player.pause(); player.seekTo(0) }, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) { Text("Desde o começo") }
        }
        Text("${videoTime(position)} / ${videoTime(duration)}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = position.coerceIn(0, duration.coerceAtLeast(1)).toFloat(),
            onValueChange = { position = it.toLong(); player.seekTo(position); latestPosition(position) },
            valueRange = 0f..duration.coerceAtLeast(1).toFloat(), enabled = enabled && error == null,
            modifier = Modifier.semantics { contentDescription = "Posição no vídeo" },
        )
    }
}

internal fun videoTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return "%d:%02d".format(java.util.Locale.ROOT, seconds / 60, seconds % 60)
}
