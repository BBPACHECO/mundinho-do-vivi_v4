package com.mundodedescobertas.app.dj

import kotlin.math.*

/** A single sample clock synchronizes both decks and all 16 sequencer steps. No wall-clock scheduling. */
class PcmMixer(val samples:List<FloatArray>,val sampleRate:Int=48000,val maxVoices:Int=32) {
    data class State(val playA:Boolean=false,val playB:Boolean=false,val rhythmA:Int=0,val rhythmB:Int=4,
        val bpm:Float=100f,val crossfade:Float=.5f,val gain:Float=.5f,val sequencer:Boolean=false,
        val selectedPad:Int=0,val steps:Set<Int> = emptySet())
    private data class Voice(val pcm:FloatArray,var position:Int,val gain:Float)
    private val voices=ArrayList<Voice>(maxVoices)
    var state=State(); private set
    var currentStep=-1; private set
    var renderedFrames=0L;private set
    var emittedSteps=0L;private set
    private var untilStep=0.0
    val activeVoices:Int get()=voices.size
    val hasSound:Boolean get()=voices.isNotEmpty()||state.playA||state.playB||state.sequencer
    private val notes=Array(8){n->tone(65.406*2.0.pow(n/12.0),.21,sampleRate)}

    fun configure(next:State){
        val oldRunning=state.playA||state.playB||state.sequencer
        state=next.copy(bpm=next.bpm.takeIf{it.isFinite()}?.coerceIn(60f,170f)?:100f,
            crossfade=next.crossfade.coerceIn(0f,1f),gain=next.gain.coerceIn(0f,1f),
            rhythmA=next.rhythmA.coerceIn(0,15),rhythmB=next.rhythmB.coerceIn(0,15),
            steps=next.steps.filter{it in 0..15}.toSet(),selectedPad=next.selectedPad.coerceIn(0,maxOf(0,samples.lastIndex)))
        if(!oldRunning && (state.playA||state.playB||state.sequencer)){untilStep=0.0;currentStep=-1}
    }
    fun trigger(index:Int,gain:Float=1f){samples.getOrNull(index)?.let{add(it,gain)}}
    private fun add(pcm:FloatArray,gain:Float){
        if(pcm.isEmpty()||gain<=0f)return
        if(voices.size>=maxVoices)voices.removeAt(0)
        voices.add(Voice(pcm,0,gain))
    }
    fun silence(){voices.clear();configure(state.copy(playA=false,playB=false,sequencer=false));currentStep=-1;untilStep=0.0}
    fun render(destination:ShortArray){
        val s=state
        val running=s.playA||s.playB||s.sequencer
        val interval=framesPerStep(sampleRate,s.bpm)
        for(i in destination.indices){
            if(running){
                if(untilStep<=0.0){
                    currentStep=(currentStep+1)%16;emittedSteps++
                    if(s.playA)rhythm(s.rhythmA,currentStep,cos(s.crossfade*PI/2).toFloat()*.55f)
                    if(s.playB)rhythm(s.rhythmB,currentStep,sin(s.crossfade*PI/2).toFloat()*.55f)
                    if(s.sequencer&&currentStep in s.steps)trigger(s.selectedPad,.8f)
                    untilStep+=interval
                }
                untilStep--
            }
            var mixed=0f;var v=0
            while(v<voices.size){val voice=voices[v];mixed+=voice.pcm[voice.position++]*voice.gain
                if(voice.position==voice.pcm.size)voices.removeAt(v)else v++}
            // Smooth saturation bounds all overlapping voices; no integer overflow or wraparound.
            val output=tanh(mixed.toDouble()).toFloat()*s.gain
            destination[i]=(output*32760f).roundToInt().coerceIn(-32760,32760).toShort()
        }
        renderedFrames+=destination.size
    }
    private fun rhythm(style:Int,step:Int,g:Float){
        if(g<.001f)return
        val kick=when(style){0->setOf(0,6,10);1->setOf(0,7,10);2->setOf(0,3,8,11);3->setOf(0,4,8,12);4->setOf(0,4,8,12);5->setOf(0,8,10);6->setOf(0,4,8,12);7->setOf(0,8);8->setOf(0,6,8);9->setOf(0,3,9,14);10->setOf(0,7,10);11->setOf(0,5,8,13);12->setOf(0,4,7,8,12,15);13->setOf(0,8);14->setOf(0,3,6,8,11,14);else->emptySet()}
        val snare=when(style){2->setOf(2,6,10,14);7->setOf(4,12);9->setOf(8);11->setOf(4,10,14);14->setOf(4,12);15->emptySet();else->setOf(4,12)}
        if(step in kick)trigger(0,g*.9f)
        if(step in snare)trigger(if(style==11)4 else 1,g*.65f)
        val hats=if(style in listOf(3,4,6,9,12))1 else if(style==13)4 else 2
        if(style!=15&&step%hats==0)trigger(if(style==7&&step%4==2)6 else 2,g*(if(step%4==0).26f else .18f))
        if(style==9&&step in setOf(13,15))trigger(2,g*.12f)
        if(style in listOf(6,14)&&step%4==2)trigger(3,g*.2f)
        if(style==11&&step%3==0)trigger(5,g*.4f)
        if(style==14&&step%2==1)trigger(6,g*.22f)
        if(style!=15&&step%4==0){val note=intArrayOf(0,3,5,7)[(step/4+style)%4];add(notes[note],g*(if(style==10).22f else .33f))}
    }
    companion object {
        fun framesPerStep(rate:Int,bpm:Float):Double=rate*60.0/(bpm.coerceIn(60f,170f)*4.0)
        fun tone(frequency:Double,seconds:Double,rate:Int):FloatArray=FloatArray((rate*seconds).toInt()){i->
            val t=i.toDouble()/rate; val attack=min(1.0,t/.006);val end=min(1.0,(seconds-t)/.012)
            ((sin(2*PI*frequency*t)+.16*sin(2*PI*frequency*2*t))*exp(-t*9)*attack*end*.55).toFloat()
        }
    }
}
