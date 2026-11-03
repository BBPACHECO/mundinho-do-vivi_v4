package com.mundodedescobertas.app.videos

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mundodedescobertas.app.BuildConfig
import java.net.URI
import java.util.Locale
import kotlin.math.roundToInt

private val allowedYouTubeHosts = setOf(
    "youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be",
    "youtube-nocookie.com", "www.youtube-nocookie.com",
)

/** Navigation policy, separate from WebView so URL boundary cases can be tested locally. */
internal fun isAllowedYouTubeNavigation(rawUrl: String): Boolean = runCatching {
    if (rawUrl.length > 4096) return false
    val uri = URI(rawUrl)
    uri.scheme.equals("https", ignoreCase = true) && uri.rawUserInfo == null &&
        uri.port in listOf(-1, 443) && uri.host?.lowercase(Locale.ROOT) in allowedYouTubeHosts
}.getOrDefault(false)

/** The child player cannot navigate its main window into general YouTube browsing. */
internal fun isAllowedPlayerNavigation(rawUrl: String, videoId: String, appOrigin: String): Boolean = runCatching {
    if (!YouTubeVideoLinks.isValidId(videoId)) return false
    val uri = URI(rawUrl)
    val app = runCatching { URI(appOrigin) }.getOrNull()
    if (app != null && app.scheme.equals("https", ignoreCase = true) && app.host != null && app.rawUserInfo == null &&
        app.port in listOf(-1, 443) && app.path in listOf("", "/") && app.query == null && app.fragment == null && uri.host != null &&
        uri.scheme.equals("https", ignoreCase = true) && uri.rawUserInfo == null &&
        uri.host.equals(app.host, ignoreCase = true) && uri.port in listOf(-1, 443) &&
        uri.path in listOf("", "/") && uri.query == null && uri.fragment == null) return true
    isAllowedYouTubeNavigation(rawUrl) && uri.host?.lowercase(Locale.ROOT) != "youtu.be" &&
        uri.path == "/embed/$videoId"
}.getOrDefault(false)

/** Instantiate only after the child's explicit selection and play action. No autoplay. */
@Composable
internal fun YouTubePlayer(
    video: ApprovedVideo,
    volume: Float,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!YouTubeVideoLinks.isValidId(video.id)) {
        Text("Este link de vídeo não é válido.", modifier = modifier)
        return
    }
    val origin = "https://${BuildConfig.APPLICATION_ID}"
    val volumePercent = if (volume.isFinite()) (volume.coerceIn(0f, 1f) * 100).roundToInt() else 0
    val html = remember(video.id, origin) { playerDocument(video.id, origin, volumePercent) }
    ManagedYouTubeWebView(
        sourceKey = "player:${video.id}",
        description = "Player do YouTube: ${video.title}",
        selectedVideoId = video.id,
        onLoad = { it.loadDataWithBaseURL("$origin/", html, "text/html", "UTF-8", null) },
        onOpenedUrl = {},
        onError = onError,
        onUpdate = { view ->
            // Only our fixed wrapper defines this function. The value is a bounded integer.
            view.evaluateJavascript("if(window.simbaPlayer&&window.simbaPlayer.setVolume){window.simbaPlayer.setVolume($volumePercent);}", null)
        },
        modifier = modifier,
    )
}

/** Official mobile results page; selection is read from the visited URL, never scraped. */
@Composable
internal fun AdultYouTubeSearch(
    url: String,
    onOpenedUrl: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isAllowedYouTubeNavigation(url)) {
        Text("A pesquisa aceita apenas páginas HTTPS do YouTube.", modifier = modifier)
        return
    }
    ManagedYouTubeWebView(
        sourceKey = "search:$url",
        description = "Pesquisa no YouTube para responsáveis",
        selectedVideoId = null,
        onLoad = { it.loadUrl(url) },
        onOpenedUrl = onOpenedUrl,
        onError = onError,
        onUpdate = {},
        modifier = modifier,
    )
}

@SuppressLint("SetJavaScriptEnabled") // Required by the official YouTube player and search page.
@Composable
private fun ManagedYouTubeWebView(
    sourceKey: String,
    description: String,
    selectedVideoId: String?,
    onLoad: (WebView) -> Unit,
    onOpenedUrl: (String) -> Unit,
    onError: (String) -> Unit,
    onUpdate: (WebView) -> Unit,
    modifier: Modifier,
) {
    key(sourceKey) {
        ManagedYouTubeWebViewContent(sourceKey, description, selectedVideoId, onLoad, onOpenedUrl, onError, onUpdate, modifier)
    }
}

@SuppressLint("SetJavaScriptEnabled") // Required by the official YouTube player and search page.
@Composable
private fun ManagedYouTubeWebViewContent(
    sourceKey: String,
    description: String,
    selectedVideoId: String?,
    onLoad: (WebView) -> Unit,
    onOpenedUrl: (String) -> Unit,
    onError: (String) -> Unit,
    onUpdate: (WebView) -> Unit,
    modifier: Modifier,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var webView by remember { mutableStateOf<WebView?>(null) }
    var suspended by remember { mutableStateOf(false) }
    val currentError by rememberUpdatedState(onError)
    val currentOpened by rememberUpdatedState(onOpenedUrl)
    val currentLoad by rememberUpdatedState(onLoad)
    val currentUpdate by rememberUpdatedState(onUpdate)
    val origin = "https://${BuildConfig.APPLICATION_ID}"

    DisposableEffect(lifecycle, webView) {
        val view = webView
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                suspended = true
                view?.stopLoading()
                // A blank document destroys media frames, including search-page playback.
                // onPause alone does not promise that HTML video audio has stopped.
                view?.loadUrl("about:blank")
                view?.tag = null
                view?.onPause()
                currentOpened("")
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    DisposableEffect(webView) {
        val view = webView
        onDispose {
            view?.stopLoading()
            view?.loadUrl("about:blank")
            view?.onPause()
            (view?.parent as? ViewGroup)?.removeView(view)
            view?.removeAllViews()
            view?.destroy()
        }
    }

    Box(modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    contentDescription = description
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.mediaPlaybackRequiresUserGesture = true
                    settings.setGeolocationEnabled(false)
                    settings.safeBrowsingEnabled = true
                    setDownloadListener { _, _, _, _, _ -> currentError("Downloads não são feitos nesta área. Peça ajuda a um responsável.") }
                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest) { request.deny() }
                        override fun onGeolocationPermissionsShowPrompt(site: String, callback: GeolocationPermissions.Callback) {
                            callback.invoke(site, false, false)
                        }
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            if (message.message().startsWith("SIMBA_PLAYER_ERROR:")) {
                                currentError("O YouTube não conseguiu reproduzir este vídeo. Ele pode estar indisponível ou não permitir reprodução em outros apps.")
                                return true
                            }
                            return false
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val requested = request.url.toString()
                            // Service-owned HTTPS subframes (ads/consent/player internals) are
                            // left intact. File/content and mixed-content access stay disabled.
                            val permitted = if (!request.isForMainFrame)
                                request.url.scheme.equals("https", ignoreCase = true)
                            else if (selectedVideoId != null) isAllowedPlayerNavigation(requested, selectedVideoId, origin)
                            else isAllowedYouTubeNavigation(requested)
                            if (!permitted && request.isForMainFrame) currentError(
                                if (selectedVideoId != null) "Para abrir outro vídeo ou link, peça ajuda aos responsáveis."
                                else "Este link sai do YouTube e não será aberto. Você pode adicionar um link de vídeo manualmente.",
                            )
                            return !permitted
                        }
                        override fun onPageStarted(view: WebView, pageUrl: String, favicon: Bitmap?) {
                            if (pageUrl != "about:blank") currentOpened(pageUrl)
                        }
                        override fun doUpdateVisitedHistory(view: WebView, pageUrl: String, isReload: Boolean) {
                            if (pageUrl != "about:blank") currentOpened(pageUrl)
                        }
                        override fun onPageFinished(view: WebView, pageUrl: String) {
                            if (pageUrl != "about:blank") currentOpened(pageUrl)
                        }
                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                            if (request.isForMainFrame) currentError("Não foi possível carregar o YouTube. Confira a conexão e tente novamente.")
                        }
                        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                            if (request.isForMainFrame && response.statusCode >= 400) currentError("O YouTube não está disponível agora. Tente novamente mais tarde.")
                        }
                        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                            handler.cancel()
                            currentError("A conexão segura com o YouTube não pôde ser confirmada. Tente novamente mais tarde.")
                        }
                    }
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (!suspended) {
                    if (view.tag != sourceKey) {
                        view.tag = sourceKey
                        currentLoad(view)
                    }
                    currentUpdate(view)
                }
            },
        )
        if (suspended) Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("O YouTube foi pausado quando você saiu do aplicativo.")
                Button(onClick = {
                    webView?.onResume()
                    webView?.tag = null
                    suspended = false
                }) { Text(if (selectedVideoId == null) "Retomar pesquisa" else "Preparar vídeo novamente") }
            }
        }
    }
}

private fun playerDocument(videoId: String, origin: String, initialVolume: Int): String = """
<!DOCTYPE html>
<html lang="pt-BR"><head><meta name="viewport" content="width=device-width, initial-scale=1">
<style>html,body{margin:0;width:100%;height:100%;background:#000}#player{width:100%;height:100%}</style></head>
<body><div id="player"></div><script>
function onYouTubeIframeAPIReady(){window.simbaPlayer=new YT.Player('player',{
host:'https://www.youtube-nocookie.com',videoId:'$videoId',
playerVars:{autoplay:0,controls:1,playsinline:1,rel:0,fs:0,origin:'$origin'},
events:{onReady:function(e){e.target.setVolume($initialVolume);},onError:function(e){console.log('SIMBA_PLAYER_ERROR:'+e.data);}}
});}
</script><script src="https://www.youtube.com/iframe_api" onerror="console.log('SIMBA_PLAYER_ERROR:NETWORK')"></script></body></html>
""".trimIndent()
