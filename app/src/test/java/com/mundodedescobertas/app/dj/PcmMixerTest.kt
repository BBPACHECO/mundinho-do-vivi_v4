package com.mundodedescobertas.app.dj

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.math.abs

class PcmMixerTest {
    @get:Rule val temporary=TemporaryFolder()
    private fun bank()=List(32){FloatArray(400){.3f}}

    @Test fun `sample clock emits exactly sixteen steps in one 100 BPM bar`(){
        val mixer=PcmMixer(bank(),sampleRate=48000)
        mixer.configure(PcmMixer.State(playA=true,bpm=100f,gain=.5f))
        val bar=ShortArray(115200) // 4 * 60 / 100 seconds at 48 kHz
        mixer.render(bar)
        assertEquals(16L,mixer.emittedSteps)
        assertEquals(115200L,mixer.renderedFrames)
        assertEquals(15,mixer.currentStep)
        assertTrue(bar.any{it.toInt()!=0})
    }
    @Test fun `polyphony is bounded and many simultaneous pads never overflow PCM`(){
        val mixer=PcmMixer(bank(),maxVoices=8)
        repeat(100){mixer.trigger(it%32,10f)}
        assertEquals(8,mixer.activeVoices)
        val output=ShortArray(512);mixer.render(output)
        assertTrue(output.all{abs(it.toInt())<=32760})
        assertEquals(0,mixer.activeVoices)
    }
    @Test fun `crossfade endpoint suppresses the opposite deck and master zero mutes`(){
        val mixer=PcmMixer(bank())
        mixer.configure(PcmMixer.State(playB=true,crossfade=0f,gain=1f))
        val output=ShortArray(8000);mixer.render(output)
        assertTrue(output.all{it.toInt()==0})
        mixer.configure(PcmMixer.State(playA=true,gain=0f));mixer.render(output)
        assertTrue(output.all{it.toInt()==0})
    }
    @Test fun `sequencer triggers only selected active steps and pause clears voices`(){
        val mixer=PcmMixer(bank())
        mixer.configure(PcmMixer.State(sequencer=true,selectedPad=2,steps=setOf(0),bpm=120f,gain=1f))
        val first=ShortArray(6000);mixer.render(first)
        assertTrue(first.any{it.toInt()!=0})
        val next=ShortArray(6000);mixer.render(next)
        assertTrue(next.all{it.toInt()==0})
        mixer.silence();assertEquals(0,mixer.activeVoices);assertFalse(mixer.hasSound)
    }
    @Test fun `wav recording finalizes valid RIFF and round trips signed PCM`(){
        val f=temporary.newFile("music.wav")
        WavRecorder(f).use{it.append(shortArrayOf(0,1234,-1234,32760,-32760))}
        assertEquals(54L,f.length())
        val decoded=WavRecorder.decode(f.readBytes())
        assertEquals(5,decoded.size)
        assertEquals(1234f/32768,decoded[1],.00001f)
        assertEquals(-32760f/32768,decoded[4],.00001f)
    }
    @Test fun `recording is bounded and corrupt WAV is rejected`(){
        val f=temporary.newFile("bounded.wav")
        WavRecorder(f,sampleRate=100,maxSeconds=1).use{it.append(ShortArray(200){10});assertTrue(it.full);it.append(ShortArray(20))}
        assertEquals(244L,f.length())
        assertEquals(100,WavRecorder.decode(f.readBytes(),100).size)
        try{WavRecorder.decode(byteArrayOf(1,2,3));fail("Corruption must not play")}catch(_:IllegalArgumentException){}
    }
    @Test fun `catalog contains thirty two individually named embedded samples and sixteen rhythms`(){
        assertEquals(32,DjCatalog.pads.size)
        assertEquals(32,DjCatalog.pads.map{it.source}.toSet().size)
        assertEquals(16,DjCatalog.rhythms.size)
    }
}
