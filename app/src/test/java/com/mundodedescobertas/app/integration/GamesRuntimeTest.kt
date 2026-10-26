package com.mundodedescobertas.app.integration

import android.app.Application
import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.games.GamesScreen
import com.mundodedescobertas.app.ui.theme.ViviTheme
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GamesRuntimeTest {
    @get:Rule val compose = createComposeRule()

    @Before fun resetPrivateWorld() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.filesDir, "vivi-block-world.txt").delete()
        File(context.filesDir, "vivi-block-world.txt.bak").delete()
    }

    @Test fun allEightGamesHaveAPlayableCanvasStartAndReturnToLibrary() {
        compose.setContent { ViviTheme { Surface {
            GamesScreen(ChildPreferences(reducedStimuli = true, masterVolume = 0f), onSpeak = {})
        } } }
        val titles = listOf("Passarinho Aventureiro", "Corrida das Cores", "Mundinho de Blocos", "Bolhas Mágicas",
            "Jogo da Memória", "Quebra-cabeça", "Formas e Cores", "Aventura dos Números")
        titles.forEach { title ->
            compose.onNodeWithTag("games_library").performScrollToNode(hasText(title))
            compose.onNodeWithText(title).performClick()
            compose.onNodeWithTag("game_canvas").assertExists()
            compose.onNodeWithText("Começar").assertIsEnabled()
            compose.onNodeWithText("Ver jogos").performClick()
            compose.onNodeWithTag("games_library").assertExists()
        }
    }

    @Test fun buildingRespondsToTouchPauseStopsInputAndWorldRestoresOnReopen() {
        compose.setContent { ViviTheme { Surface {
            GamesScreen(ChildPreferences(reducedStimuli = true, masterVolume = 0f), onSpeak = {})
        } } }
        compose.onNodeWithTag("games_library").performScrollToNode(hasText("Mundinho de Blocos"))
        compose.onNodeWithText("Mundinho de Blocos").performClick()
        compose.onNodeWithTag("game_status").assertTextContains("4 construções", substring = true)
        // A running game intentionally requests every frame. Drive that clock explicitly in tests.
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Começar").performClick()
        compose.mainClock.advanceTimeBy(64)
        val boardSize = compose.onNodeWithTag("game_canvas").fetchSemanticsNode().boundsInRoot.size
        compose.onNodeWithTag("game_canvas").performTouchInput {
            click(Offset(boardSize.width * .85f, boardSize.height * .8f))
        }
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag("game_status").assertTextContains("5 construções", substring = true)
        compose.onNodeWithText("Pausar").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithText("Uma pausa tranquila").assertExists()
        compose.onNodeWithTag("game_canvas").performTouchInput {
            click(Offset(boardSize.width * .18f, boardSize.height * .8f))
        }
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag("game_status").assertTextContains("5 construções", substring = true)
        compose.onNodeWithText("Ver jogos").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.mainClock.autoAdvance = true
        compose.onNodeWithTag("games_library").performScrollToNode(hasText("Mundinho de Blocos"))
        compose.onNodeWithText("Mundinho de Blocos").performClick()
        compose.onNodeWithTag("game_status").assertTextContains("5 construções", substring = true)
    }

    @Test fun bubblesPopFromRealTouchAndCanPauseResumeWithoutLeavingTheGame() {
        compose.setContent { ViviTheme { Surface {
            GamesScreen(ChildPreferences(reducedStimuli = true, masterVolume = 0f, maxElements = 3), onSpeak = {})
        } } }
        compose.onNodeWithTag("games_library").performScrollToNode(hasText("Bolhas Mágicas"))
        compose.onNodeWithText("Bolhas Mágicas").performClick()
        compose.onNodeWithTag("game_status").assertTextContains("Bolhas descobertas: 0", substring = true)
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Começar").performClick()
        compose.mainClock.advanceTimeBy(64)
        // Bubble placement is intentionally random. A dense touch grid covers every possible initial
        // bubble position without mutating the production engine or replacing its randomness.
        val boardSize = compose.onNodeWithTag("game_canvas").fetchSemanticsNode().boundsInRoot.size
        compose.onNodeWithTag("game_canvas").performTouchInput {
            for (y in 10..90 step 6) for (x in 10..90 step 6) {
                click(Offset(boardSize.width * x / 100f, boardSize.height * y / 100f))
            }
        }
        compose.mainClock.advanceTimeBy(32)
        val status = compose.onNodeWithTag("game_status").fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString(" ") { it.text }
        val popped = Regex("[0-9]+").find(status)?.value?.toInt() ?: 0
        assertTrue("O toque deve estourar pelo menos uma bolha: $status", popped > 0)
        compose.onNodeWithText("Pausar").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithText("Uma pausa tranquila").assertExists()
        compose.onNodeWithText("Continuar brincando").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithText("Uma pausa tranquila").assertDoesNotExist()
        compose.onNodeWithText("Ver jogos").performClick()
        compose.mainClock.advanceTimeBy(32)
        compose.mainClock.autoAdvance = true
        compose.onNodeWithTag("games_library").assertExists()
    }
}
