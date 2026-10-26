package com.mundodedescobertas.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mundodedescobertas.app.data.*
import kotlin.math.roundToInt

@Composable
fun ParentsScreen(settings: ChildPreferences, voiceAvailable: Boolean, update: ((ChildPreferences) -> ChildPreferences) -> Unit, onOpenArtLibrary: () -> Unit, onOpenCredits: () -> Unit, onOpenRecordings: () -> Unit, onTestVoice: () -> Unit) {
    var name by rememberSaveable(settings.childName) { mutableStateOf(settings.childName) }
    var externalConfirm by remember { mutableStateOf(false) }
    var browserError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Text("Cada criança tem seu ritmo",style=MaterialTheme.typography.headlineMedium)
        Text("As preferências ficam apenas neste aparelho. O salvamento é automático, exceto o nome, que tem seu próprio botão.",style=MaterialTheme.typography.bodyLarge)
        SettingsGroup("Um mundo com seu nome") {
            OutlinedTextField(value=name,onValueChange={ name=it.filterNot(Char::isISOControl).take(24) },
                label={Text("Nome da criança")},singleLine=true,keyboardOptions=KeyboardOptions(capitalization=KeyboardCapitalization.Words),modifier=Modifier.fillMaxWidth())
            Button(onClick={update{it.copy(childName=name)}},enabled=name.trim().isNotEmpty() && name.trim()!=settings.childName,modifier=Modifier.heightIn(min=48.dp)) { Text("Salvar nome") }
        }
        SettingsGroup("Bem-estar e estímulos") {
            ToggleSetting("Modo Calminho", "Movimentos e sons mais suaves; mantém suas escolhas individuais.",settings.calmMode){v->update{it.copy(calmMode=v)}}
            ToggleSetting("Reduzir estímulos visuais", "Desliga o balanço do mascote e reduz a quantidade de elementos nas atividades.",settings.reducedStimuli){v->update{it.copy(reducedStimuli=v)}}
            SettingSlider("Intensidade das animações",settings.animationIntensity,onChange={v->update{it.copy(animationIntensity=v)}})
            Text("Sem flashes: efeitos piscantes não fazem parte do aplicativo.",style=MaterialTheme.typography.bodyMedium)
            ToggleSetting("Vibração ao cumprimentar o mascote", "Desativada por padrão. Respeita também as configurações do Android.",settings.vibration){v->update{it.copy(vibration=v)}}
        }
        SettingsGroup("Som e narração") {
            SettingSlider("Volume geral",settings.masterVolume,onChange={v->update{it.copy(masterVolume=v)}})
            ToggleSetting("Narração", "Falas curtas, acionadas pelo toque; sem elogios repetidos a cada ação.",settings.narrationEnabled){v->update{it.copy(narrationEnabled=v)}}
            SettingSlider("Volume da narração",settings.narrationVolume,onChange={v->update{it.copy(narrationVolume=v)}})
            SettingSlider("Volume dos efeitos",settings.effectsVolume,onChange={v->update{it.copy(effectsVolume=v)}})
            Text(if(voiceAvailable) "Voz pt-BR offline encontrada no aparelho. A naturalidade depende do mecanismo instalado." else "Nenhuma voz pt-BR offline foi encontrada. O app continua funcionando sem fala. Instale uma voz em português nas configurações de texto para voz do Android e reabra o app.",style=MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick=onTestVoice,enabled=voiceAvailable && settings.narrationEnabled && settings.effectiveNarrationVolume>0f,modifier=Modifier.heightIn(min=48.dp)){Text("Ouvir a voz")}
        }
        SettingsGroup("Ritmo das atividades") {
            Text("Escolha a velocidade, a dificuldade e a quantidade de elementos das brincadeiras. O Modo Calminho suaviza as opções sem apagar suas escolhas.",style=MaterialTheme.typography.bodyMedium)
            Text("Dificuldade",style=MaterialTheme.typography.titleMedium)
            Difficulty.entries.forEach { level ->
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
                    RadioButton(selected=settings.difficulty==level,onClick={update{it.copy(difficulty=level)}},modifier=Modifier.semantics{contentDescription="Dificuldade ${level.title}"})
                    Text(level.title)
                }
            }
            SettingSlider("Velocidade dos jogos",settings.gameSpeed,range=.25f..1f,onChange={v->update{it.copy(gameSpeed=v)}})
            SettingSlider("Elementos simultâneos",settings.maxElements.toFloat(),range=3f..12f,steps=8,valueText=settings.maxElements.toString(),onChange={v->update{it.copy(maxElements=v.roundToInt())}})
        }
        SettingsGroup("Tempo de sessão") {
            Text("Um lembrete suave convida a fazer uma pausa. Não bloqueia o app nem apaga progresso. Conta somente o tempo em primeiro plano. O lembrete pode ser dispensado a qualquer momento.",style=MaterialTheme.typography.bodyMedium)
            listOf(0,10,20,30,45,60).chunked(2).forEach { row ->
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    row.forEach { minutes ->
                        FilterChip(selected=settings.sessionMinutes==minutes,onClick={update{it.copy(sessionMinutes=minutes)}},
                            label={Text(if(minutes==0) "Sem lembrete" else "$minutes min")},modifier=Modifier.heightIn(min=48.dp).weight(1f))
                    }
                }
            }
        }
        SettingsGroup("Biblioteca e arquivos") {
            Text("Importe desenhos PNG, JPG ou a primeira página de um PDF. As pinturas ficam no aparelho; salvar na galeria é sempre uma ação explícita.",style=MaterialTheme.typography.bodyLarge)
            Button(onClick=onOpenArtLibrary,modifier=Modifier.heightIn(min=48.dp)) { Text("Desenhos e pinturas salvas") }
            OutlinedButton(onClick=onOpenRecordings,modifier=Modifier.heightIn(min=48.dp)) { Text("Gerenciar músicas gravadas") }
            OutlinedButton(onClick={externalConfirm=true},modifier=Modifier.heightIn(min=48.dp)) { Text("Conhecer desenhos no Wikimedia Commons") }
            Text("Os desenhos desse site têm licenças próprias. Confira os direitos antes de distribuir arquivos.",style=MaterialTheme.typography.bodyMedium)
        }
        SettingsGroup("Privacidade") {
            Text("Sem anúncios, compras, login ou rastreadores. A câmera é opcional e só é ligada em Caretas, depois da sua autorização. Não pedimos microfone, localização, internet ou acesso amplo ao armazenamento. Fotos e rostos são processados no aparelho. As preferências são locais e o backup automático está desativado. O link externo abre outro aplicativo e segue a política do site. Nenhum conteúdo é enviado pelo Mundinho do Vivi.",style=MaterialTheme.typography.bodyLarge)
        }
        SettingsGroup("Créditos e versão") {
            Text("Mundinho do Vivi · 0.5.0 · Teste em família\nArte vetorial original, atividades locais e detector facial com modelo incluído no aplicativo.\n\nA voz usa o pacote pt-BR offline do Android. A disponibilidade e a naturalidade dependem do aparelho.",style=MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick=onOpenCredits,modifier=Modifier.heightIn(min=48.dp)) { Text("Ver créditos e licenças") }
        }
        Spacer(Modifier.height(16.dp))
    }
    if(externalConfirm) AlertDialog(onDismissRequest={externalConfirm=false},title={Text("Abrir um site externo?")},
        text={Text("Você vai sair do Mundinho do Vivi e abrir commons.wikimedia.org no navegador. Essa ação precisa de internet e deve ser acompanhada por um adulto.")},
        confirmButton={TextButton(onClick={
            externalConfirm=false
            try { context.startActivity(Intent(Intent.ACTION_VIEW,"https://commons.wikimedia.org/wiki/Category:Coloring_books".toUri())) }
            catch(_: ActivityNotFoundException) {browserError=true}
        }){Text("Abrir navegador")}},dismissButton={TextButton(onClick={externalConfirm=false}){Text("Ficar no app")}})
    if(browserError) AlertDialog(onDismissRequest={browserError=false},title={Text("Nenhum navegador disponível")},text={Text("Você pode continuar usando o aplicativo normalmente.")},confirmButton={TextButton(onClick={browserError=false}){Text("Tudo bem")}})
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface,tonalElevation=1.dp) {
        Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(title,style=MaterialTheme.typography.titleLarge,modifier=Modifier.semantics{heading()})
            content()
        }
    }
}
@Composable
private fun ToggleSetting(title:String,detail:String,checked:Boolean,onChange:(Boolean)->Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(detail,style=MaterialTheme.typography.bodyMedium)}
        Switch(checked=checked,onCheckedChange=onChange,modifier=Modifier.semantics{contentDescription=title})
    }
}
@Composable
private fun SettingSlider(title:String,value:Float,range:ClosedFloatingPointRange<Float> = 0f..1f,steps:Int=0,valueText:String="${(value*100).roundToInt()}%",onChange:(Float)->Unit) {
    var draft by remember(value){mutableFloatStateOf(value)}
    Column {
        Text("$title · $valueText",style=MaterialTheme.typography.titleMedium)
        Slider(value=draft,onValueChange={draft=it},onValueChangeFinished={onChange(draft)},valueRange=range,steps=steps,
            modifier=Modifier.fillMaxWidth().semantics{contentDescription=title})
    }
}
