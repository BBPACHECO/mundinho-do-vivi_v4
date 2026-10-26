package com.mundodedescobertas.app.integration

import android.app.Application
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.learning.DiscoverScreen
import com.mundodedescobertas.app.learning.LearningCatalog
import com.mundodedescobertas.app.ui.theme.ViviTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Verifica pedidos de narração, sem alegar testar a pronúncia do sistema TTS físico. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DiscoverRuntimeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun alphabetHasTwentySixChoicesAndWordTapRequestsCorrectNarration() {
        val spoken = mutableListOf<String>()
        compose.setContent { ViviTheme { Surface {
            DiscoverScreen(ChildPreferences(reducedStimuli = true), onSpeak = { spoken.add(it) })
        } } }
        ('A'..'Z').forEach { letter -> compose.onNodeWithTag("letter_$letter").assertExists() }
        compose.onNodeWithTag("letter_A").performScrollTo().performClick()
        val aWords = LearningCatalog.forLetter('A')
        assertEquals(10, aWords.size)
        aWords.forEach { word -> compose.onNodeWithTag("word_${word.word}").assertExists() }
        compose.onNodeWithTag("word_Abelha").performScrollTo().performClick()
        compose.onNodeWithText("A abelha visita as flores.").assertExists()
        val expected = aWords.first { it.word == "Abelha" }.narration
        compose.runOnIdle { assertEquals(expected, spoken.last()) }
        val beforeRepeat = spoken.size
        compose.onNodeWithText("Ouvir novamente").performClick()
        compose.runOnIdle {
            assertEquals(beforeRepeat + 1, spoken.size)
            assertEquals(expected, spoken.last())
        }
        compose.onNodeWithText("Voltar às palavras").performClick()
        compose.onNodeWithTag("word_Abelha").assertExists()
    }

    @Test fun calmPaginationPreservesFiveObjectsAndDoesNotCountATouchTwice() {
        val spoken = mutableListOf<String>()
        compose.setContent { ViviTheme { Surface {
            DiscoverScreen(ChildPreferences(reducedStimuli = true, maxElements = 3), onSpeak = { spoken.add(it) })
        } } }
        compose.onNodeWithText("Números").performClick()
        compose.onNodeWithTag("number_5").performScrollTo().performClick()
        compose.onNodeWithText("Grupo 1 de 2 · a quantidade total continua sendo 5").assertExists()
        compose.onNodeWithTag("count_object_3").assertDoesNotExist()
        compose.onNodeWithTag("count_object_0").performScrollTo().performClick().performClick()
        compose.onNodeWithTag("count_progress").assertTextEquals("1 de 5 contadas")
        compose.onNodeWithTag("count_object_1").performScrollTo().performClick()
        compose.onNodeWithTag("count_object_2").performScrollTo().performClick()
        compose.onNodeWithText("Próximo grupo").performScrollTo().performClick()
        compose.onNodeWithTag("count_object_0").assertDoesNotExist()
        compose.onNodeWithTag("count_object_3").performScrollTo().performClick()
        compose.onNodeWithTag("count_object_4").performScrollTo().performClick()
        compose.onNodeWithTag("count_progress").assertTextEquals("5 de 5 contadas")
        compose.onNodeWithText("Você contou todas as estrelas!").assertExists()
        compose.runOnIdle { assertEquals("cinco", spoken.last()) }
    }

    @Test fun zeroMeansNoObjectsAndAllThirtyOneNumberChoicesExist() {
        compose.setContent { ViviTheme { Surface {
            DiscoverScreen(ChildPreferences(reducedStimuli = true), onSpeak = {})
        } } }
        compose.onNodeWithText("Números").performClick()
        (0..30).forEach { n -> compose.onNodeWithTag("number_$n").assertExists() }
        compose.onNodeWithTag("number_0").performScrollTo().performClick()
        compose.onNodeWithText("Nenhuma estrela. Isto é zero!").assertExists()
        compose.onNodeWithTag("count_object_0").assertDoesNotExist()
        compose.onNodeWithTag("count_progress").assertDoesNotExist()
    }
}
