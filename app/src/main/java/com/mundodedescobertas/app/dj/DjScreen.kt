package com.mundodedescobertas.app.dj

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.*

@Composable
fun DjScreen(settings:ChildPreferences) {
    val context=LocalContext.current.applicationContext
    var engine by remember { mutableStateOf<DjAudioEngine?>(null) }
    var loading by remember{mutableStateOf(true)}
    var failure by remember{mutableStateOf<String?>(null)}
    var controls by remember{mutableStateOf(PcmMixer.State(gain=settings.effectiveEffectsVolume))}
    var category by rememberSaveable{mutableStateOf("Bateria")}
    var library by rememberSaveable{mutableStateOf(false)}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit){
        try{val bank=withContext(Dispatchers.IO){DjAudioEngine.loadBank(context)};engine=DjAudioEngine(context,bank)}
        catch(_:Exception){failure="Não conseguimos preparar o áudio. Volte e tente abrir o DJ novamente."}
        finally{loading=false}
    }
    LaunchedEffect(settings.effectiveEffectsVolume){controls=controls.copy(gain=settings.effectiveEffectsVolume)}
    LaunchedEffect(controls,engine){engine?.configure(controls)}
    DisposableEffect(lifecycle){
        val observer=LifecycleEventObserver{_,event->
            if(event==Lifecycle.Event.ON_STOP){controls=controls.copy(playA=false,playB=false,sequencer=false);engine?.setForeground(false)}
            if(event==Lifecycle.Event.ON_START)engine?.setForeground(true)
        }
        lifecycle.addObserver(observer)
        onDispose{lifecycle.removeObserver(observer);engine?.close()}
    }
    val state=engine?.status?.collectAsStateWithLifecycle()?.value?:DjAudioEngine.Status()
    if(library){
        BackHandler{library=false}
        Column(Modifier.fillMaxSize()){
            TextButton(onClick={library=false}){Text("Voltar à mesa")}
            RecordingList(settings,allowDelete=false,onPlay={engine?.playRecording(it)},onStop={engine?.pauseAll()},available=state.available)
        }
        return
    }
    val level by animateFloatAsState(if(settings.reducedStimuli)0f else state.level.coerceIn(0f,1f),label="Nível suave")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        Text("Seu pequeno estúdio",style=MaterialTheme.typography.headlineMedium)
        Text("Misture dois ritmos, toque nos pads e invente sua música.",style=MaterialTheme.typography.bodyLarge)
        if(loading){CircularProgressIndicator();Text("Preparando os sons offline…")}
        failure?.let{Text(it,color=MaterialTheme.colorScheme.error)}
        state.message?.let{Text(it,style=MaterialTheme.typography.bodyMedium)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
            Deck("A",controls.rhythmA,state.playA,if(settings.effectiveAnimation>0f)state.step else 0,Color(0xFFECE2FF),!loading&&state.available,
                Modifier.weight(1f),onPlay={controls=controls.copy(playA=!state.playA)},onRhythm={controls=controls.copy(rhythmA=it)})
            Deck("B",controls.rhythmB,state.playB,if(settings.effectiveAnimation>0f)state.step else 0,Color(0xFFDCF1EA),!loading&&state.available,
                Modifier.weight(1f),onPlay={controls=controls.copy(playB=!state.playB)},onRhythm={controls=controls.copy(rhythmB=it)})
        }
        Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceVariant){
            Column(Modifier.padding(18.dp)){
                Text("Mistura A ↔ B",style=MaterialTheme.typography.titleMedium)
                Slider(value=controls.crossfade,onValueChange={controls=controls.copy(crossfade=it)},modifier=Modifier.semantics{contentDescription="Mistura entre os decks A e B"})
                Text("Velocidade · ${controls.bpm.roundToInt()} BPM",style=MaterialTheme.typography.titleMedium)
                Slider(value=controls.bpm,onValueChange={controls=controls.copy(bpm=it)},valueRange=60f..170f,modifier=Modifier.semantics{contentDescription="Batidas por minuto"})
                Text("Volume da mesa · ${(controls.gain*100).roundToInt()}%",style=MaterialTheme.typography.titleMedium)
                Slider(value=controls.gain,onValueChange={controls=controls.copy(gain=it.coerceAtMost(settings.effectiveEffectsVolume))},
                    valueRange=0f..1f,modifier=Modifier.semantics{contentDescription="Volume da mesa limitado pelas preferências do responsável"})
                Text("Limite definido pelo responsável: ${(settings.effectiveEffectsVolume*100).roundToInt()}%",style=MaterialTheme.typography.bodyMedium)
                if(!settings.reducedStimuli)LinearProgressIndicator(progress={level},modifier=Modifier.fillMaxWidth().height(8.dp))
                OutlinedButton(onClick={controls=controls.copy(playA=false,playB=false,sequencer=false);engine?.pauseAll()},modifier=Modifier.heightIn(min=48.dp)){Text("Pausar tudo")}
            }
        }
        Text("Pads · toque para ouvir",style=MaterialTheme.typography.titleLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            DjCatalog.categories.forEach{cat->FilterChip(selected=category==cat,onClick={category=cat},label={Text(cat)},modifier=Modifier.heightIn(min=48.dp))}
        }
        DjCatalog.pads.indices.filter{DjCatalog.pads[it].category==category}.chunked(2).forEach{row->
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                row.forEach{index->
                    FilledTonalButton(onClick={controls=controls.copy(selectedPad=index);engine?.pad(index)},enabled=state.available&&!loading,
                        shape=RoundedCornerShape(20.dp),colors=ButtonDefaults.filledTonalButtonColors(containerColor=if(controls.selectedPad==index)Color(0xFFFFDEAB)else MaterialTheme.colorScheme.primaryContainer),
                        modifier=Modifier.weight(1f).heightIn(min=72.dp)){
                        Text(DjCatalog.pads[index].title,style=MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Text("Todos os 32 pads usam arquivos de áudio CC0 empacotados no app. Os sons eletrônicos são identificados como eletrônicos; o Corvo é o animal disponível nesta versão.",style=MaterialTheme.typography.bodyMedium)
        Surface(shape=RoundedCornerShape(24.dp),color=Color(0xFFFFEAD1)){
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                Text("Sequenciador de 16 passos",style=MaterialTheme.typography.titleLarge)
                Text("Som escolhido: ${DjCatalog.pads[controls.selectedPad].title}. Toque nos passos para criar uma sequência.",style=MaterialTheme.typography.bodyMedium)
                (0..15).chunked(4).forEach{row->
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        row.forEach{step->
                            val selected=step in controls.steps
                            val beat=state.step==step&&state.sequence
                            OutlinedButton(onClick={controls=controls.copy(steps=if(selected)controls.steps-step else controls.steps+step)},
                                colors=ButtonDefaults.outlinedButtonColors(containerColor=if(beat&&!settings.reducedStimuli)Color(0xFFE1D4F5)else if(selected)Color(0xFFFFD18D)else Color.White),
                                modifier=Modifier.weight(1f).heightIn(min=52.dp).semantics{contentDescription="Passo ${step+1}, ${if(selected)"ligado" else "desligado"}"}){Text("${step+1}")}
                        }
                    }
                }
                Button(onClick={controls=controls.copy(sequencer=!state.sequence)},enabled=state.available,modifier=Modifier.heightIn(min=48.dp)){Text(if(state.sequence)"Pausar sequência" else "Tocar sequência")}
                TextButton(onClick={controls=controls.copy(steps=emptySet())}){Text("Limpar passos")}
            }
        }
        Text("Guardar sua criação",style=MaterialTheme.typography.titleLarge)
        Text("A gravação captura a mistura digital, sem microfone. Duração máxima: 3 minutos.",style=MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
            Button(onClick={if(state.recording)engine?.stopRecording()else engine?.startRecording()},enabled=state.available,modifier=Modifier.heightIn(min=52.dp).weight(1f)){
                Text(if(state.recording)"Parar · ${state.seconds}s" else "Gravar mistura")}
            OutlinedButton(onClick={controls=controls.copy(playA=false,playB=false,sequencer=false);engine?.pauseAll();library=true},modifier=Modifier.heightIn(min=52.dp).weight(1f)){Text("Minhas músicas")}
        }
        Text("Suas músicas ficam guardadas aqui. Toque em Salvar cópia para colocar uma música na pasta Música do aparelho.",style=MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Deck(label:String,rhythm:Int,playing:Boolean,step:Int,color:Color,enabled:Boolean,modifier:Modifier,onPlay:()->Unit,onRhythm:(Int)->Unit){
    var open by remember{mutableStateOf(false)}
    Surface(color=color,shape=RoundedCornerShape(26.dp),modifier=modifier){
        Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Deck $label",style=MaterialTheme.typography.titleLarge)
            Canvas(Modifier.size(88.dp).clearAndSetSemantics{}){
                val c=Offset(size.width/2,size.height/2)
                drawCircle(Color(0xFF383449),size.minDimension*.47f,c)
                drawCircle(Color(0xFF797189),size.minDimension*.37f,c,style=androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawCircle(Color(0xFFF9D595),size.minDimension*.19f,c)
                val a=(if(playing)step.coerceAtLeast(0) else 0)*PI/8
                drawLine(Color.White,c,Offset(c.x+cos(a).toFloat()*size.width*.15f,c.y+sin(a).toFloat()*size.height*.15f),3.dp.toPx(),StrokeCap.Round)
            }
            Box{
                TextButton(onClick={open=true},modifier=Modifier.heightIn(min=48.dp)){Text(DjCatalog.rhythms[rhythm])}
                DropdownMenu(expanded=open,onDismissRequest={open=false},modifier=Modifier.heightIn(max=320.dp)){
                    DjCatalog.rhythms.forEachIndexed{i,name->DropdownMenuItem(text={Text(name)},onClick={onRhythm(i);open=false})}
                }
            }
            Button(onClick=onPlay,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(if(playing)"Pausar $label" else "Tocar $label")}
        }
    }
}

@Composable
fun AdultRecordingsScreen(settings:ChildPreferences=ChildPreferences()){
    val context=LocalContext.current.applicationContext
    var engine by remember{mutableStateOf<DjAudioEngine?>(null)}
    var error by remember{mutableStateOf<String?>(null)}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit){try{engine=DjAudioEngine(context,emptyList())}catch(_:Exception){error="Não foi possível iniciar a reprodução."}}
    DisposableEffect(lifecycle){
        val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_STOP)engine?.setForeground(false)else if(event==Lifecycle.Event.ON_START)engine?.setForeground(true)}
        lifecycle.addObserver(observer);onDispose{lifecycle.removeObserver(observer);engine?.close()}
    }
    LaunchedEffect(settings.effectiveEffectsVolume,engine){engine?.configure(PcmMixer.State(gain=settings.effectiveEffectsVolume))}
    val state=engine?.status?.collectAsStateWithLifecycle()?.value?:DjAudioEngine.Status()
    Column(Modifier.fillMaxSize()){
        error?.let{Text(it,Modifier.padding(16.dp))}
        state.message?.let{Text(it,Modifier.padding(16.dp))}
        RecordingList(settings,true,{engine?.playRecording(it)},{engine?.pauseAll()},state.available)
    }
}

@Composable
private fun RecordingList(settings:ChildPreferences,allowDelete:Boolean,onPlay:(File)->Unit,onStop:()->Unit,available:Boolean=true){
    val context=LocalContext.current.applicationContext
    val scope=rememberCoroutineScope()
    var files by remember{mutableStateOf(DjAudioEngine.recordingsDirectory(context).listFiles()?.filter{it.extension=="wav"}?.sortedByDescending{it.lastModified()}?:emptyList())}
    var pendingDelete by remember{mutableStateOf<File?>(null)}
    var message by remember{mutableStateOf<String?>(null)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text("Músicas do Vivi",style=MaterialTheme.typography.headlineMedium)
        message?.let{Text(it,style=MaterialTheme.typography.bodyLarge)}
        if(files.isEmpty())Text("Suas gravações vão aparecer aqui. Toque em Gravar mistura na mesa DJ para começar.")
        OutlinedButton(onClick=onStop,modifier=Modifier.heightIn(min=48.dp)){Text("Parar reprodução")}
        files.forEach{file->
            Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceVariant){
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Text(file.name,style=MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        Button(onClick={onPlay(file)},enabled=available&&settings.effectiveEffectsVolume>0f,modifier=Modifier.heightIn(min=48.dp)){Text("Ouvir")}
                        OutlinedButton(onClick={scope.launch{message=try{withContext(Dispatchers.IO){DjAudioEngine.export(context,file)};"Cópia salva em Música/MundinhoDoVivi."}catch(_:Exception){"Não foi possível salvar a cópia. Tente novamente."}}},modifier=Modifier.heightIn(min=48.dp)){Text("Salvar cópia")}
                    }
                    if(allowDelete)TextButton(onClick={pendingDelete=file}){Text("Apagar do app")}
                }
            }
        }
    }
    pendingDelete?.let{file->AlertDialog(onDismissRequest={pendingDelete=null},title={Text("Apagar esta gravação?")},text={Text("A cópia privada será apagada. Arquivos já exportados em Música permanecem no dispositivo.")},confirmButton={TextButton(onClick={onStop();if(file.delete()){files=files-file;message="Gravação apagada."}else message="Não foi possível apagar.";pendingDelete=null}){Text("Apagar")}},dismissButton={TextButton(onClick={pendingDelete=null}){Text("Manter")}})}
}
