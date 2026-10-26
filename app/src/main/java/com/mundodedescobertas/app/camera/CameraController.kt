package com.mundodedescobertas.app.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

internal data class CameraFrame(val bitmap: Bitmap, val faceFound: Boolean)

/** One bounded analysis stream. No camera/microphone/network data is written or transmitted. */
internal class CameraController(
    private val context: Context,
    private val owner: LifecycleOwner,
    private val front: Boolean,
    private val calm: Boolean,
    private val onFrame: (CameraFrame) -> Unit,
    private val onError: (String) -> Unit,
) : AutoCloseable {
    @Volatile var filter: FaceFilter = FaceFilter.DOG
    @Volatile private var active = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    private val closed=AtomicBoolean(false)
    private val framePending=AtomicBoolean(false)
    private val executor=Executors.newSingleThreadExecutor()
    private val main=ContextCompat.getMainExecutor(context)
    private var provider:ProcessCameraProvider?=null
    private var analysis:ImageAnalysis?=null
    private var previous:FacePose?=null
    private var lastFrame=0L
    private val detector=FaceDetection.getClient(FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setMinFaceSize(.16f)
        .build())
    private val observer=LifecycleEventObserver { _,event->
        if(event==Lifecycle.Event.ON_START)active=true
        if(event==Lifecycle.Event.ON_STOP){active=false;previous=null}
    }
    init { owner.lifecycle.addObserver(observer) }

    @SuppressLint("MissingPermission")
    fun start() {
        val future=ProcessCameraProvider.getInstance(context)
        future.addListener({
            if(closed.get()) return@addListener
            try {
                val p=future.get()
                provider=p
                val selector=if(front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                if(!p.hasCamera(selector)) {onError("Esta câmera não está disponível. Tente a outra câmera.");return@addListener}
                val useCase=ImageAnalysis.Builder()
                    .setResolutionSelector(ResolutionSelector.Builder()
                        .setResolutionStrategy(ResolutionStrategy(Size(480,640),ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                        .build())
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis=useCase
                useCase.setAnalyzer(executor,::analyze)
                p.bindToLifecycle(owner,selector,useCase)
            } catch(_: SecurityException) {
                onError("A câmera não foi autorizada. Você pode continuar brincando nas outras áreas.")
            } catch(_: Exception) {
                onError("Não conseguimos abrir a câmera agora. Feche outro aplicativo de câmera e tente novamente.")
            }
        },main)
    }

    private fun analyze(image:ImageProxy) {
        val now=android.os.SystemClock.elapsedRealtime()
        if(closed.get() || !active || framePending.get() || now-lastFrame<if(calm)125 else 85) {image.close();return}
        lastFrame=now
        var upright:Bitmap?=null
        var mirrored:Bitmap?=null
        var pendingDetection:Task<List<Face>>?=null
        var imageClosed=false
        fun releaseImage() {if(!imageClosed){imageClosed=true;image.close()}}
        try {
            val raw=yuvBitmap(image)
            val rotation=image.imageInfo.rotationDegrees
            releaseImage()
            upright=if(rotation==0)raw else Bitmap.createBitmap(raw,0,0,raw.width,raw.height,Matrix().apply{postRotate(rotation.toFloat())},true).also{raw.recycle()}
            if(closed.get() || !active)return
            // InputImage receives only a local in-memory bitmap; bundled ML Kit never downloads a model.
            val detection=detector.process(InputImage.fromBitmap(upright,0))
            pendingDetection=detection
            val faces=Tasks.await(detection,3,TimeUnit.SECONDS)
            if(closed.get() || !active)return
            val face=faces.maxByOrNull {it.boundingBox.width()*it.boundingBox.height()}
            var pose=face?.let{facePose(it,upright.width,front)}
            pose=if(pose!=null)previous?.smoothed(pose) ?: pose else null
            previous=pose
            val source=if(front) {
                Bitmap.createBitmap(upright,0,0,upright.width,upright.height,Matrix().apply{setScale(-1f,1f)},true).also{mirrored=it}
            } else upright
            val rendered=FaceRenderer.render(source,pose,filter)
            if(framePending.compareAndSet(false,true)) main.execute {
                try {
                    if(closed.get() || !active) rendered.recycle()
                    else onFrame(CameraFrame(rendered,pose!=null))
                } finally {framePending.set(false)}
            } else rendered.recycle()
        } catch(_:Exception) {
            if(!closed.get()) main.execute {if(!closed.get())onError("O rastreamento foi interrompido. Toque em tentar novamente para reiniciar a câmera.")}
        } finally {
            releaseImage()
            mirrored?.recycle()
            val bitmap=upright
            val task=pendingDetection
            // A timed-out detector may still own the input. Release it only after its task finishes.
            if(bitmap!=null && task!=null && !task.isComplete) task.addOnCompleteListener(Executor {it.run()}){bitmap.recycle()}
            else bitmap?.recycle()
        }
    }

    override fun close() {
        if(!closed.compareAndSet(false,true))return
        active=false
        owner.lifecycle.removeObserver(observer)
        analysis?.clearAnalyzer()
        analysis?.let{provider?.unbind(it)}
        detector.close()
        executor.shutdown()
        previous=null
    }
}

private fun facePose(face:Face,width:Int,mirror:Boolean):FacePose {
    fun point(type:Int):FacePoint? = face.getLandmark(type)?.position?.let{FacePoint(if(mirror)width-it.x else it.x,it.y)}
    val box=face.boundingBox
    val eyes=listOfNotNull(point(FaceLandmark.LEFT_EYE),point(FaceLandmark.RIGHT_EYE)).sortedBy{it.x}
    val roll=if(eyes.size==2) atan2(eyes[1].y-eyes[0].y,eyes[1].x-eyes[0].x)*180f/PI.toFloat() else if(mirror)-face.headEulerAngleZ else face.headEulerAngleZ
    val mouthL=point(FaceLandmark.MOUTH_LEFT);val mouthR=point(FaceLandmark.MOUTH_RIGHT)
    val mouth=if(mouthL!=null && mouthR!=null) FacePoint((mouthL.x+mouthR.x)/2,(mouthL.y+mouthR.y)/2) else point(FaceLandmark.MOUTH_BOTTOM)
    return FacePose(FacePoint(if(mirror)width-box.exactCenterX() else box.exactCenterX(),box.exactCenterY()),
        box.width().toFloat(),box.height().toFloat(),roll,eyes.getOrNull(0),eyes.getOrNull(1),point(FaceLandmark.NOSE_BASE),mouth)
}

/** CameraX's stride-aware YUV_420_888 conversion, with no JPEG encode/decode on every frame. */
private fun yuvBitmap(image:ImageProxy):Bitmap {
    val sample=max(1,ceil(max(image.width,image.height)/640f).toInt())
    val width=image.width/sample;val height=image.height/sample
    val y=image.planes[0];val u=image.planes[1];val v=image.planes[2]
    val yBytes=y.buffer;val uBytes=u.buffer;val vBytes=v.buffer
    val pixels=IntArray(width*height)
    for(row in 0 until height) {
        val sourceRow=row*sample
        val yRow=sourceRow*y.rowStride;val uRow=(sourceRow/2)*u.rowStride;val vRow=(sourceRow/2)*v.rowStride
        for(col in 0 until width) {
            val sourceCol=col*sample
            val yy=(yBytes.get(yRow+sourceCol*y.pixelStride).toInt() and 255)-16
            val uu=(uBytes.get(uRow+(sourceCol/2)*u.pixelStride).toInt() and 255)-128
            val vv=(vBytes.get(vRow+(sourceCol/2)*v.pixelStride).toInt() and 255)-128
            val base=298*max(0,yy)
            val r=((base+409*vv+128) shr 8).coerceIn(0,255)
            val g=((base-100*uu-208*vv+128) shr 8).coerceIn(0,255)
            val b=((base+516*uu+128) shr 8).coerceIn(0,255)
            pixels[row*width+col]=(255 shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
    return Bitmap.createBitmap(pixels,width,height,Bitmap.Config.ARGB_8888)
}
