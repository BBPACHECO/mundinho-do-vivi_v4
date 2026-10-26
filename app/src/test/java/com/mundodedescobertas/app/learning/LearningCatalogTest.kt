package com.mundodedescobertas.app.learning

import org.junit.Assert.*
import org.junit.Test

class LearningCatalogTest {
    @Test fun `every letter has ten different matching examples`() {
        assertEquals(260,LearningCatalog.words.size)
        assertEquals(260,LearningCatalog.words.map{it.word}.toSet().size)
        for(letter in 'A'..'Z') {
            val words=LearningCatalog.forLetter(letter)
            assertEquals("Quantidade da letra $letter",10,words.size)
            words.forEach {
                assertEquals("Inicial de ${it.word}",letter,LearningCatalog.initialOf(it.word))
                assertTrue(it.sentence.endsWith('.'))
                assertTrue(it.art.isNotBlank())
                assertTrue(it.narration.contains(it.word))
            }
        }
    }
    @Test fun `portuguese diacritics are retained in words but normalized for letter association`() {
        assertEquals('A',LearningCatalog.initialOf("Árvore"))
        assertEquals('I',LearningCatalog.initialOf("Ímã"))
        assertEquals('O',LearningCatalog.initialOf("Ônibus"))
        assertTrue(LearningCatalog.forLetter('A').any{it.word=="Árvore"})
    }
    @Test fun `number names cover zero through thirty without changing the quantity`() {
        assertEquals(31,LearningCatalog.numberNames.size)
        assertEquals("zero",LearningCatalog.numberNames[0])
        assertEquals("vinte e três",LearningCatalog.numberNames[23])
        assertEquals("trinta",LearningCatalog.numberNames[30])
        assertEquals(31,LearningCatalog.numberNames.toSet().size)
    }
    @Test fun `counting ignores out of range touches and supports undo without double counting`() {
        var round=CountingRound(3)
        assertFalse(round.complete)
        assertEquals(round,round.toggle(-1))
        assertEquals(round,round.toggle(3))
        round=round.toggle(0).toggle(1).toggle(2)
        assertTrue(round.complete)
        assertEquals(setOf(0,1,2),round.selected)
        assertFalse(round.toggle(1).complete)
        assertTrue(CountingRound(0).complete)
    }
}
