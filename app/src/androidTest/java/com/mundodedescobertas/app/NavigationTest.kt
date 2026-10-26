package com.mundodedescobertas.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Executar em emulador/dispositivo; não substitui a validação sensorial no Galaxy A57. */
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun fiveAreasOpenAndBackReturnsHome() {
        compose.waitUntil(10000){compose.onAllNodesWithText("Responsáveis").fetchSemanticsNodes().isNotEmpty()}
        listOf("Meu Ateliê","Meu DJ","Descobrir","Meus Joguinhos","Caretas Divertidas").forEach { title ->
            compose.onNodeWithTag("home_areas").performScrollToNode(hasText(title))
            compose.onNodeWithText(title).performClick()
            compose.onNodeWithText(title).assertExists()
            compose.onNodeWithText("Voltar").performClick()
            compose.onNodeWithTag("home_areas").performScrollToIndex(0)
            compose.onNodeWithText("Mundinho do Vivi").assertExists()
        }
    }
    @Test fun parentalAreaRequiresCorrectChallenge() {
        compose.waitUntil(10000){compose.onAllNodesWithText("Responsáveis").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("home_areas").performScrollToIndex(0)
        compose.onNodeWithText("Responsáveis").performClick()
        compose.onNodeWithTag("adult_answer").performTextInput("0")
        compose.onNodeWithText("Entrar nas configurações").performClick()
        compose.onNodeWithText("Vamos tentar outra vez? Peça ajuda a um adulto.").assertExists()
        val text=compose.onNodeWithTag("adult_challenge").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
        val digits=Regex("[0-9]+").findAll(text).map{it.value.toInt()}.toList()
        compose.onNodeWithTag("adult_answer").performTextReplacement((digits[0]*digits[1]).toString())
        compose.onNodeWithText("Entrar nas configurações").performClick()
        compose.onNodeWithText("Cada criança tem seu ritmo").assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Um momento com o responsável").assertExists()
    }
}
