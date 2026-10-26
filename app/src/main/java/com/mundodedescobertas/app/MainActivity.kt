package com.mundodedescobertas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mundodedescobertas.app.painting.PaintingScreen
import com.mundodedescobertas.app.painting.AdultArtLibraryScreen
import com.mundodedescobertas.app.learning.DiscoverScreen
import com.mundodedescobertas.app.dj.DjScreen
import com.mundodedescobertas.app.dj.AdultRecordingsScreen
import com.mundodedescobertas.app.games.GamesScreen
import com.mundodedescobertas.app.camera.CameraScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.*
import com.mundodedescobertas.app.audio.LocalNarrator
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.ui.screens.*
import com.mundodedescobertas.app.ui.theme.ViviTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val model: ViviViewModel by viewModels { ViviViewModel.factory((application as ViviApplication).preferences) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val narrator = LocalNarrator(this)
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onStop(owner: androidx.lifecycle.LifecycleOwner) { narrator.stop() }
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) { narrator.close() }
        })
        setContent { ViviTheme { ViviApp(model,narrator) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViviApp(model: ViviViewModel, narrator: LocalNarrator) {
    val state by model.state.collectAsStateWithLifecycle()
    val error by model.writeError.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    // Non-saveable: rotation/recreation and backgrounding re-lock the adult area.
    var adultUnlocked by remember { mutableStateOf(false) }
    val adultRoutes = setOf("parents", "art-library", "credits", "recordings")
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _,event -> if(event==Lifecycle.Event.ON_STOP) adultUnlocked=false }
        owner.lifecycle.addObserver(observer)
        onDispose {owner.lifecycle.removeObserver(observer)}
    }
    LaunchedEffect(route) { narrator.stop(); if(route !in adultRoutes) adultUnlocked=false }
    val settings = (state as? PreferencesState.Ready)?.value
    LaunchedEffect(settings?.narrationEnabled, settings?.effectiveNarrationVolume) { narrator.stop() }
    val title = when(route) { "parents" -> "Área dos responsáveis"; "art-library" -> "Desenhos e criações"; "credits" -> "Créditos e privacidade"; "recordings" -> "Músicas salvas"; else -> ActivityArea.entries.firstOrNull{it.route==route}?.title.orEmpty() }
    Scaffold(topBar={if(route!="home") TopAppBar(title={Text(title,style=MaterialTheme.typography.titleMedium)},navigationIcon={
        TextButton(onClick={narrator.stop();nav.popBackStack()},modifier=Modifier.heightIn(min=48.dp)) {
            val color=MaterialTheme.colorScheme.primary
            Canvas(Modifier.size(20.dp)) {
                drawLine(color,Offset(size.width*.8f,size.height*.5f),Offset(size.width*.2f,size.height*.5f),3.dp.toPx(),StrokeCap.Round)
                drawLine(color,Offset(size.width*.2f,size.height*.5f),Offset(size.width*.5f,size.height*.2f),3.dp.toPx(),StrokeCap.Round)
                drawLine(color,Offset(size.width*.2f,size.height*.5f),Offset(size.width*.5f,size.height*.8f),3.dp.toPx(),StrokeCap.Round)
            }
            Text("Voltar")
        }
    })}) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when(val current=state) {
                PreferencesState.Loading -> Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally) {
                    CircularProgressIndicator();Text("Preparando seu mundinho…",Modifier.padding(16.dp))
                }
                is PreferencesState.Failed -> Text(current.message,Modifier.align(Alignment.Center).padding(24.dp))
                is PreferencesState.Ready -> {
                    val p=current.value
                    val voiceAvailable by narrator.available.collectAsStateWithLifecycle()
                    val haptic=LocalHapticFeedback.current
                    var lastGreeting by remember { mutableLongStateOf(0L) }
                    NavHost(navController=nav,startDestination="home",
                        enterTransition={if(p.effectiveAnimation==0f) EnterTransition.None else fadeIn(tween(250))},
                        exitTransition={if(p.effectiveAnimation==0f) ExitTransition.None else fadeOut(tween(250))}) {
                        composable("home") {
                            HomeScreen(p,onOpen={nav.navigate(it.route){launchSingleTop=true}},onParents={nav.navigate("parents"){launchSingleTop=true}},onMascot={
                                if(p.vibration) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val now=android.os.SystemClock.elapsedRealtime()
                                if(now-lastGreeting>4000) {narrator.speak("Oi, ${p.childName}! Vamos brincar?",p);lastGreeting=now}
                            })
                        }
                        composable("atelier") { PaintingScreen(p) }
                        composable("discover") { DiscoverScreen(p) { narrator.speak(it,p) } }
                        composable("dj") { DjScreen(p) }
                        composable("games") { GamesScreen(settings=p,onSpeak={narrator.speak(it,p)},onStopSpeech=narrator::stop) }
                        composable("camera") { CameraScreen(p) }
                        composable("art-library") { AdultArtLibraryScreen() }
                        composable("credits") { CreditsScreen() }
                        composable("recordings") { AdultRecordingsScreen(p) }
                        composable("parents") {
                            ParentsScreen(settings=p, voiceAvailable=voiceAvailable, update=model::update,
                                onOpenArtLibrary={nav.navigate("art-library")}, onOpenCredits={nav.navigate("credits")}, onOpenRecordings={nav.navigate("recordings")},
                                onTestVoice={narrator.speak("Oi, ${p.childName}! Vamos descobrir coisas novas, no seu ritmo.",p)})
                        }
                    }
                }
            }
        }
    }
    if(route in adultRoutes && !adultUnlocked) Dialog(
        onDismissRequest={nav.popBackStack()},
        properties=DialogProperties(usePlatformDefaultWidth=false, dismissOnClickOutside=false),
    ) {
        // Keep the adult screen composed behind a modal window: document-picker results
        // still reach the registered launcher after backgrounding has re-locked access.
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding()) {
                TextButton(onClick={nav.popBackStack()},modifier=Modifier.heightIn(min=48.dp)) { Text("Voltar às brincadeiras") }
                Box(Modifier.weight(1f)) { ParentGate { adultUnlocked=true } }
            }
        }
    }
    if(settings!=null) SessionReminder(settings,owner.lifecycle)
    if(error!=null) AlertDialog(onDismissRequest=model::dismissError,title={Text("Vamos tentar salvar de novo?")},text={Text(error.orEmpty())},confirmButton={TextButton(onClick=model::dismissError){Text("Entendi")}})
}

@Composable
private fun SessionReminder(settings: ChildPreferences,lifecycle: Lifecycle) {
    var elapsed by rememberSaveable(settings.sessionMinutes) {mutableIntStateOf(0)}
    var show by rememberSaveable {mutableStateOf(false)}
    LaunchedEffect(settings.sessionMinutes,lifecycle) {
        show=false
        if(settings.sessionMinutes>0) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while(true) {
                delay(1000)
                if(!show) {
                    elapsed++
                    if(elapsed>=settings.sessionMinutes*60) show=true
                }
            }
        }
    }
    if(show) AlertDialog(onDismissRequest={show=false;elapsed=0},title={Text("Que tal uma pausa?")},text={Text("Podemos descansar um pouquinho. Seu mundinho continua aqui quando você voltar.")},
        confirmButton={TextButton(onClick={show=false;elapsed=0}){Text("Tudo bem")}})
}
