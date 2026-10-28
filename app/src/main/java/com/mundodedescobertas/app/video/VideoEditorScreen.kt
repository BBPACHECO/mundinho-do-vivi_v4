package com.mundodedescobertas.app.video

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.dj.DjCatalog
import com.mundodedescobertas.app.dj.DjPad

@Composable
fun VideoEditorScreen(settings: ChildPreferences, model: VideoEditorViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val project = state.project
    var capture by remember { mutableStateOf(false) }
    var replace by remember { mutableStateOf(false) }
    var confirmSave by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var showResult by remember(state.rendered) { mutableStateOf(state.rendered != null) }
    val openVideo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(model::importVideo) }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var audition by remember { mutableStateOf<MediaPlayer?>(null) }
    fun stopAudition() { audition?.let { runCatching { it.stop() }; it.release() }; audition = null }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) { stopAudition(); model.cancelRender() } }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); stopAudition() }
    }
    fun listen(pad: DjPad) {
        stopAudition()
        try {
            val player = MediaPlayer()
            audition = player
            context.assets.openFd("dj/${pad.source}.wav").use { player.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            player.setVolume(settings.effectiveEffectsVolume, settings.effectiveEffectsVolume)
            player.setOnCompletionListener { if (audition === it) { audition = null; it.release() } }
            player.setOnErrorListener { media, _, _ -> if (audition === media) { audition = null; media.release() }; true }
            player.prepare()
            player.start()
        } catch (_: Exception) { stopAudition(); model.message("Esse som não pôde tocar agora. Tente outro.") }
    }
    if (capture && project != null) {
        BackHandler { capture = false }
        ReactionCaptureScreen(
            sourceUri = project.source.uri, durationMs = project.source.durationMs, settings = settings,
            onRecorded = { capture = false; model.acceptReaction(it); positionMs = 0 },
            onCancel = { capture = false },
            aspectRatio = project.source.width.toFloat() / project.source.height.coerceAtLeast(1),
        )
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Meu miniestúdio", style = MaterialTheme.typography.headlineMedium)
        Text("Vamos inventar um vídeo do seu jeitinho?", style = MaterialTheme.typography.bodyLarge)
        Text("Escolha um vídeo de até 1 minuto e 30 segundos e 500 MB. O original fica guardado como está.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = {
            stopAudition()
            if (project == null) openVideo.launch(arrayOf("video/*")) else replace = true
        }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
            Text(if (project == null) "Escolher um vídeo" else "Escolher outro vídeo")
        }
        if (state.preparing) { CircularProgressIndicator(); Text("Preparando seu estúdio…") }
        if (state.importing) { CircularProgressIndicator(); Text("Preparando seu vídeo…") }
        state.message?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        if (project == null) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cores, emojis e brincadeiras", style = MaterialTheme.typography.titleLarge)
                    Text("Mude a cor, acrescente emojis, sons e uma voz divertida. Você também pode gravar uma janelinha sua enquanto o vídeo passa.")
                    Text("Tudo é feito neste aparelho. Só o vídeo escolhido será aberto; nada é enviado.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Text(if (showResult) "Olha como ficou!" else "Prévia da imagem", style = MaterialTheme.typography.titleLarge)
            VideoPreview(project, settings, if (showResult) state.rendered else null, !state.busy) { positionMs = it }
            if (showResult && state.rendered != null) {
                Text("Aqui você vê e ouve a edição completa. Seu vídeo só vai para a galeria quando você escolher salvar.")
                Button(onClick = { confirmSave = true }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text(if (state.saving) "Salvando…" else "Salvar vídeo na galeria") }
                OutlinedButton(onClick = { showResult = false }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Voltar a editar") }
            } else {
                Text("A prévia mostra as cores e os emojis com o som original. Toque em Ver resultado para ouvir a voz, os sons e ver a câmera juntos.", style = MaterialTheme.typography.bodyMedium)
                EditorSection("Cores do vídeo") {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VideoColor.entries.forEach { color ->
                            FilterChip(selected = project.color == color, onClick = { model.update { it.copy(color = color) } }, enabled = !state.busy,
                                label = { Text(color.title) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                }
                EditorSection("Emojis") {
                    Text("Pare o vídeo no momento desejado e escolha um emoji. Ele aparece desse ponto até o fim, ou pelo tempo que você escolher.", style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VideoEmoji.entries.forEach { emoji ->
                            FilledTonalButton(onClick = { model.addSticker(emoji, positionMs) }, enabled = !state.busy && project.stickers.size < VideoLimits.MAX_STICKERS,
                                modifier = Modifier.heightIn(min = 52.dp).semantics { contentDescription = "Adicionar emoji ${emoji.title}" }) { Text("${emoji.glyph} ${emoji.title}") }
                        }
                    }
                    Text("${project.stickers.size} de ${VideoLimits.MAX_STICKERS} emojis", style = MaterialTheme.typography.bodyMedium)
                    project.stickers.forEachIndexed { index, sticker ->
                        key(sticker.id) {
                            StickerControls(index, sticker, project.source.durationMs, !state.busy,
                                onChange = { next -> model.update { it.copy(stickers = it.stickers.map { old -> if (old.id == sticker.id) next else old }) } },
                                onRemove = { model.update { it.copy(stickers = it.stickers.filterNot { old -> old.id == sticker.id }) } })
                        }
                    }
                }
                EditorSection("Voz divertida") {
                    Text("O filtro muda todo o áudio original e a voz da janelinha, incluindo músicas que já estejam no vídeo.", style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceEffect.entries.forEach { voice ->
                            FilterChip(selected = project.voice == voice, onClick = { model.update { it.copy(voice = voice) } }, enabled = !state.busy,
                                label = { Text(voice.title) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    if (!project.source.hasAudio && project.reaction?.hasAudio != true) Text("Este vídeo está sem áudio. Grave a janelinha com voz para brincar com os filtros.", style = MaterialTheme.typography.bodyMedium)
                    Text("Volume do som original · ${(project.originalVolume * 100).toInt()}%")
                    Slider(value = project.originalVolume, onValueChange = { next -> model.update { it.copy(originalVolume = next) } }, enabled = !state.busy,
                        modifier = Modifier.semantics { contentDescription = "Volume do áudio original no vídeo salvo" })
                }
                EditorSection("Sons engraçados") {
                    Text("Escolha Ouvir para conhecer o som. Pare o vídeo no ponto desejado e toque em Colocar para guardar esse momento.", style = MaterialTheme.typography.bodyMedium)
                    funnyPads.forEach { pad ->
                        Text(pad.title, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { listen(pad) }, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp).weight(1f)) { Text("Ouvir") }
                            FilledTonalButton(onClick = { stopAudition(); model.addSound(pad, positionMs, .7f) }, enabled = !state.busy && project.sounds.size < VideoLimits.MAX_SOUNDS,
                                modifier = Modifier.heightIn(min = 48.dp).weight(1f).semantics { contentDescription = "Colocar ${pad.title} em ${videoTime(positionMs)}" }) { Text("Colocar · ${videoTime(positionMs)}") }
                        }
                    }
                    Text("${project.sounds.size} de ${VideoLimits.MAX_SOUNDS} sons colocados", style = MaterialTheme.typography.bodyMedium)
                    project.sounds.forEach { sound ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${sound.title} · ${videoTime(sound.startMs)}", modifier = Modifier.weight(1f))
                            TextButton(onClick = { model.update { it.copy(sounds = it.sounds.filterNot { old -> old.id == sound.id }) } }, enabled = !state.busy,
                                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Retirar som ${sound.title}" }) { Text("Retirar") }
                        }
                    }
                }
                EditorSection("Minha janelinha da câmera") {
                    Text("Grave sua reação enquanto o vídeo passa desde o começo. A câmera só liga depois da sua autorização.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { stopAudition(); capture = true }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text(if (project.reaction == null) "Gravar minha janelinha" else "Gravar a janelinha de novo")
                    }
                    project.reaction?.let { reaction ->
                        Text("Janelinha gravada · ${videoTime(reaction.durationMs)}", style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OverlayCorner.entries.forEach { corner ->
                                FilterChip(selected = reaction.corner == corner, onClick = { model.update { it.copy(reaction = reaction.copy(corner = corner)) } }, enabled = !state.busy,
                                    label = { Text(corner.title) }, modifier = Modifier.heightIn(min = 48.dp))
                            }
                        }
                        Text("Tamanho da janelinha")
                        Slider(value = reaction.widthFraction, onValueChange = { next -> model.update { it.copy(reaction = reaction.copy(widthFraction = next)) } }, valueRange = .2f.. .45f,
                            enabled = !state.busy, modifier = Modifier.semantics { contentDescription = "Tamanho da janelinha da câmera" })
                        if (reaction.hasAudio) {
                            Text("Volume da voz na janelinha · ${(project.reactionVolume * 100).toInt()}%")
                            Slider(value = project.reactionVolume, onValueChange = { next -> model.update { it.copy(reactionVolume = next) } }, enabled = !state.busy,
                                modifier = Modifier.semantics { contentDescription = "Volume do áudio da janelinha no vídeo salvo" })
                        }
                        TextButton(onClick = model::removeReaction, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("Retirar janelinha") }
                    }
                }
                val progress = state.exportProgress
                if (progress != null) {
                    Text("Preparando seu vídeo · $progress%", style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = model::cancelRender, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Cancelar e continuar editando") }
                } else {
                    Button(onClick = { stopAudition(); model.render() }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Ver resultado") }
                }
                Text("O resultado é um novo vídeo. Sua edição fica nesta sessão até você salvar na galeria.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    if (replace) AlertDialog(onDismissRequest = { replace = false }, title = { Text("Escolher outro vídeo?") },
        text = { Text("As mudanças desta edição serão substituídas quando o novo vídeo abrir. Uma cópia já salva na galeria continua guardada.") },
        confirmButton = { TextButton(onClick = { replace = false; openVideo.launch(arrayOf("video/*")) }) { Text("Escolher outro") } },
        dismissButton = { TextButton(onClick = { replace = false }) { Text("Continuar editando") } })
    if (confirmSave) AlertDialog(onDismissRequest = { confirmSave = false }, title = { Text("Salvar sua criação?") },
        text = { Text("Uma cópia deste resultado vai para a galeria do aparelho. Você pode assistir mesmo sem internet.") },
        confirmButton = { TextButton(onClick = { confirmSave = false; stopAudition(); model.saveGallery() }) { Text("Salvar na galeria") } },
        dismissButton = { TextButton(onClick = { confirmSave = false }) { Text("Agora não") } })
}

@Composable
private fun EditorSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StickerControls(index: Int, sticker: VideoSticker, durationMs: Long, enabled: Boolean, onChange: (VideoSticker) -> Unit, onRemove: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${index + 1}. ${sticker.emoji.glyph} ${sticker.emoji.title}", style = MaterialTheme.typography.titleMedium)
        Text("Lado a lado")
        Slider(value = sticker.centerX, onValueChange = { onChange(sticker.copy(centerX = it)) }, valueRange = .1f.. .9f, enabled = enabled,
            modifier = Modifier.semantics { contentDescription = "Posição horizontal do emoji ${index + 1}" })
        Text("Em cima ou embaixo")
        Slider(value = sticker.centerY, onValueChange = { onChange(sticker.copy(centerY = it)) }, valueRange = .1f.. .9f, enabled = enabled,
            modifier = Modifier.semantics { contentDescription = "Posição vertical do emoji ${index + 1}" })
        Text("Tamanho")
        Slider(value = sticker.sizeFraction, onValueChange = { onChange(sticker.copy(sizeFraction = it)) }, valueRange = .1f.. .35f, enabled = enabled,
            modifier = Modifier.semantics { contentDescription = "Tamanho do emoji ${index + 1}" })
        Text("Aparece de ${videoTime(sticker.startMs)} até ${videoTime(sticker.endMs)}")
        RangeSlider(value = sticker.startMs.toFloat()..sticker.endMs.coerceAtMost(durationMs).toFloat(),
            onValueChange = { range ->
                val start = range.start.toLong().coerceAtMost(durationMs - 100).coerceAtLeast(0)
                val end = range.endInclusive.toLong().coerceIn(start + 100, durationMs.coerceAtLeast(start + 100))
                onChange(sticker.copy(startMs = start, endMs = end))
            }, valueRange = 0f..durationMs.coerceAtLeast(100).toFloat(), enabled = enabled,
            modifier = Modifier.semantics { contentDescription = "Intervalo do emoji ${index + 1}" })
        TextButton(onClick = onRemove, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) { Text("Retirar este emoji") }
    }
}

private val funnyPads: List<DjPad> = listOf("misc_burp", "vinyl_scratch", "elec_blup", "elec_twang", "elec_plip", "perc_bell").mapNotNull { source -> DjCatalog.pads.firstOrNull { it.source == source } }
