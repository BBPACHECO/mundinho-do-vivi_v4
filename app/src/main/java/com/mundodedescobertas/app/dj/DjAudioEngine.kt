package com.mundodedescobertas.app.dj

import android.content.ContentValues
import android.content.Context
import android.media.*
import android.os.Environment
import android.os.Process
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.sqrt

class DjAudioEngine(private val context:Context,bank:List<FloatArray>) : AutoCloseable {
    data class Status(val step:Int=-1,val level:Float=0f,val recording:Boolean=false,val seconds:Int=0,
        val message:String?=null,val recordName:String?=null,val available:Boolean=false,val playA:Boolean=false,val playB:Boolean=false,val sequence:Boolean=false)
    private val mutableStatus=MutableStateFlow(Status())
    val status=mutableStatus.asStateFlow()
    private val queue=ConcurrentLinkedQueue<()->Unit>()
    private val closed=AtomicBoolean(false)
    private val foreground=AtomicBoolean(true)
    private val mixer=PcmMixer(bank)
    private var recording:WavRecorder?=null
    private var recordingFile:File?=null
    private var playback:FloatArray?=null
    private var playbackPosition=0
    private var message:String?=null
    private val audioManager=context.getSystemService(AudioManager::class.java)
    private val focusRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        .setOnAudioFocusChangeListener { if(it==AudioManager.AUDIOFOCUS_LOSS||it==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)pauseAll() }.build()
    @Volatile private var ready=false
    private val worker=thread(start=true,name="Vivi PCM mixer") { runAudio() }

    private fun enqueue(action:()->Unit){if(!closed.get())queue.add(action)}
    fun configure(state:PcmMixer.State){enqueue{mixer.configure(state)}}
    fun pad(index:Int){enqueue{mixer.trigger(index,.75f)}}
    fun setForeground(value:Boolean){if(closed.get())return;foreground.set(value);if(!value)pauseAll()}
    fun pauseAll(){enqueue{mixer.silence();playback=null;finishRecording()}}
    fun startRecording(){enqueue{
        if(recording==null){
            val dir=recordingsDirectory(context);dir.mkdirs()
            check(dir.usableSpace>20L*1024*1024){"Pouco espaço para gravar. Peça ajuda a um responsável."}
            check((dir.listFiles()?.sumOf{it.length()}?:0L)<100L*1024*1024){"A biblioteca está cheia. Um responsável pode apagar gravações antigas."}
            recordingFile=File(dir,"Vivi-${System.currentTimeMillis()}.wav")
            recording=WavRecorder(recordingFile!!);message=null
        }
    }}
    fun stopRecording(){enqueue{finishRecording()}}
    private fun finishRecording(){
        val recorder=recording?:return
        try {recorder.close();message=if(recorder.sampleCount>0)"Sua música ficou guardada no aparelho." else "A gravação ficou vazia."}
        finally {recording=null}
    }
    fun playRecording(file:File){enqueue{
        require(file.length() in 45..18_000_000){"Gravação inválida ou grande demais."}
        mixer.silence();playback=WavRecorder.decode(file.readBytes());playbackPosition=0;message="Ouvindo sua criação"
    }}
    private fun runAudio(){
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
        var track:AudioTrack?=null
        try {
            val minBuffer=AudioTrack.getMinBufferSize(48000,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT)
            check(minBuffer>0){"Saída de áudio não disponível neste aparelho."}
            track=AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(48000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(maxOf(minBuffer,2048))
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY).build()
            check(track.state==AudioTrack.STATE_INITIALIZED){"Não foi possível iniciar o áudio."}
            ready=true
            publish(0f)
            val buffer=ShortArray(256)
            var running=false;var meterFrames=0
            while(!closed.get()){
                while(true){val task=queue.poll()?:break
                    try{task()}catch(e:Exception){message=e.message?:"Não foi possível realizar esta ação.";try{finishRecording()}catch(_:Exception){}}
                }
                val sound=foreground.get()&&(mixer.hasSound||recording!=null||playback!=null)
                if(!sound){
                    if(running){track.pause();track.flush();audioManager.abandonAudioFocusRequest(focusRequest);running=false}
                    publish(0f);Thread.sleep(12);continue
                }
                if(!running){
                    if(audioManager.requestAudioFocus(focusRequest)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED){mixer.silence();playback=null;message="Outro aplicativo está usando o áudio. Tente novamente.";finishRecording();publish(0f);Thread.sleep(20);continue}
                    track.play();running=true
                }
                mixer.render(buffer)
                playback?.let {pcm->
                    for(i in buffer.indices){
                        if(playbackPosition<pcm.size)buffer[i]=(pcm[playbackPosition++]*mixer.state.gain*32760f).toInt().coerceIn(-32760,32760).toShort()
                        else{playback=null;break}
                    }
                }
                recording?.let{recorder->recorder.append(buffer);if(recorder.full)finishRecording()}
                var written=0
                while(written<buffer.size&&!closed.get()){
                    val count=track.write(buffer,written,buffer.size-written,AudioTrack.WRITE_BLOCKING)
                    check(count>0){"A saída de áudio foi interrompida. Volte e abra o DJ novamente."};written+=count
                }
                meterFrames+=buffer.size
                if(meterFrames>=2400){meterFrames=0;val rms=sqrt(buffer.sumOf{it.toDouble()*it}/buffer.size)/32768;publish(rms.toFloat())}
            }
        }catch(e:Exception){message=e.message?:"O áudio não está disponível.";publish(0f)}
        finally{closed.set(true);ready=false;queue.clear();try{finishRecording()}catch(_:Exception){};try{track?.stop()}catch(_:Exception){};track?.release();audioManager.abandonAudioFocusRequest(focusRequest);publish(0f)}
    }
    private fun publish(level:Float){mutableStatus.value=Status(mixer.currentStep,level,recording!=null,(recording?.sampleCount?:0)/48000,message,recordingFile?.name,ready&&!closed.get(),mixer.state.playA,mixer.state.playB,mixer.state.sequencer)}
    override fun close(){closed.set(true);foreground.set(false)}

    companion object {
        fun recordingsDirectory(context:Context)=File(context.filesDir,"dj_recordings")
        fun loadBank(context:Context):List<FloatArray> = DjCatalog.pads.map{pad->
            context.assets.open("dj/${pad.source}.wav").use{WavRecorder.decode(it.readBytes())}
        }
        fun export(context:Context,file:File):android.net.Uri {
            require(file.parentFile?.canonicalFile==recordingsDirectory(context).canonicalFile&&file.isFile)
            require(file.length() in 45..18_000_000 && WavRecorder.decode(file.readBytes()).isNotEmpty()){ "Aguarde a gravação terminar antes de salvar." }
            val values=ContentValues().apply{put(MediaStore.Audio.Media.DISPLAY_NAME,file.name);put(MediaStore.Audio.Media.MIME_TYPE,"audio/wav");put(MediaStore.Audio.Media.RELATIVE_PATH,Environment.DIRECTORY_MUSIC+"/MundinhoDoVivi");put(MediaStore.Audio.Media.IS_PENDING,1)}
            val uri=context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values)?:error("Não foi possível salvar a música.")
            try{
                context.contentResolver.openOutputStream(uri)?.use{output->file.inputStream().use{it.copyTo(output)}}?:error("Não foi possível abrir o destino.")
                context.contentResolver.update(uri,ContentValues().apply{put(MediaStore.Audio.Media.IS_PENDING,0)},null,null)
                return uri
            }catch(e:Exception){context.contentResolver.delete(uri,null,null);throw e}
        }
    }
}
