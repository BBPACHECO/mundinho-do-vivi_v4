package com.mundodedescobertas.app.painting

import android.app.Application
import android.graphics.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal enum class PaintTool(val title:String) { FILL("Preencher"),BRUSH("Pincel"),ERASER("Borracha"),STAMP("Carimbo") }
internal data class PaintingState(val bitmap:Bitmap?=null,val selectedId:String="",val revision:Int=0,val canUndo:Boolean=false,val canRedo:Boolean=false,val loading:Boolean=true,val message:String?=null)

class PaintingViewModel(application:Application):AndroidViewModel(application) {
    private val store=PaintingStore(application)
    private val _state=MutableStateFlow(PaintingState())
    internal val state=_state.asStateFlow()
    private var base:Bitmap?=null
    private var boundary=IntArray(0)
    private var history=PixelHistory()
    private val saveMutex=Mutex()
    private var strokeActive=false
    private var selectionGeneration=0
    init {
        val last=runCatching{store.lastSelection()}.getOrDefault("")
        val valid=DrawingCatalog.originals.any{it.id==last} || (last.startsWith("import_") && store.importedDrawings().any{last=="import_${it.nameWithoutExtension}"})
        select(if(valid)last else DrawingCatalog.originals.first().id)
    }
    internal fun select(id:String) {
        if(_state.value.loading && _state.value.selectedId==id)return
        val generation=++selectionGeneration
        _state.value=_state.value.copy(loading=true,selectedId=id,message=null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val original=store.source(id)
                    val draft=store.readDraft(id) ?: requireNotNull(original.copy(Bitmap.Config.ARGB_8888,true)) { "Não foi possível preparar o desenho" }
                    original to draft
                }
            }.onSuccess{(original,draft)->
                if(generation!=selectionGeneration){original.recycle();draft.recycle();return@onSuccess}
                base=original;boundary=readPixels(original);history=PixelHistory();strokeActive=false
                _state.value=PaintingState(bitmap=draft,selectedId=id,revision=_state.value.revision+1,loading=false)
                saveDraft()
            }.onFailure{if(generation==selectionGeneration)_state.value=_state.value.copy(loading=false,message="Não conseguimos abrir este desenho. Escolha outro na biblioteca.")}
        }
    }
    internal fun editPoint(tool:PaintTool,x:Float,y:Float,color:Int,width:Float,stamp:Int,previous:PointF?) {
        val bitmap=_state.value.bitmap ?: return
        if(_state.value.loading)return
        if(!strokeActive){history.beforeEdit(readPixels(bitmap));strokeActive=true}
        val px=x.coerceIn(0f,639f);val py=y.coerceIn(0f,639f)
        when(tool) {
            PaintTool.FILL -> {
                val pixels=readPixels(bitmap);PaintPixels.fill(pixels,boundary,640,640,px.toInt(),py.toInt(),color)
                bitmap.setPixels(pixels,0,640,0,0,640,640)
            }
            PaintTool.BRUSH -> {
                val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{this.color=color;strokeWidth=width;strokeCap=Paint.Cap.ROUND;style=Paint.Style.STROKE}
                val c=Canvas(bitmap);if(previous==null)c.drawPoint(px,py,paint)else c.drawLine(previous.x,previous.y,px,py,paint)
            }
            PaintTool.ERASER -> {
                // Restore the original source within the eraser; outlines are restored as well.
                val pixels=readPixels(bitmap);val radius=width/2f
                val from=previous ?: PointF(px,py);val distance=kotlin.math.hypot(px-from.x,py-from.y);val steps=maxOf(1,(distance/2).toInt())
                for(s in 0..steps){val cx=from.x+(px-from.x)*s/steps;val cy=from.y+(py-from.y)*s/steps
                    for(yy in maxOf(0,(cy-radius).toInt())..minOf(639,(cy+radius).toInt()))for(xx in maxOf(0,(cx-radius).toInt())..minOf(639,(cx+radius).toInt())) {
                        if((xx-cx)*(xx-cx)+(yy-cy)*(yy-cy)<=radius*radius)pixels[yy*640+xx]=boundary[yy*640+xx]
                    }
                }
                bitmap.setPixels(pixels,0,640,0,0,640,640)
            }
            PaintTool.STAMP -> {
                val c=Canvas(bitmap);val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{this.color=color;style=Paint.Style.FILL}
                val r=maxOf(15f,width*1.5f)
                when(stamp) {
                    0->{val path=Path();for(i in 0..9){val a=i*Math.PI/5-Math.PI/2;val rr=if(i%2==0)r else r*.45f;val xx=px+kotlin.math.cos(a).toFloat()*rr;val yy=py+kotlin.math.sin(a).toFloat()*rr;if(i==0)path.moveTo(xx,yy)else path.lineTo(xx,yy)};path.close();c.drawPath(path,p)}
                    1->{val path=Path();path.moveTo(px,py+r);path.cubicTo(px-r*2,py,px-r,py-r*1.7f,px,py-r*.6f);path.cubicTo(px+r,py-r*1.7f,px+r*2,py,px,py+r);c.drawPath(path,p)}
                    else->{for(i in 0..5){val a=i*Math.PI/3;c.drawCircle(px+kotlin.math.cos(a).toFloat()*r*.6f,py+kotlin.math.sin(a).toFloat()*r*.6f,r*.45f,p)};p.color=Color.rgb(255,213,102);c.drawCircle(px,py,r*.4f,p)}
                }
            }
        }
        publish()
    }
    internal fun finishStroke(){if(strokeActive){strokeActive=false;saveDraft()}}
    internal fun undo(){val b=_state.value.bitmap?:return;history.undo(readPixels(b))?.let{b.setPixels(it,0,640,0,0,640,640);publish();saveDraft()}}
    internal fun redo(){val b=_state.value.bitmap?:return;history.redo(readPixels(b))?.let{b.setPixels(it,0,640,0,0,640,640);publish();saveDraft()}}
    internal fun clear(){val b=_state.value.bitmap?:return;history.beforeEdit(readPixels(b));b.setPixels(boundary,0,640,0,0,640,640);publish();saveDraft()}
    private fun publish(){_state.value=_state.value.copy(revision=_state.value.revision+1,canUndo=history.canUndo,canRedo=history.canRedo)}
    private fun saveDraft(){
        val state=_state.value;val snapshot=state.bitmap?.copy(Bitmap.Config.ARGB_8888,false)?:return
        PaintingIo.scope.launch{try{saveMutex.withLock{store.writeDraft(state.selectedId,snapshot)}}catch(_:Exception){withContext(Dispatchers.Main){message("Não foi possível guardar o rascunho. Tente salvar na galeria.")}}finally{snapshot.recycle()}}
    }
    internal fun saveGallery(){
        val snapshot=_state.value.bitmap?.copy(Bitmap.Config.ARGB_8888,false)?:return
        viewModelScope.launch {
            runCatching{withContext(Dispatchers.IO){store.retainCreation(snapshot);store.export(snapshot)}}
                .onSuccess{message("Pintura salva na galeria, em MundinhoDoVivi.")}
                .onFailure{message("Não foi possível salvar na galeria. Sua criação foi mantida no aplicativo quando possível.")}
            snapshot.recycle()
        }
    }
    internal fun message(message:String?){_state.value=_state.value.copy(message=message)}
    private fun readPixels(bitmap:Bitmap)=IntArray(640*640).also{bitmap.getPixels(it,0,640,0,0,640,640)}
}

private object PaintingIo { val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO.limitedParallelism(1)) }
