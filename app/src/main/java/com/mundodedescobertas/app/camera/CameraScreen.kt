package com.mundodedescobertas.app.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mundodedescobertas.app.data.ChildPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraScreen(settings:ChildPreferences,onOpenEditor:(()->Unit)?=null) {
    val context=LocalContext.current
    val owner=LocalLifecycleOwner.current
    val scope=rememberCoroutineScope()
    var granted by remember{mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)}
    var requested by remember{mutableStateOf(false)}
    var enabled by remember{mutableStateOf(false)}
    var front by remember{mutableStateOf(true)}
    var selected by remember{mutableIntStateOf(0)}
    var restart by remember{mutableIntStateOf(0)}
    var frame by remember{mutableStateOf<CameraFrame?>(null)}
    var error by remember{mutableStateOf<String?>(null)}
    var message by remember{mutableStateOf<String?>(null)}
    var captured by remember{mutableStateOf<Bitmap?>(null)}
    var saving by remember{mutableStateOf(false)}
    var foreground by remember{mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ ok->
        granted=ok;requested=true;enabled=ok
        if(!ok)message="Tudo bem! A câmera só funciona com sua autorização. As outras brincadeiras continuam disponíveis."
    }
    DisposableEffect(owner) {
        val observer=LifecycleEventObserver{_,event->
            if(event==Lifecycle.Event.ON_START) {
                foreground=true
                granted=ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
            }
            if(event==Lifecycle.Event.ON_STOP){foreground=false;frame=null}
        }
        owner.lifecycle.addObserver(observer)
        onDispose{owner.lifecycle.removeObserver(observer)}
    }
    val latestFilter by rememberUpdatedState(FaceFilter.entries[selected])
    var controller by remember{mutableStateOf<CameraController?>(null)}
    DisposableEffect(granted,enabled,front,foreground,restart,settings.calmMode) {
        frame=null
        if(granted && enabled && foreground) {
            error=null
            val camera=CameraController(context,owner,front,settings.calmMode,
                onFrame={frame=it},onError={error=it;frame=null;enabled=false})
            controller=camera;camera.filter=latestFilter;camera.start()
            onDispose{camera.close();if(controller===camera)controller=null}
        } else onDispose{}
    }
    LaunchedEffect(selected){controller?.filter=FaceFilter.entries[selected]}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)) {
        if(onOpenEditor!=null) ElevatedCard(onClick=onOpenEditor,modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text("Meu Miniestúdio",style=MaterialTheme.typography.titleLarge)
                Text("Edite vídeos com cores, emojis, sons e sua janelinha da câmera.",style=MaterialTheme.typography.bodyMedium)
            }
        }
        Text("Vamos fazer uma careta?",style=MaterialTheme.typography.headlineMedium)
        Text("Tudo acontece neste aparelho. Nenhuma imagem é enviada. Deslize para trocar o filtro.",style=MaterialTheme.typography.bodyMedium)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val previewHeight=(maxWidth*(4f/3f)).coerceIn(260.dp,420.dp)
        Box(Modifier.fillMaxWidth().height(previewHeight)
            .clip(RoundedCornerShape(28.dp)).background(Color(0xff30283c))
            .pointerInput(Unit){var drag=0f;detectHorizontalDragGestures(onDragStart={drag=0f},onDragEnd={
                if(drag>60)selected=(selected+FaceFilter.entries.size-1)%FaceFilter.entries.size
                else if(drag < -60)selected=(selected+1)%FaceFilter.entries.size
            }){change,delta->change.consume();drag+=delta}},contentAlignment=Alignment.Center) {
            val current=frame
            if(current!=null) Image(current.bitmap.asImageBitmap(),contentDescription="Câmera com filtro ${FaceFilter.entries[selected].title}",
                contentScale=ContentScale.Fit,modifier=Modifier.fillMaxSize())
            else Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp),modifier=Modifier.padding(24.dp)) {
                if(granted && enabled && foreground && error==null)CircularProgressIndicator(color=Color(0xffd8c6f4))
                Text(when {
                    error!=null -> error.orEmpty()
                    !granted || !enabled -> "A câmera está desligada. Ligue apenas quando quiser brincar."
                    !foreground -> "Câmera pausada"
                    else -> "Preparando a câmera…"
                },color=Color.White,style=MaterialTheme.typography.bodyLarge)
            }
            if(current!=null && !current.faceFound) Surface(color=Color(0xeef7f0ff),shape=RoundedCornerShape(16.dp),modifier=Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                Text("Cadê o rostinho? Olhe para a câmera com boa iluminação.",Modifier.padding(12.dp),style=MaterialTheme.typography.bodyMedium)
            }
        }
        }
        if(!granted || !enabled) {
            Button(onClick={
                if(granted){enabled=true;error=null}else launcher.launch(Manifest.permission.CAMERA)
            },modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text(if(error!=null)"Tentar novamente" else if(requested && !granted)"Autorizar câmera" else "Ligar câmera")}
            if(requested && !granted)Text("Se o Android não mostrar o pedido novamente, um responsável pode autorizar Câmera em Configurações do Android → Aplicativos → Mundinho do Vivi → Permissões.",style=MaterialTheme.typography.bodyMedium)
        } else {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(onClick={frame?.takeIf{it.faceFound}?.let{captured=it.bitmap.copy(Bitmap.Config.ARGB_8888,false)}},
                    enabled=frame?.faceFound==true && error==null,modifier=Modifier.weight(1f).heightIn(min=52.dp)){Text("Fotografar")}
                OutlinedButton(onClick={front=!front;error=null},modifier=Modifier.weight(1f).heightIn(min=52.dp)){Text("Trocar câmera")}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick={enabled=false;frame=null;error=null},modifier=Modifier.heightIn(min=48.dp)){Text("Desligar")}
                if(error!=null)Button(onClick={restart++;error=null},modifier=Modifier.heightIn(min=48.dp)){Text("Tentar novamente")}
            }
        }
        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            itemsIndexed(FaceFilter.entries){index,filter->
                FilterChip(selected=selected==index,onClick={selected=index},label={Text(filter.title)},modifier=Modifier.heightIn(min=48.dp))
            }
        }
        Text(FaceFilter.entries[selected].title,style=MaterialTheme.typography.titleLarge)
        Text(when(FaceFilter.entries[selected]) {
            FaceFilter.NOSE -> "Vamos brincar de nariz grandão?"
            FaceFilter.EYES -> "Olha só esses olhos grandões!"
            FaceFilter.CHEEKS -> "Bochechas fofinhas para uma careta divertida."
            FaceFilter.HEAD -> "Uma cabeça grandona para inventar caretas."
            FaceFilter.LONG -> "Vamos esticar essa careta?"
            else -> "Este acessório acompanha seu rosto."
        },style=MaterialTheme.typography.bodyMedium)
        if(message!=null)Text(message.orEmpty(),style=MaterialTheme.typography.bodyLarge)
        Text("Use boa iluminação e olhe para a câmera. Você pode desligar quando quiser.",style=MaterialTheme.typography.bodyMedium)
    }
    val photo=captured
    if(photo!=null)AlertDialog(onDismissRequest={if(!saving)captured=null},title={Text("Sua careta ficou pronta!")},
        text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            Image(photo.asImageBitmap(),contentDescription="Fotografia com o filtro aplicado",modifier=Modifier.fillMaxWidth().heightIn(max=340.dp),contentScale=ContentScale.Fit)
            Text("Salvar esta fotografia na galeria? Ela só será gravada se você escolher salvar.")
            if(saving)CircularProgressIndicator()
        }},confirmButton={TextButton(enabled=!saving,onClick={
            saving=true
            scope.launch {
                val saved=withContext(Dispatchers.IO){saveCameraPhoto(context,photo)}
                message=if(saved)"Fotografia salva em Imagens/MundinhoDoVivi." else "Não foi possível salvar a fotografia. Tente novamente."
                saving=false;if(saved)captured=null
            }
        }){Text("Salvar na galeria")}},dismissButton={TextButton(enabled=!saving,onClick={captured=null}){Text("Descartar")}})
}

private fun saveCameraPhoto(context:android.content.Context,bitmap:Bitmap):Boolean {
    val resolver=context.contentResolver
    var uri:android.net.Uri?=null
    return try {
        val values=ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"Vivi_Careta_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS",Locale.ROOT).format(Date())}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH,"${Environment.DIRECTORY_PICTURES}/MundinhoDoVivi")
            put(MediaStore.Images.Media.IS_PENDING,1)
        }
        uri=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values) ?: return false
        resolver.openOutputStream(uri)?.use{check(bitmap.compress(Bitmap.CompressFormat.JPEG,94,it))} ?: error("Sem saída")
        resolver.update(uri,ContentValues().apply{put(MediaStore.Images.Media.IS_PENDING,0)},null,null)
        true
    } catch(_:Exception){uri?.let{runCatching{resolver.delete(it,null,null)}};false}
}
