package com.mundodedescobertas.app.video

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.delay
import java.util.Locale

/** Live reaction capture starts at the beginning of the selected video and never edits its file. */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ReactionCaptureScreen(
    sourceUri: Uri,
    durationMs: Long,
    settings: ChildPreferences,
    onRecorded: (VideoReaction) -> Unit,
    onCancel: () -> Unit,
    aspectRatio: Float = 9f / 16f,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val latestRecorded by rememberUpdatedState(onRecorded)
    val latestCancel by rememberUpdatedState(onCancel)
    var cameraGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.CAMERA)) }
    var micGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.RECORD_AUDIO)) }
    var cameraEnabled by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }
    var front by remember { mutableStateOf(true) }
    var recordVoice by remember { mutableStateOf(false) }
    var starting by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var finishing by remember { mutableStateOf(false) }
    var foreground by remember { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    var message by remember { mutableStateOf<String?>(null) }
    var playerReady by remember { mutableStateOf(false) }
    var recordedMs by remember { mutableLongStateOf(0) }
    var playerPosition by remember { mutableLongStateOf(0) }
    var videoRatio by remember(sourceUri) { mutableFloatStateOf(aspectRatio.takeIf { it.isFinite() && it > 0f } ?: 9f / 16f) }
    var controller by remember { mutableStateOf<ReactionRecorder?>(null) }
    val preview = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    val player = remember(sourceUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(sourceUri))
            playWhenReady = false
            prepare()
        }
    }
    LaunchedEffect(player, settings.masterVolume) { player.volume = settings.masterVolume.coerceIn(0f, 1f) }

    fun stopRecording() {
        if (recording || starting) {
            player.pause()
            starting = false
            recording = false
            finishing = true
            controller?.stop()
        }
    }

    fun startRecording(withVoice: Boolean) {
        if (!foreground || !cameraEnabled || !cameraReady || !playerReady || starting || recording || finishing) return
        message = null
        player.pause()
        player.seekTo(0)
        playerPosition = 0
        recordedMs = 0
        starting = true
        controller?.start(withVoice)
    }

    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
        cameraEnabled = granted
        if (!granted) message = "Tudo bem! A janelinha precisa da câmera, mas você pode continuar editando o vídeo sem ela."
    }
    val requestMicrophone = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (!granted) {
            recordVoice = false
            startRecording(withVoice = false)
            message = "O microfone ficou desligado. Vamos gravar a janelinha sem voz."
        } else startRecording(withVoice = true)
    }
    DisposableEffect(owner, player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                playerReady = playbackState == Player.STATE_READY
                if (playbackState == Player.STATE_ENDED) stopRecording()
            }
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoRatio = videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                stopRecording()
                playerReady = false
                message = "Não conseguimos reproduzir este vídeo. Volte ao editor e escolha outro arquivo."
            }
        }
        player.addListener(listener)
        playerReady = player.playbackState == Player.STATE_READY
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                foreground = true
                cameraGranted = hasPermission(context, Manifest.permission.CAMERA)
                micGranted = hasPermission(context, Manifest.permission.RECORD_AUDIO)
            }
            if (event == Lifecycle.Event.ON_STOP) {
                foreground = false
                player.pause()
                controller?.close()
                controller = null
                cameraEnabled = false
                cameraReady = false
                if (starting || recording || finishing) message = "A gravação foi interrompida ao sair. Quando quiser, ligue a câmera e tente outra vez."
                starting = false
                recording = false
                finishing = false
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            player.removeListener(listener)
            owner.lifecycle.removeObserver(observer)
            controller?.close()
            controller = null
            player.release()
        }
    }
    DisposableEffect(cameraEnabled, cameraGranted, foreground, front, owner, preview, player) {
        cameraReady = false
        if (cameraEnabled && cameraGranted && foreground) {
            val current = ReactionRecorder(context, owner, preview, front,
                durationMs.coerceIn(250, VideoLimits.MAX_DURATION_MS),
                onReady = { cameraReady = true },
                onStarted = {
                    if (foreground && cameraEnabled) {
                        starting = false
                        recording = true
                        player.seekTo(0)
                        player.play()
                    }
                },
                onProgress = { recordedMs = it },
                onFinished = { file, milliseconds, hasAudio ->
                    player.pause()
                    starting = false
                    recording = false
                    finishing = false
                    latestRecorded(VideoReaction(file = file, durationMs = milliseconds, hasAudio = hasAudio))
                },
                onError = {
                    player.pause()
                    cameraEnabled = false
                    cameraReady = false
                    starting = false
                    recording = false
                    finishing = false
                    message = it
                },
            )
            controller = current
            current.bind()
            onDispose {
                current.close()
                if (controller === current) controller = null
            }
        } else onDispose { }
    }
    LaunchedEffect(recording, player) {
        while (recording) {
            playerPosition = player.currentPosition.coerceAtLeast(0)
            if (playerPosition >= durationMs) stopRecording()
            delay(100)
        }
    }
    BackHandler {
        player.pause()
        controller?.close()
        latestCancel()
    }

    val busy = starting || recording || finishing
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Sua reação no vídeo", style = MaterialTheme.typography.headlineMedium)
        Text("A janelinha grava você enquanto o vídeo passa, desde o começo. Depois você pode escolher o canto dela.")
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val ratio = videoRatio.coerceIn(.1f, 10f)
        val videoHeight = (maxWidth / ratio).coerceAtMost(400.dp)
        Box(
            Modifier.width(videoHeight * ratio).height(videoHeight)
                .clip(RoundedCornerShape(24.dp)).background(Color.Black),
        ) {
            AndroidView(factory = { PlayerView(it).apply { this.player = player; useController = false } },
                modifier = Modifier.fillMaxSize())
            if (cameraEnabled && cameraGranted && foreground) {
                AndroidView(factory = {
                    // This remembered camera view can be attached to a new AndroidViewHolder
                    // after an explicit off/on or background round-trip.
                    (preview.parent as? ViewGroup)?.removeView(preview)
                    preview
                }, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                    .fillMaxWidth(.3f).aspectRatio(9f / 16f).clip(RoundedCornerShape(14.dp)).background(Color.Black))
            }
            if (recording || starting || finishing) {
                Surface(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        when {
                            starting -> "Preparando gravação…"
                            finishing -> "Guardando sua reação…"
                            else -> "Gravando ${captureTime(recordedMs)} / ${captureTime(durationMs)}"
                        }, Modifier.padding(10.dp),
                    )
                }
            }
        }
        }
        if (!cameraEnabled || !cameraGranted) {
            Button(onClick = {
                message = null
                if (hasPermission(context, Manifest.permission.CAMERA)) {
                    cameraGranted = true
                    cameraEnabled = true
                } else requestCamera.launch(Manifest.permission.CAMERA)
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Ligar câmera") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { front = !front; message = null }, enabled = !busy,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Trocar câmera") }
                OutlinedButton(onClick = { cameraEnabled = false; controller?.close() }, enabled = !busy,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Desligar") }
            }
            if (!cameraReady && !busy && message == null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.width(24.dp))
                    Text("Preparando câmera e vídeo…")
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Gravar minha voz", style = MaterialTheme.typography.titleMedium)
                Text("O microfone só será pedido ao começar com esta opção ligada.", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = recordVoice, onCheckedChange = { recordVoice = it }, enabled = !busy)
        }
        if (recordVoice) Text("Use fones, se possível: o microfone também pode captar o som do vídeo. Os filtros de voz são escolhidos no editor.",
            style = MaterialTheme.typography.bodyMedium)
        if (recording) {
            Button(onClick = ::stopRecording, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Terminar e usar esta reação") }
        } else {
            Button(onClick = {
                if (recordVoice && !hasPermission(context, Manifest.permission.RECORD_AUDIO)) {
                    requestMicrophone.launch(Manifest.permission.RECORD_AUDIO)
                } else startRecording(withVoice = recordVoice && micGranted)
            }, enabled = cameraReady && playerReady && !starting && !finishing,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(if (starting) "Preparando…" else if (finishing) "Finalizando…" else "Gravar junto com o vídeo")
            }
        }
        if (message != null) Text(message.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        Text("A câmera e o microfone desligam ao sair. Tudo fica neste aparelho.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { player.pause(); controller?.close(); latestCancel() },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Voltar ao editor") }
    }
}

private fun hasPermission(context: android.content.Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun captureTime(ms: Long): String = String.format(Locale.ROOT, "%d:%02d", ms.coerceAtLeast(0) / 60_000, ms.coerceAtLeast(0) / 1000 % 60)
