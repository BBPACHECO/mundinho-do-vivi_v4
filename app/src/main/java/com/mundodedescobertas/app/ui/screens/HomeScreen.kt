package com.mundodedescobertas.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.ui.components.*
import com.mundodedescobertas.app.ui.theme.*
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/** Cinco destinos infantis estáveis, compartilhados pela navegação e pelos testes. */
enum class ActivityArea(val route: String, val title: String, val subtitle: String, val color: Color, val scene: Scene) {
    ATELIER("atelier", "Meu Ateliê", "Um cantinho para criar", Color(0xFFFFE6D7), Scene.ATELIER),
    DJ("dj", "Meu DJ", "Sons para inventar", Color(0xFFEAE1FA), Scene.DJ),
    DISCOVER("discover", "Descobrir", "Letras, números e histórias", Color(0xFFFFEFC4), Scene.DISCOVER),
    GAMES("games", "Meus Joguinhos", "Pequenas grandes aventuras", Color(0xFFDFF0E5), Scene.GAMES),
    CAMERA("camera", "Caretas Divertidas", "Um mundo de caras engraçadas", Color(0xFFDDEDF9), Scene.CAMERA),
}

@Composable
fun HomeScreen(settings: ChildPreferences, onOpen: (ActivityArea) -> Unit, onParents: () -> Unit, onMascot: () -> Unit) {
    var happy by remember { mutableStateOf(false) }
    var tilt by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(settings.effectiveAnimation, lifecycle) {
        tilt = false
        if (settings.effectiveAnimation > 0f) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try { while (true) { delay(2200); tilt = !tilt } } finally { tilt = false }
        }
    }
    LaunchedEffect(happy) { if (happy) { delay(1600); happy = false } }
    val angle by animateFloatAsState(if (tilt) 4f * settings.effectiveAnimation else -4f * settings.effectiveAnimation, tween(1400), label="Nuvino balança suavemente")
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        contentPadding = PaddingValues(start=20.dp,end=20.dp,bottom=24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().testTag("home_areas"),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.End) {
                TextButton(onClick=onParents, modifier=Modifier.heightIn(min=48.dp)) { Text("Responsáveis", style=MaterialTheme.typography.bodyMedium) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally) {
                Text("Mundinho do Vivi", style=MaterialTheme.typography.headlineLarge, modifier=Modifier.semantics { heading() })
                Spacer(Modifier.height(4.dp))
                Text("Oi, ${settings.childName}! Vamos brincar?", style=MaterialTheme.typography.bodyLarge)
                Illustration(Scene.MASCOT,Modifier.padding(top=8.dp).size(100.dp).rotate(angle)
                    .semantics { contentDescription="Nuvino, nosso mascote. Toque para dizer oi."; role=Role.Button }
                    .clickable { happy=true; onMascot() },happy)
                Text(if(happy) "Que bom ter você aqui!" else "Um mundo de descobertas, no seu ritmo.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
        }
        items(ActivityArea.entries, key={it.route}) { area ->
            Card(onClick={onOpen(area)}, shape=RoundedCornerShape(28.dp),
                colors=CardDefaults.cardColors(containerColor=area.color),
                elevation=CardDefaults.cardElevation(defaultElevation=1.dp),
                modifier=Modifier.fillMaxWidth().semantics(mergeDescendants=true) { contentDescription=area.title }) {
                Row(Modifier.padding(horizontal=16.dp,vertical=8.dp).heightIn(min=74.dp),verticalAlignment=Alignment.CenterVertically) {
                    Illustration(area.scene,Modifier.size(72.dp).clearAndSetSemantics {})
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(area.title,style=MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(3.dp))
                        Text(area.subtitle,style=MaterialTheme.typography.bodyMedium,color=Ink.copy(alpha=.85f))
                    }
                }
            }
        }
        item(span={GridItemSpan(maxLineSpan)}) {
            Text(if(settings.calmMode) "Modo Calminho ligado · Sem pressa, sem barulho demais" else "Feito para explorar com carinho",style=MaterialTheme.typography.bodyMedium,
                color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=12.dp))
        }
    }
}

@Preview(name="Início · 393 dp", widthDp=393, heightDp=852, showBackground=true)
@Composable
private fun HomePreview() {
    ViviTheme { Surface { HomeScreen(ChildPreferences(reducedStimuli=true), {}, {}, {}) } }
}
