package com.mundodedescobertas.app.games

import android.util.AtomicFile
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.*

private val gameTitles=listOf("Passarinho Aventureiro","Corrida das Cores","Mundinho de Blocos","Bolhas Mágicas","Jogo da Memória","Quebra-cabeça","Formas e Cores","Aventura dos Números")
private val gameDescriptions=listOf("Voe por caminhos entre as árvores","Três pistas, saltos e estrelas","Construa e guarde seu próprio mundo","Bolhas leves, sem relógio","Encontre as figuras que combinam","Arraste e encaixe um cenário original","Cada forma tem seu cantinho","Uma coleção de estrelas para contar")

@Composable
fun GamesScreen(settings:ChildPreferences,onSpeak:(String)->Unit,onStopSpeech:()->Unit = {}) {
    var chosen by rememberSaveable{mutableStateOf<Int?>(null)}
    var round by remember{mutableIntStateOf(0)}
    if(chosen==null) {
        LazyColumn(Modifier.fillMaxSize().testTag("games_library"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            item{Text("Uma aventura de cada vez",style=MaterialTheme.typography.headlineMedium)}
            item{Text("Escolha uma brincadeira. Você pode pausar ou voltar quando quiser.",style=MaterialTheme.typography.bodyLarge)}
            itemsIndexed(gameTitles){i,title->
                Card(onClick={chosen=i;round=0},shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=gameColors[i%gameColors.size].copy(alpha=.3f)),modifier=Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                        Canvas(Modifier.size(70.dp).clearAndSetSemantics{}) {
                            scale(size.width/200,size.height/200,Offset.Zero) {
                                when(i){0->bird(100f,100f,65f);1->character(100f,110f,55f);2->house(100f,110f,65f);3->{drawCircle(gameColors[1].copy(alpha=.5f),65f,Offset(100f,100f));drawCircle(Color.White,14f,Offset(80f,70f))};4->animal(0,100f,100f,65f);5->{house(100f,120f,50f);drawLine(Color.White,Offset(100f,20f),Offset(100f,190f),5f);drawLine(Color.White,Offset(20f,105f),Offset(190f,105f),5f)};6->shape(2,Offset(100f,100f),65f,gameColors[4]);else->{star(100f,100f,70f);label("5",100f,120f,65f)}}
                            }
                        }
                        Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(gameDescriptions[i],style=MaterialTheme.typography.bodyMedium)}
                    }
                }
            }
        }
        return
    }
    BackHandler{onStopSpeech();chosen=null}
    val context=LocalContext.current
    val selected=chosen!!
    val file=remember(context){context.filesDir.resolve("vivi-block-world.txt")}
    val engine=remember(selected,round) {
        when(selected) {
            0->FlightGame(settings);1->RunnerGame(settings)
            2->BlocksGame(settings,try{if(file.length()<=16384)file.readText()else""}catch(_:Exception){""})
            3->BubblesGame(settings);4->MemoryGame(settings);5->PuzzleGame(settings);6->ShapesGame(settings);else->NumbersGame(settings)
        }
    }
    var started by remember(engine){mutableStateOf(false)}
    var paused by remember(engine){mutableStateOf(false)}
    var frame by remember(engine){mutableIntStateOf(0)}
    val dragPosition=remember(engine){arrayOf(Offset.Zero)}
    var status by remember(engine){mutableStateOf(engine.status)}
    var message by remember(engine){mutableStateOf(engine.message)}
    val sounds=remember(context){GameSounds(context)}
    val speak by rememberUpdatedState(onSpeak)
    val currentSettings by rememberUpdatedState(settings)
    val stopSpeech by rememberUpdatedState(onStopSpeech)
    engine.feedback={text->sounds.play(currentSettings.effectiveEffectsVolume);if(text!=null)speak(text)}
    fun refresh(){frame++;status=engine.status;message=engine.message}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    fun saveWorld() {
        val blocks=engine as? BlocksGame ?: return
        if(!blocks.world.dirty)return
        val atomic=AtomicFile(file)
        var stream:java.io.FileOutputStream?=null
        try {
            stream=atomic.startWrite();stream.write(blocks.world.snapshot().toByteArray());atomic.finishWrite(stream);blocks.world.dirty=false
        } catch(_:Exception) {if(stream!=null)atomic.failWrite(stream);blocks.message="Não conseguimos salvar agora. Seu mundo anterior foi mantido."}
    }
    DisposableEffect(lifecycle,engine) {
        val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_STOP){paused=true;sounds.stop();stopSpeech();saveWorld()}}
        lifecycle.addObserver(observer)
        onDispose{saveWorld();stopSpeech();lifecycle.removeObserver(observer)}
    }
    DisposableEffect(sounds){onDispose{sounds.close()}}
    LaunchedEffect(engine,started,paused,lifecycle) {
        if(started && !paused)lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last=0L;var uiTime=0f;var saveTime=0f
            while(isActive) {
                withFrameNanos{time->
                    val dt=if(last==0L)0f else ((time-last)/1_000_000_000f).coerceIn(0f,.04f)
                    last=time;val wasAnimating=engine.animating;engine.update(dt);if(wasAnimating || engine.animating)frame++;uiTime+=dt;saveTime+=dt
                    if(uiTime>.15f){status=engine.status;message=engine.message;uiTime=0f}
                    if(saveTime>2f){saveWorld();saveTime=0f}
                }
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={saveWorld();stopSpeech();chosen=null}){Text("Ver jogos")}
            TextButton(onClick={paused=!paused;sounds.stop();stopSpeech()},enabled=started){Text(if(paused)"Continuar"else"Pausar")}
            TextButton(onClick={saveWorld();stopSpeech();round++}){Text(if(selected==2)"Retomar"else"Recomeçar")}
        }
        Text(gameTitles[selected],style=MaterialTheme.typography.titleMedium,modifier=Modifier.semantics{heading()})
        Text(status,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.testTag("game_status"))
        Box(Modifier.fillMaxWidth().weight(1f).heightIn(min=180.dp)) {
            Canvas(Modifier.fillMaxSize().testTag("game_canvas").semantics{contentDescription="$status. $message"}
                .pointerInput(engine,started,paused){if(started&&!paused)detectTapGestures{p->engine.tap(Offset(p.x/size.width*1000,p.y/size.height*1000));refresh()}}
                .pointerInput(engine,started,paused){if(started&&!paused)detectDragGestures(
                    onDragStart={p->dragPosition[0]=Offset(p.x/size.width*1000,p.y/size.height*1000);engine.down(dragPosition[0]);refresh()},
                    onDragEnd={engine.release(dragPosition[0]);refresh()},
                    onDragCancel={engine.release(dragPosition[0]);refresh()},
                    onDrag={change,delta->change.consume();dragPosition[0]=Offset(change.position.x/size.width*1000,change.position.y/size.height*1000);engine.drag(dragPosition[0],Offset(delta.x/size.width*1000,delta.y/size.height*1000));refresh()})}) {
                @Suppress("UNUSED_EXPRESSION")
                frame
                scale(size.width/1000f,size.height/1000f,Offset.Zero){engine.draw(this)}
            }
            if(!started || paused)Surface(Modifier.align(Alignment.Center).padding(24.dp),shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.96f)) {
                Column(Modifier.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    Text(if(!started)"Vamos brincar?"else"Uma pausa tranquila",style=MaterialTheme.typography.titleLarge)
                    Button(onClick={started=true;paused=false;onSpeak(engine.message)}){Text(if(!started)"Começar"else"Continuar brincando")}
                }
            }
        }
        Text(message,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.heightIn(min=44.dp))
        engine.actions.chunked(3).forEach {row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            row.forEach {label->val index=engine.actions.indexOf(label)
                OutlinedButton(onClick={engine.action(index);refresh()},enabled=started&&!paused,contentPadding=PaddingValues(6.dp),modifier=Modifier.weight(1f).heightIn(min=48.dp)){Text(label,style=MaterialTheme.typography.labelMedium)}
            }
        }}
        Spacer(Modifier.height(4.dp))
    }
}
