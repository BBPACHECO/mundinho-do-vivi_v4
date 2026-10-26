package com.mundodedescobertas.app.painting

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt

private val Palette = listOf(
    0xFFEF5350,0xFFFF8A80,0xFFE91E63,0xFFF8BBD0,0xFFAB47BC,0xFFE1BEE7,0xFF7E57C2,0xFFD1C4E9,
    0xFF5C6BC0,0xFFC5CAE9,0xFF4285F4,0xFF90CAF9,0xFF29B6F6,0xFFB3E5FC,0xFF26C6DA,0xFFB2EBF2,
    0xFF26A69A,0xFFB2DFDB,0xFF66BB6A,0xFFC8E6C9,0xFF9CCC65,0xFFDCEDC8,0xFFD4E157,0xFFF0F4C3,
    0xFFFFEE58,0xFFFFF9C4,0xFFFFCA28,0xFFFFECB3,0xFFFFA726,0xFFFFE0B2,0xFFFF7043,0xFFFFCCBC,
    0xFF8D6E63,0xFFD7CCC8,0xFF5D4037,0xFFA1887F,0xFF78909C,0xFFCFD8DC,0xFF455A64,0xFFB0BEC5,
    0xFF212121,0xFF616161,0xFF9E9E9E,0xFFE0E0E0,0xFFFFFFFF,0xFFFFF3E0,0xFFFFD9DB,0xFFC9E7DD,
).map { Color(it) }

private val PaletteNames=listOf(
    "Vermelho", "Coral claro", "Rosa forte", "Rosa pastel", "Violeta", "Lilás claro", "Roxo", "Lavanda",
    "Azul índigo", "Azul lavanda", "Azul", "Azul céu", "Azul celeste", "Azul gelo", "Turquesa", "Turquesa claro",
    "Verde água", "Menta", "Verde", "Verde claro", "Verde folha", "Verde pastel", "Lima", "Lima claro",
    "Amarelo", "Amarelo claro", "Dourado", "Amarelo creme", "Laranja", "Pêssego", "Laranja coral", "Pêssego claro",
    "Marrom", "Bege", "Marrom escuro", "Castanho", "Cinza azulado", "Cinza gelo", "Cinza escuro azulado", "Cinza claro azulado",
    "Preto", "Cinza escuro", "Cinza", "Cinza claro", "Branco", "Creme", "Rosa suave", "Verde suave",
)

@Composable
fun PaintingScreen(settings:ChildPreferences) {
    val model:PaintingViewModel=viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    val context=LocalContext.current
    DisposableEffect(model){onDispose{model.finishStroke()}}
    var tool by remember{mutableStateOf(PaintTool.FILL)}
    var color by remember{mutableStateOf(Palette.first())}
    var width by remember{mutableFloatStateOf(15f)}
    var stamp by remember{mutableIntStateOf(0)}
    var library by remember{mutableStateOf(false)}
    var custom by remember{mutableStateOf(false)}
    var confirmClear by remember{mutableStateOf(false)}
    var zoom by remember(state.selectedId){mutableFloatStateOf(1f)}
    var pan by remember(state.selectedId){mutableStateOf(Offset.Zero)}
    Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick={library=true},modifier=Modifier.heightIn(min=48.dp)){Text("Desenhos")}
            TextButton(onClick={model.undo()},enabled=state.canUndo){Text("Desfazer")}
            TextButton(onClick={model.redo()},enabled=state.canRedo){Text("Refazer")}
        }
        // Always at the top of the painting surface, independently of artwork zoom.
        LazyRow(contentPadding=PaddingValues(horizontal=8.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)) {
            items(Palette.chunked(2).withIndex().toList()){(group,colors)->
                Column { colors.forEachIndexed{index,swatch->
                    Box(Modifier.size(48.dp).clip(CircleShape).clickable{color=swatch}
                        .semantics{contentDescription="${PaletteNames[group*2+index]}${if(swatch==color) ", selecionada" else ""}"},contentAlignment=Alignment.Center) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(swatch).border(if(swatch==color)3.dp else 1.dp,if(swatch==color)MaterialTheme.colorScheme.primary else Color.Gray,CircleShape))
                    }
                }}
            }
            item {Column{
                TextButton(onClick={custom=true},modifier=Modifier.height(44.dp)){Text("Minha cor")}
                Box(Modifier.padding(8.dp).size(28.dp).clip(CircleShape).background(color).border(1.dp,Color.Gray,CircleShape))
            }}
        }
        LazyRow(contentPadding=PaddingValues(horizontal=8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(PaintTool.entries){item->FilterChip(selected=tool==item,onClick={tool=item},label={Text(item.title)},modifier=Modifier.heightIn(min=48.dp))}
        }
        if(tool==PaintTool.BRUSH || tool==PaintTool.ERASER || tool==PaintTool.STAMP) {
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){
                Text("Traço",style=MaterialTheme.typography.bodyMedium)
                Slider(value=width,onValueChange={width=it},valueRange=4f..45f,modifier=Modifier.weight(1f).semantics{contentDescription="Espessura do traço"})
                if(tool==PaintTool.STAMP)TextButton(onClick={stamp=(stamp+1)%3}){Text(listOf("Estrela","Coração","Flor")[stamp])}
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal=10.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFEAE3F0))) {
            val bitmap=state.bitmap
            if(bitmap!=null && !state.loading) {
                val image=remember(bitmap){bitmap.asImageBitmap()}
                val revision=state.revision
                Canvas(Modifier.fillMaxSize().semantics{contentDescription="Desenho para pintar. Um dedo pinta; dois dedos ampliam e movem."}
                    .pointerInput(tool,color,width,stamp,bitmap) {
                        awaitEachGesture {
                            val down=awaitFirstDown(requireUnconsumed=false)
                            val board=min(size.width,size.height).toFloat()
                            val origin=Offset((size.width-board)/2,(size.height-board)/2)
                            fun location(position:Offset):PointF {
                                val center=origin+Offset(board/2,board/2)
                                val mapped=(position-center-pan)/zoom+Offset(board/2,board/2)
                                return PointF(mapped.x/board*640,mapped.y/board*640)
                            }
                            fun inside(p:PointF)=p.x in 0f..639f && p.y in 0f..639f
                            var previous:PointF?=null
                            var movedToZoom=false
                            var initial=true
                            var event:PointerEvent
                            do {
                                event=awaitPointerEvent()
                                val fingers=event.changes.count{it.pressed}
                                if(fingers>=2){
                                    movedToZoom=true;model.finishStroke();previous=null
                                    val factor=event.calculateZoom();zoom=(zoom*factor).coerceIn(1f,4f)
                                    pan=(pan+event.calculatePan()).let{Offset(it.x.coerceIn(-board*(zoom-1)/2,board*(zoom-1)/2),it.y.coerceIn(-board*(zoom-1)/2,board*(zoom-1)/2))}
                                    event.changes.forEach{it.consume()}
                                } else if(!movedToZoom) {
                                    val pointer=event.changes.firstOrNull{it.id==down.id}
                                    if(pointer!=null){
                                        val point=location(if(initial)down.position else pointer.position)
                                        if(inside(point) && (initial || ((tool==PaintTool.BRUSH || tool==PaintTool.ERASER) && pointer.pressed))) {
                                            model.editPoint(tool,point.x,point.y,color.toArgb(),width,stamp,previous)
                                            previous=point
                                        }
                                        pointer.consume()
                                    }
                                }
                                initial=false
                            } while(event.changes.any{it.pressed})
                            model.finishStroke()
                        }
                    }) {
                    @Suppress("UNUSED_VARIABLE") val invalidate=revision
                    val board=min(size.width,size.height)
                    val x=(size.width-board)/2;val y=(size.height-board)/2
                    withTransform({translate(x+board/2+pan.x,y+board/2+pan.y);scale(zoom,zoom,Offset.Zero);translate(-board/2,-board/2)}) {
                        drawImage(image,dstOffset=IntOffset.Zero,dstSize=IntSize(board.roundToInt(),board.roundToInt()))
                    }
                }
            }
            if(state.loading)CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={zoom=1f;pan=Offset.Zero}){Text("Ajustar zoom")}
            TextButton(onClick={confirmClear=true},enabled=state.bitmap!=null){Text("Limpar")}
            Button(onClick={model.saveGallery()},enabled=state.bitmap!=null && !state.loading){Text("Salvar")}
        }
        Text("Seu rascunho fica neste aparelho · Dois dedos para ampliar",style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(horizontal=12.dp,vertical=4.dp))
    }
    if(library)DrawingPicker(onDismiss={library=false},onSelect={model.select(it);library=false})
    if(custom)CustomColorDialog(color,onDismiss={custom=false},onSelect={color=it;custom=false})
    if(confirmClear)AlertDialog(onDismissRequest={confirmClear=false},title={Text("Começar de novo?")},text={Text("As cores deste desenho serão limpas. Você ainda poderá desfazer.")},confirmButton={TextButton(onClick={model.clear();confirmClear=false}){Text("Limpar desenho")}},dismissButton={TextButton(onClick={confirmClear=false}){Text("Continuar pintando")}})
    state.message?.let{message->AlertDialog(onDismissRequest={model.message(null)},title={Text("Meu Ateliê")},text={Text(message)},confirmButton={TextButton(onClick={model.message(null)}){Text("Tudo bem")}})}
}

@Composable
private fun DrawingPicker(onDismiss:()->Unit,onSelect:(String)->Unit) {
    val context=LocalContext.current
    val imported=remember{PaintingStore(context).importedDrawings()}
    var category by remember{mutableStateOf("Todos")}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Escolha seu desenho")},text={Column(Modifier.heightIn(max=520.dp)) {
        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(listOf("Todos")+DrawingCatalog.originals.map{it.category}.distinct()+if(imported.isNotEmpty())listOf("Importados")else emptyList()){cat->
                FilterChip(selected=cat==category,onClick={category=cat},label={Text(cat)})
            }
        }
        LazyVerticalGrid(columns=GridCells.Fixed(2),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            items(DrawingCatalog.originals.filter{category=="Todos" || it.category==category},key={it.id}){item->
                Card(onClick={onSelect(item.id)}){Column(Modifier.padding(8.dp)) {
                    val bitmap=remember(item.id){DrawingCatalog.render(item,180)}
                    Image(bitmap.asImageBitmap(),item.title,Modifier.fillMaxWidth().aspectRatio(1f))
                    Text(item.title,style=MaterialTheme.typography.bodyMedium)
                }}
            }
            if(category=="Todos" || category=="Importados")items(imported,key={it.name}){file->
                Card(onClick={onSelect("import_${file.nameWithoutExtension}")}){Column(Modifier.padding(8.dp)) {
                    var image by remember(file.path){mutableStateOf<android.graphics.Bitmap?>(null)}
                    LaunchedEffect(file.path){image=withContext(Dispatchers.IO){runCatching{PaintingStore.decodeBounded(file)}.getOrNull()}}
                    image?.let{Image(it.asImageBitmap(),"Desenho importado",Modifier.fillMaxWidth().aspectRatio(1f))}
                    Text("Desenho da família",style=MaterialTheme.typography.bodyMedium)
                }}
            }
        }
    }},confirmButton={TextButton(onClick=onDismiss){Text("Voltar a pintar")}})
}

@Composable
private fun CustomColorDialog(initial:Color,onDismiss:()->Unit,onSelect:(Color)->Unit) {
    var r by remember{mutableFloatStateOf(initial.red)};var g by remember{mutableFloatStateOf(initial.green)};var b by remember{mutableFloatStateOf(initial.blue)}
    val color=Color(r,g,b)
    AlertDialog(onDismissRequest=onDismiss,title={Text("Minha cor especial")},text={Column {
        Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)).background(color))
        Text("Vermelho");Slider(r,{r=it},modifier=Modifier.semantics{contentDescription="Vermelho"})
        Text("Verde");Slider(g,{g=it},modifier=Modifier.semantics{contentDescription="Verde"})
        Text("Azul");Slider(b,{b=it},modifier=Modifier.semantics{contentDescription="Azul"})
    }},confirmButton={TextButton(onClick={onSelect(color)}){Text("Usar minha cor")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}
