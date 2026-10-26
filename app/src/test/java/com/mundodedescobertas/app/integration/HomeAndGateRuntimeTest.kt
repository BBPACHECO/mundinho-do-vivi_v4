package com.mundodedescobertas.app.integration

import android.app.Application
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.ui.screens.ActivityArea
import com.mundodedescobertas.app.ui.screens.HomeScreen
import com.mundodedescobertas.app.ui.screens.ParentGate
import com.mundodedescobertas.app.ui.theme.ViviTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Executa Compose Android de verdade em JVM; não simula câmera, microfone ou alto-falante. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeAndGateRuntimeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allFiveIllustratedAreasRespondToTouchWithCorrectDestination() {
        val opened = mutableListOf<ActivityArea>()
        compose.setContent {
            ViviTheme { Surface {
                HomeScreen(ChildPreferences(childName = "Bia", reducedStimuli = true),
                    onOpen = { opened.add(it) }, onParents = {}, onMascot = {})
            } }
        }
        compose.onNodeWithText("Oi, Bia! Vamos brincar?").assertExists()
        val requested = listOf(
            "Meu Ateliê" to ActivityArea.ATELIER,
            "Meu DJ" to ActivityArea.DJ,
            "Descobrir" to ActivityArea.DISCOVER,
            "Meus Joguinhos" to ActivityArea.GAMES,
            "Caretas Divertidas" to ActivityArea.CAMERA,
        )
        requested.forEach { (title, destination) ->
            compose.onNodeWithTag("home_areas").performScrollToNode(hasText(title))
            compose.onNodeWithText(title).performClick()
            compose.runOnIdle { assertEquals(destination, opened.last()) }
        }
        compose.runOnIdle { assertEquals(5, opened.distinct().size) }
    }

    @Test fun parentControlsAndMascotAreSeparateFromChildDestinations() {
        var parentOpenCount = 0
        var greetingCount = 0
        var areaOpenCount = 0
        compose.setContent {
            ViviTheme { Surface {
                HomeScreen(ChildPreferences(reducedStimuli = true),
                    onOpen = { areaOpenCount++ }, onParents = { parentOpenCount++ },
                    onMascot = { greetingCount++ })
            } }
        }
        compose.onNodeWithText("Responsáveis").performClick()
        compose.onNodeWithContentDescription("Nuvino, nosso mascote. Toque para dizer oi.").performClick()
        compose.runOnIdle {
            assertEquals(1, parentOpenCount)
            assertEquals(1, greetingCount)
            assertEquals(0, areaOpenCount)
        }
    }

    @Test fun wrongAdultAnswerCannotUnlockAndCorrectAnswerCan() {
        var unlocked = false
        compose.setContent { ViviTheme { Surface { ParentGate { unlocked = true } } } }
        compose.onNodeWithText("Entrar nas configurações").assertIsNotEnabled()
        compose.onNodeWithTag("adult_answer").performTextInput("0")
        compose.onNodeWithText("Entrar nas configurações").performClick()
        compose.onNodeWithText("Vamos tentar outra vez? Peça ajuda a um adulto.").assertExists()
        compose.runOnIdle { assertFalse(unlocked) }
        val challenge = compose.onNodeWithTag("adult_challenge").fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString(" ") { it.text }
        val operands = Regex("[0-9]+").findAll(challenge).map { it.value.toInt() }.toList()
        assertEquals(2, operands.size)
        compose.onNodeWithTag("adult_answer").performTextReplacement((operands[0] * operands[1]).toString())
        compose.onNodeWithText("Entrar nas configurações").performClick()
        compose.runOnIdle { assertTrue(unlocked) }
    }
}
