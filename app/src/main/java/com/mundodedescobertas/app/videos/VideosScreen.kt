package com.mundodedescobertas.app.videos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.mundodedescobertas.app.data.ChildPreferences
import java.net.URLEncoder

@Composable
fun VideosScreen(settings: ChildPreferences, onManageVideos: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val library = remember(context) { ApprovedVideoLibrary(context) }
    val videos = rememberApprovedVideos(library)
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val selected = videos.firstOrNull { it.id == selectedId }
    val matching = filterApprovedVideos(videos, query)
    val listState = rememberLazyListState()
    LaunchedEffect(selectedId) {
        if (selectedId != null) {
            if (settings.effectiveAnimation == 0f) listState.scrollToItem(4)
            else listState.animateScrollToItem(4)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("approved_videos_list"),
        state = listState,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Uma pausa para assistir", style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() })
        }
        item {
            Text("Os responsáveis escolhem os vídeos desta lista. A busca procura somente nos títulos aprovados.",
                style = MaterialTheme.typography.bodyLarge)
        }
        item {
            YouTubeNotice()
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = cleanInput(it, 100) },
                label = { Text("Buscar nos vídeos aprovados") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (selected != null) item(key = "selected_video") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(selected.title, style = MaterialTheme.typography.titleLarge)
                if (playingId == selected.id) {
                    YouTubePlayer(selected, settings.masterVolume.coerceIn(0f, 1f),
                        onError = { error = it },
                        modifier = Modifier.fillMaxWidth().height(240.dp))
                    Text("Use o botão do player para iniciar. Ao sair desta tela, a reprodução é encerrada.",
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { playingId = null; error = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Fechar vídeo")
                    }
                } else {
                    Text("Este vídeo usa internet e abre o player do YouTube dentro do aplicativo.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { error = null; playingId = selected.id }, modifier = Modifier.heightIn(min = 52.dp)) {
                        Text("Reproduzir vídeo")
                    }
                }
                error?.let { message ->
                    Text(message, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    OutlinedButton(onClick = { playingId = null; error = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Tentar novamente")
                    }
                }
            }
        }
        if (videos.isEmpty()) item {
            Text("Nenhum vídeo aprovado ainda", style = MaterialTheme.typography.titleLarge)
            Text("Peça ajuda a um responsável para escolher os primeiros vídeos.", style = MaterialTheme.typography.bodyLarge)
        } else if (matching.isEmpty()) item {
            Text("Nenhum vídeo encontrado nessa lista.", style = MaterialTheme.typography.bodyLarge)
        }
        items(matching, key = { it.id }) { video ->
            Card(onClick = {
                if (selectedId != video.id) playingId = null
                selectedId = video.id
                error = null
            }, modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "Selecionar vídeo ${video.title}" }) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(video.title, style = MaterialTheme.typography.titleMedium)
                    Text(if (selectedId == video.id) "Vídeo selecionado" else "Toque para escolher", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            OutlinedButton(onClick = { playingId = null; onManageVideos() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Pedir ajuda aos responsáveis")
            }
        }
    }
}

@Composable
fun ManageVideosScreen() {
    val context = LocalContext.current.applicationContext
    val library = remember(context) { ApprovedVideoLibrary(context) }
    val videos = rememberApprovedVideos(library)
    var query by rememberSaveable { mutableStateOf("") }
    var searchUrl by remember { mutableStateOf<String?>(null) }
    var searchGeneration by remember { mutableIntStateOf(0) }
    var openedUrl by remember { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var pastedUrl by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val openedId = YouTubeVideoLinks.videoId(openedUrl)
    val pastedId = YouTubeVideoLinks.videoId(pastedUrl)
    val approve: (String?) -> Unit = { id ->
        message = when {
            id == null -> "Abra um vídeo no YouTube ou cole um link de vídeo válido."
            title.isBlank() -> "Escreva um título para a criança."
            library.add(ApprovedVideo(id, title)) -> "Vídeo aprovado e salvo nesta lista."
            else -> "Não foi possível salvar. A lista aceita até ${ApprovedVideoLibrary.MAX_VIDEOS} vídeos."
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("manage_videos_list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Escolher vídeos para a criança", style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() })
        }
        item {
            Text("Esta área é dos responsáveis. Assista ao vídeo antes de aprovar e dê um título fácil de encontrar. A criança só pesquisa os títulos da lista aprovada.",
                style = MaterialTheme.typography.bodyLarge)
        }
        item { YouTubeNotice() }
        item {
            OutlinedTextField(value = query, onValueChange = { query = cleanInput(it, 100) },
                label = { Text("Pesquisar vídeos no YouTube") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(onClick = {
                openedUrl = ""
                message = null
                searchGeneration++
                searchUrl = "https://m.youtube.com/results?search_query=${URLEncoder.encode(query.trim(), "UTF-8")}"
            }, enabled = query.isNotBlank(), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Buscar no YouTube")
            }
        }
        searchUrl?.let { requestedUrl ->
            item(key = "youtube_search") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resultados no site oficial do YouTube", style = MaterialTheme.typography.titleMedium)
                    key(searchGeneration) {
                        AdultYouTubeSearch(requestedUrl, onOpenedUrl = { openedUrl = it }, onError = { message = it },
                            modifier = Modifier.fillMaxWidth().height(380.dp))
                    }
                    Text(if (openedId != null) "Vídeo aberto. Confira o conteúdo antes de aprovar." else "Abra um vídeo dos resultados. A aprovação fica disponível ao entrar na página dele.",
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { searchUrl = null; openedUrl = "" }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Fechar pesquisa online")
                    }
                }
            }
        }
        item {
            OutlinedTextField(value = title, onValueChange = { title = cleanInput(it, ApprovedVideoLibrary.MAX_TITLE_LENGTH) },
                label = { Text("Título para a criança") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(onClick = { approve(openedId) }, enabled = openedId != null && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Adicionar vídeo aberto")
            }
        }
        item {
            Text("Também pode colar um link", style = MaterialTheme.typography.titleLarge)
            Text("Use um link HTTPS de vídeo do YouTube quando a pesquisa não carregar. O cadastro não garante a reprodução: vídeos que bloqueiam incorporação continuam indisponíveis no player.",
                style = MaterialTheme.typography.bodyMedium)
        }
        item {
            OutlinedTextField(value = pastedUrl, onValueChange = { pastedUrl = cleanInput(it, 2048) },
                label = { Text("Link do vídeo do YouTube") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedButton(onClick = { approve(pastedId) }, enabled = pastedId != null && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Adicionar link à lista")
            }
        }
        message?.let { current -> item {
            Text(current, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        } }
        item {
            Text("Vídeos aprovados · ${videos.size}/${ApprovedVideoLibrary.MAX_VIDEOS}", style = MaterialTheme.typography.titleLarge)
        }
        if (videos.isEmpty()) item { Text("Nenhum vídeo aprovado ainda", style = MaterialTheme.typography.bodyLarge) }
        items(videos, key = { it.id }) { video ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(video.title, style = MaterialTheme.typography.titleMedium)
                    Text(YouTubeVideoLinks.watchUrl(video.id), style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = {
                        message = if (library.remove(video.id)) "Vídeo retirado da lista." else "Não foi possível remover este vídeo. Tente novamente."
                    },
                        modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Remover vídeo ${video.title}" }) {
                        Text("Remover da lista")
                    }
                }
            }
        }
    }
}

@Composable
private fun YouTubeNotice() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Text("Vídeos online do YouTube precisam de internet. O player pode mostrar anúncios, links e sugestões; não oferece os mesmos filtros do YouTube Kids. Acompanhe a criança durante o uso.",
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
    }
}

@Composable
private fun rememberApprovedVideos(library: ApprovedVideoLibrary): List<ApprovedVideo> {
    var videos by remember(library) { mutableStateOf(library.read()) }
    DisposableEffect(library) {
        val stopObserving = library.observe { videos = library.read() }
        onDispose(stopObserving)
    }
    return videos
}

private fun cleanInput(value: String, limit: Int) = value.filterNot(Char::isISOControl).take(limit)
