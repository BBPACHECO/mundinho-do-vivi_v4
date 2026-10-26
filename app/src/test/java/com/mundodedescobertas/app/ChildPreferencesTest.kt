package com.mundodedescobertas.app

import com.mundodedescobertas.app.data.*
import org.junit.Assert.*
import org.junit.Test

class ChildPreferencesTest {
    @Test fun `calm mode reduces stimulation without destroying individual choices`() {
        val p=ChildPreferences(masterVolume=.8f,narrationVolume=.6f,effectsVolume=.7f,animationIntensity=.9f,gameSpeed=.9f,maxElements=10)
        val calm=p.copy(calmMode=true)
        assertEquals(.2f,calm.effectiveAnimation,.001f)
        assertEquals(.288f,calm.effectiveNarrationVolume,.001f)
        assertEquals(.28f,calm.effectiveEffectsVolume,.001f)
        assertEquals(.5f,calm.effectiveSpeed,.001f)
        assertEquals(4,calm.effectiveElements)
        assertEquals(p,calm.copy(calmMode=false))
    }
    @Test fun `reduced stimuli stops mascot movement even outside calm mode`() {
        assertEquals(0f,ChildPreferences(reducedStimuli=true).effectiveAnimation,0f)
    }
    @Test fun `invalid persisted values are bounded and names remain readable`() {
        val p=ChildPreferences(childName="\n  \t",masterVolume=Float.NaN,narrationVolume=3f,effectsVolume=-3f,gameSpeed=Float.POSITIVE_INFINITY,sessionMinutes=-1,maxElements=500).normalized()
        assertEquals("Vivi",p.childName)
        assertTrue(p.masterVolume.isFinite())
        assertEquals(1f,p.narrationVolume,0f)
        assertEquals(0f,p.effectsVolume,0f)
        assertEquals(0,p.sessionMinutes)
        assertEquals(12,p.maxElements)
        assertEquals(.75f,p.gameSpeed,0f)
    }
    @Test fun `parental challenge rejects empty malformed and incorrect answers`() {
        val challenge=AdultChallenge(7,8)
        listOf("", "abc", "55", "56.0", "9999999999999999").forEach{assertFalse(challenge.accepts(it))}
        assertTrue(challenge.accepts(" 56 "))
    }
}
