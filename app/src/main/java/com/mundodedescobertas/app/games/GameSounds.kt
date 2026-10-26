package com.mundodedescobertas.app.games

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/** Original soft sine chimes, never imitations of recorded animals or objects. */
internal class GameSounds(context:Context) {
    private val pool=SoundPool.Builder().setMaxStreams(4).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val loaded=mutableSetOf<Int>()
    private val sounds=mutableListOf<Int>()
    private var last=0L
    init {
        pool.setOnLoadCompleteListener{_,id,status->if(status==0)synchronized(loaded){loaded.add(id)}}
        val sampleRate=22050
        repeat(5){note->
            val samples=IntArray(2205){i->val t=i.toDouble()/sampleRate;val envelope=sin(PI*i/2205).pow(2);(sin(2*PI*(440.0*2.0.pow(note/12.0))*t)*envelope*4200).toInt()}
            val dataSize=samples.size*2
            val data=ByteBuffer.allocate(44+dataSize).order(ByteOrder.LITTLE_ENDIAN)
            data.put("RIFF".toByteArray()).putInt(36+dataSize).put("WAVEfmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(sampleRate).putInt(sampleRate*2).putShort(2).putShort(16).put("data".toByteArray()).putInt(dataSize)
            samples.forEach{data.putShort(it.toShort())}
            val file=context.cacheDir.resolve("vivi-game-chime-$note.wav")
            try {file.writeBytes(data.array());sounds.add(pool.load(file.absolutePath,1))}catch(_:Exception){}
        }
    }
    fun play(volume:Float) {
        if(volume<=0f || sounds.isEmpty())return
        val now=android.os.SystemClock.elapsedRealtime()
        if(now-last<65)return
        last=now
        val id=sounds.random()
        if(synchronized(loaded){id in loaded})pool.play(id,volume.coerceIn(0f,1f),volume.coerceIn(0f,1f),1,0,1f)
    }
    fun stop(){pool.autoPause()}
    fun close(){pool.release()}
}
