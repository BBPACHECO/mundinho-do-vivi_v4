package com.mundodedescobertas.app.painting

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Root navigation must expose this destination only after the parental gate. */
@Composable
fun AdultArtLibraryScreen() {
    val context=LocalContext.current
    val store=remember{PaintingStore(context.applicationContext)}
    val scope=rememberCoroutineScope()
    var revision by remember{mutableIntStateOf(0)}
    var busy by remember{mutableStateOf(false)}
    var message by remember{mutableStateOf<String?>(null)}
    var deleting by remember{mutableStateOf<File?>(null)}
    var tab by remember{mutableIntStateOf(0)}
    val files=remember(revision,tab){if(tab==0)store.importedDrawings()else store.savedCreations()}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
        if(uri!=null)scope.launch {
            busy=true
            runCatching{withContext(Dispatchers.IO){store.import(uri)}}
                .onSuccess{revision++;message="Desenho adicionado. Ele aparece em Desenhos → Importados no Ateliê."}
                .onFailure{message=it.message ?: "Não conseguimos abrir esse arquivo. Use PNG, JPG ou um PDF sem senha."}
            busy=false
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Biblioteca da família",style=MaterialTheme.typography.headlineMedium)
        Text("Adicione desenhos próprios ou licenciados. Arquivos de até 20 MB; PDFs importam apenas a primeira página, sem senha. A cópia fica no aparelho.",style=MaterialTheme.typography.bodyMedium)
        Button(onClick={picker.launch(arrayOf("image/png","image/jpeg","application/pdf"))},enabled=!busy,modifier=Modifier.heightIn(min=48.dp)){Text("Adicionar desenho PNG, JPG ou PDF")}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(selected=tab==0,onClick={tab=0},label={Text("Desenhos importados")})
            FilterChip(selected=tab==1,onClick={tab=1},label={Text("Pinturas criadas")})
        }
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
        if(files.isEmpty())Text(if(tab==0)"Nenhum desenho importado ainda. Os 22 desenhos originais já estão no Ateliê."else "As pinturas aparecem aqui quando a criança toca em Salvar no Ateliê.",style=MaterialTheme.typography.bodyLarge)
        LazyVerticalGrid(columns=GridCells.Adaptive(145.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),modifier=Modifier.weight(1f)) {
            items(files,key={it.path}){file->
                Card{Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    var bitmap by remember(file.path){mutableStateOf<Bitmap?>(null)}
                    LaunchedEffect(file.path){bitmap=withContext(Dispatchers.IO){runCatching{PaintingStore.decodeBounded(file)}.getOrNull()}}
                    bitmap?.let{Image(it.asImageBitmap(),if(tab==0)"Desenho importado"else "Pintura salva",Modifier.fillMaxWidth().aspectRatio(1f))}
                    if(tab==1)TextButton(onClick={scope.launch {
                        busy=true
                        runCatching{withContext(Dispatchers.IO){val bmp=PaintingStore.decodeBounded(file);try{store.export(bmp)}finally{bmp.recycle()}}}
                            .onSuccess{message="Pintura exportada para Pictures/MundinhoDoVivi."}
                            .onFailure{message="Não foi possível exportar a pintura. Ela continua guardada no aplicativo."}
                        busy=false
                    }},enabled=!busy){Text("Salvar na galeria")}
                    TextButton(onClick={deleting=file},enabled=!busy){Text("Excluir do aplicativo")}
                }}
            }
        }
    }
    deleting?.let{file->AlertDialog(onDismissRequest={deleting=null},title={Text("Excluir este arquivo?")},text={Text("A cópia dentro do aplicativo será excluída. O arquivo original e as imagens já exportadas na galeria serão preservados.")},confirmButton={TextButton(onClick={
        deleting=null;scope.launch {withContext(Dispatchers.IO){if(tab==0)store.deleteImported(file)else file.delete()};revision++}
    }){Text("Excluir cópia")}},dismissButton={TextButton(onClick={deleting=null}){Text("Manter")}})}
    message?.let{text->AlertDialog(onDismissRequest={message=null},title={Text("Biblioteca da família")},text={Text(text)},confirmButton={TextButton(onClick={message=null}){Text("Tudo bem")}})}
}
