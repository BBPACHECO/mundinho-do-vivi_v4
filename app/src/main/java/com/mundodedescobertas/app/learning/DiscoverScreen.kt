package com.mundodedescobertas.app.learning

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mundodedescobertas.app.data.ChildPreferences
import kotlin.math.*
import kotlin.random.Random

private val cardColors=listOf(Color(0xFFFFE6D7),Color(0xFFEAE1FA),Color(0xFFFFEFC4),Color(0xFFDFF0E5),Color(0xFFDDEDF9))

/** Tudo é local; a narração é uma ação explícita e delegada ao mecanismo offline do app. */
@Composable
fun DiscoverScreen(settings: ChildPreferences, onSpeak: (String) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var letterString by rememberSaveable { mutableStateOf("") }
    var wordName by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableIntStateOf(-1) }
    val selectedWord=LearningCatalog.words.firstOrNull{it.word==wordName}
    BackHandler(enabled=letterString.isNotEmpty() || number>=0) { letterString="";number=-1 }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp).testTag("discover_screen"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("Cada descoberta começa com um toque",style=MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("Letras","Números","Contar").forEachIndexed { index,label ->
                FilterChip(selected=tab==index,onClick={tab=index;letterString="";number=-1},label={Text(label)},modifier=Modifier.weight(1f).heightIn(min=52.dp))
            }
        }
        when(tab) {
            0 -> {
                if(letterString.isEmpty()) {
                    Text("Toque em uma letra. Depois, escolha uma das dez palavras para ver e ouvir.",style=MaterialTheme.typography.bodyLarge)
                    ('A'..'Z').toList().chunked(5).forEach { row ->
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            row.forEach { letter ->
                                Card(onClick={letterString=letter.toString();onSpeak(LearningCatalog.letterNames.getValue(letter))},colors=CardDefaults.cardColors(containerColor=cardColors[(letter-'A')%5]),shape=RoundedCornerShape(20.dp),modifier=Modifier.weight(1f).heightIn(min=72.dp).testTag("letter_$letter")) {
                                    Box(Modifier.fillMaxWidth().padding(vertical=14.dp),contentAlignment=Alignment.Center){Text(letter.toString(),fontSize=30.sp,fontWeight=FontWeight.Bold)}
                                }
                            }
                            repeat(5-row.size){Spacer(Modifier.weight(1f))}
                        }
                    }
                } else {
                    val letter=letterString.first()
                    TextButton(onClick={letterString=""},modifier=Modifier.heightIn(min=48.dp)){Text("Ver todas as letras")}
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                        Text(letter.toString(),fontSize=68.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                        Column {Text("Vamos explorar a letra $letter",style=MaterialTheme.typography.titleLarge);Text("Dez palavras para descobrir",style=MaterialTheme.typography.bodyLarge)}
                    }
                    if(letter in listOf('K','W','Y')) Text("Essa letra aparece bastante em nomes próprios e palavras vindas de outras línguas. Os nomes abaixo pertencem a personagens inventados.",style=MaterialTheme.typography.bodyMedium)
                    LearningCatalog.forLetter(letter).chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            row.forEach { word ->
                                Card(onClick={wordName=word.word;onSpeak(word.narration)},colors=CardDefaults.cardColors(containerColor=cardColors[(word.word.hashCode() and Int.MAX_VALUE)%5]),modifier=Modifier.weight(1f).testTag("word_${word.word}"),shape=RoundedCornerShape(24.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                        LearningIllustration(word,Modifier.size(94.dp).clearAndSetSemantics{})
                                        Text(word.word,style=MaterialTheme.typography.titleMedium)
                                        if(word.properName) Text("Nome próprio",style=MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                if(number<0) {
                    Text("Do zero ao trinta. Toque em um número para contar estrelas, uma de cada vez.",style=MaterialTheme.typography.bodyLarge)
                    (0..30).toList().chunked(5).forEach { row ->
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            row.forEach { n ->
                                Card(onClick={number=n;onSpeak("Esse é o número ${LearningCatalog.numberNames[n]}.")},colors=CardDefaults.cardColors(containerColor=cardColors[n%5]),modifier=Modifier.weight(1f).heightIn(min=68.dp).testTag("number_$n")) {
                                    Box(Modifier.fillMaxWidth().padding(vertical=15.dp),contentAlignment=Alignment.Center){Text(n.toString(),style=MaterialTheme.typography.headlineMedium)}
                                }
                            }
                            repeat(5-row.size){Spacer(Modifier.weight(1f))}
                        }
                    }
                } else {
                    TextButton(onClick={number=-1},modifier=Modifier.heightIn(min=48.dp)){Text("Escolher outro número")}
                    Text(number.toString(),fontSize=76.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                    Text(LearningCatalog.numberNames[number].replaceFirstChar{it.uppercase()},style=MaterialTheme.typography.titleLarge)
                    CountObjects(number,settings,onSpeak)
                }
            }
            else -> CountingGame(settings,onSpeak)
        }
        Spacer(Modifier.height(16.dp))
    }
    if(selectedWord!=null) AlertDialog(onDismissRequest={wordName=""},title={Text(selectedWord.word)},text={
        Column(verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            LearningIllustration(selectedWord,Modifier.size(172.dp))
            Text(selectedWord.sentence,style=MaterialTheme.typography.bodyLarge)
            val caption=illustrationCaption(selectedWord)
            if(caption!=null)Text(caption,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    },confirmButton={TextButton(onClick={onSpeak(selectedWord.narration)},modifier=Modifier.heightIn(min=48.dp)){Text("Ouvir novamente")}},dismissButton={TextButton(onClick={wordName=""},modifier=Modifier.heightIn(min=48.dp)){Text("Voltar às palavras")}})
}

/** Quando o conceito é mais amplo que o desenho, a legenda identifica o exemplo exato. */
private fun illustrationCaption(word:LearningWord):String? = when {
    word.properName -> "Personagem original com nome fictício. Pessoas com esse nome podem ter qualquer aparência."
    word.art=="artkit" -> "Desenho de um kit de materiais de pintura."
    word.art=="triangle" -> "Ilustração do instrumento musical triângulo."
    word.art=="spoon" -> "Ilustração de uma colher, um exemplo de utensílio."
    word.art=="number" -> "Ilustração do algarismo sete, um exemplo de número."
    word.art=="dancer" -> "Ilustração estilizada de uma pessoa dançando."
    word.art=="karate" -> "Ilustração estilizada de uma pessoa com uniforme e faixa de karatê."
    word.art=="shirt" -> "Ilustração de uma camiseta de uniforme; os uniformes podem ser diferentes."
    word.art=="garden" -> "Ilustração de um canteiro de flores. Hortas e quintais também podem ter outros tipos de plantas."
    word.word=="Xaréu" -> "Ilustração estilizada de um peixe; não serve para identificar espécies."
    word.art=="mint" || word.art=="fern" -> "Ramo de planta estilizado; o desenho não serve para identificação botânica."
    word.art=="zebu" -> "Rosto de bovino estilizado; a figura não distingue raças."
    word.art=="zoo" -> "Ilustração estilizada de animais. Não representa uma instituição real."
    else -> null
}

@Composable
private fun CountObjects(total:Int,settings:ChildPreferences,onSpeak:(String)->Unit,resetKey:Int=total) {
    var selected by rememberSaveable(total,resetKey){mutableStateOf(intArrayOf())}
    var page by rememberSaveable(total,resetKey){mutableIntStateOf(0)}
    val perPage=if(settings.calmMode||settings.reducedStimuli)settings.effectiveElements.coerceIn(3,10) else 10
    val pageCount=maxOf(1,(total+perPage-1)/perPage)
    LaunchedEffect(pageCount) { if(page>=pageCount)page=0 }
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        if(total==0) {
            Surface(shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.primaryContainer){Text("Nenhuma estrela. Isto é zero!",Modifier.fillMaxWidth().padding(24.dp),style=MaterialTheme.typography.titleLarge)}
        } else {
            Text("Há ${LearningCatalog.numberNames[total]} ${if(total==1)"estrela" else "estrelas"}. Toque para contar.",style=MaterialTheme.typography.bodyLarge)
            if(pageCount>1)Text("Grupo ${page+1} de $pageCount · a quantidade total continua sendo $total",style=MaterialTheme.typography.bodyMedium)
            val indices=(page*perPage until minOf(total,(page+1)*perPage)).toList()
            indices.chunked(5).forEach{row->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    row.forEach{index->
                        val done=index in selected
                        val targetScale=if(done&&settings.effectiveAnimation>0f)1.04f else 1f
                        val scale by animateFloatAsState(targetScale,tween(if(settings.effectiveAnimation>0f)220 else 0),label="Estrela contada")
                        Card(onClick={
                            val ordinal=if(done)selected.indexOf(index)+1 else selected.size+1
                            if(!done)selected=selected+index
                            onSpeak(LearningCatalog.numberNames[ordinal])
                        },colors=CardDefaults.cardColors(containerColor=if(done)Color(0xFFDFF0E5) else Color(0xFFFFEFC4)),modifier=Modifier.weight(1f).heightIn(min=64.dp).scale(scale).testTag("count_object_$index").semantics{contentDescription="Estrela ${index+1}${if(done)", contada como ${selected.indexOf(index)+1}" else ""}"}){
                            Box(Modifier.fillMaxWidth().padding(8.dp),contentAlignment=Alignment.Center){
                                if(done)Text((selected.indexOf(index)+1).toString(),style=MaterialTheme.typography.titleLarge) else CountingStar(Modifier.size(36.dp))
                            }
                        }
                    }
                    repeat(5-row.size){Spacer(Modifier.weight(1f))}
                }
            }
            Text("${selected.size} de $total contadas",style=MaterialTheme.typography.titleMedium,modifier=Modifier.testTag("count_progress"))
            if(pageCount>1)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                OutlinedButton(onClick={page--},enabled=page>0,modifier=Modifier.heightIn(min=48.dp)){Text("Anterior")}
                OutlinedButton(onClick={page++},enabled=page<pageCount-1,modifier=Modifier.heightIn(min=48.dp)){Text("Próximo grupo")}
            }
            if(selected.size==total)Text("Você contou todas as estrelas!",style=MaterialTheme.typography.titleMedium)
            TextButton(onClick={selected=intArrayOf();page=0},modifier=Modifier.heightIn(min=48.dp)){Text("Contar de novo")}
        }
    }
}

@Composable
private fun CountingStar(modifier:Modifier) {
    Canvas(modifier.clearAndSetSemantics{}) {
        val radius=min(size.width,size.height)*.47f
        val p=Path()
        repeat(10){i->val t=i*PI/5-PI/2;val r=if(i%2==0)radius else radius*.46f;val x=size.width/2+(cos(t)*r).toFloat();val y=size.height/2+(sin(t)*r).toFloat();if(i==0)p.moveTo(x,y)else p.lineTo(x,y)}
        p.close();drawPath(p,Color(0xFFCA9746))
    }
}

@Composable
private fun CountingGame(settings:ChildPreferences,onSpeak:(String)->Unit) {
    var round by rememberSaveable {mutableIntStateOf(0)}
    val maxNumber=when(settings.difficulty.ordinal){0->5;1->10;else->30}
    val target=remember(round,maxNumber){Random(round*79+23).nextInt(maxNumber+1)}
    val answers=remember(round,target,maxNumber){
        val candidates=(0..maxOf(5,maxNumber)).filter{it!=target}.shuffled(Random(round+11)).take(2)
        (candidates+target).shuffled(Random(round+71))
    }
    var feedback by rememberSaveable(round,target){mutableStateOf("")}
    var solved by rememberSaveable(round,target){mutableStateOf(false)}
    Text("Quantas estrelas há neste grupo?",style=MaterialTheme.typography.titleLarge)
    Text("Conte com calma. Você pode tocar nas estrelas e tentar quantas vezes quiser.",style=MaterialTheme.typography.bodyLarge)
    // Este exercício inclui uma contagem guiada: não esconde a quantidade para punir tentativas.
    CountObjects(target,settings,onSpeak,round)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        answers.forEach{n->
            FilledTonalButton(onClick={
                solved=n==target
                feedback=if(solved)"Isso! São ${LearningCatalog.numberNames[target]}." else "Vamos contar juntos? Você pode tentar outra vez."
                onSpeak(feedback)
            },modifier=Modifier.weight(1f).heightIn(min=64.dp).testTag("count_choice_$n")) {Text(n.toString(),style=MaterialTheme.typography.headlineMedium)}
        }
    }
    if(feedback.isNotEmpty())Text(feedback,style=MaterialTheme.typography.bodyLarge,modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite})
    Button(onClick={round++},modifier=Modifier.heightIn(min=52.dp)){Text(if(solved)"Vamos contar mais?" else "Outro grupo")}
}
