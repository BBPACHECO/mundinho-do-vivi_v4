package com.mundodedescobertas.app.dj

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** PCM16 mono WAV, written incrementally with a bounded duration and patched final RIFF lengths. */
class WavRecorder(private val file:File,val sampleRate:Int=48000,val maxSeconds:Int=180):AutoCloseable {
    private val stream=RandomAccessFile(file,"rw")
    var sampleCount=0;private set
    private var closed=false
    init{stream.setLength(0);stream.write(header(0,sampleRate))}
    fun append(pcm:ShortArray){
        check(!closed)
        val count=minOf(pcm.size,maxSeconds*sampleRate-sampleCount)
        if(count<=0)return
        val bytes=ByteArray(count*2)
        for(i in 0 until count){bytes[2*i]=(pcm[i].toInt() and 255).toByte();bytes[2*i+1]=(pcm[i].toInt() shr 8).toByte()}
        stream.write(bytes);sampleCount+=count
    }
    val full:Boolean get()=sampleCount>=maxSeconds*sampleRate
    override fun close(){if(!closed){closed=true;stream.seek(0);stream.write(header(sampleCount*2,sampleRate));stream.close()}}
    companion object{
        fun header(dataBytes:Int,rate:Int):ByteArray=ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply{
            put("RIFF".toByteArray());putInt(36+dataBytes);put("WAVEfmt ".toByteArray());putInt(16);putShort(1);putShort(1)
            putInt(rate);putInt(rate*2);putShort(2);putShort(16);put("data".toByteArray());putInt(dataBytes)
        }.array()
        fun decode(bytes:ByteArray,expectedRate:Int=48000):FloatArray{
            require(bytes.size>=44&&String(bytes,0,4)=="RIFF"&&String(bytes,8,4)=="WAVE"){"WAV inválido"}
            val buffer=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            var at=12;var channels=0;var rate=0;var bits=0;var format=0
            while(at+8<=bytes.size){
                val name=String(bytes,at,4);val length=buffer.getInt(at+4)
                require(length>=0&&at.toLong()+8+length<=bytes.size){"WAV truncado"}
                if(name=="fmt "){require(length>=16);format=buffer.getShort(at+8).toInt();channels=buffer.getShort(at+10).toInt();rate=buffer.getInt(at+12);bits=buffer.getShort(at+22).toInt()}
                if(name=="data"){
                    require(format==1&&channels==1&&rate==expectedRate&&bits==16){"Formato de áudio não suportado"}
                    return FloatArray(length/2){buffer.getShort(at+8+2*it)/32768f}
                }
                at+=8+length+(length%2)
            }
            error("Áudio sem dados PCM")
        }
    }
}
