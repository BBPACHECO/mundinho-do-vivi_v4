package com.mundodedescobertas.app.integration

import android.app.Application
import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.ui.theme.ViviTheme
import com.mundodedescobertas.app.videos.ApprovedVideo
import com.mundodedescobertas.app.videos.ApprovedVideoLibrary
import com.mundodedescobertas.app.videos.VideosScreen
import com.mundodedescobertas.app.videos.ManageVideosScreen
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Valida a biblioteca local; não cria WebView nem simula reprodução do YouTube. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VideosRuntimeTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var context: Context

    @Before fun clearApprovedVideos() {
        context = ApplicationProvider.getApplicationContext()
        assertTrue(context.getSharedPreferences(ApprovedVideoLibrary.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit())
    }

    @Test fun anEmptyLibraryOffersAdultHelpInsteadOfUnapprovedVideos() {
        var requests = 0
        compose.setContent { ViviTheme { Surface { VideosScreen(ChildPreferences(), onManageVideos = { requests++ }) } } }
        compose.onNodeWithTag("approved_videos_list").performScrollToNode(hasText("Nenhum vídeo aprovado ainda"))
        compose.onNodeWithText("Nenhum vídeo aprovado ainda").assertExists()
        compose.onNodeWithTag("approved_videos_list").performScrollToNode(hasText("Pedir ajuda aos responsáveis"))
        compose.onNodeWithText("Pedir ajuda aos responsáveis").performClick()
        compose.runOnIdle { assertEquals(1, requests) }
    }

    @Test fun nativeSearchShowsOnlyMatchingApprovedTitlesWithoutStartingAPlayer() {
        val library = ApprovedVideoLibrary(context)
        assertTrue(library.add(ApprovedVideo("AbC_dE-f123", "Música do Simba")))
        assertTrue(library.add(ApprovedVideo("XyZ_12-3456", "Números para descobrir")))
        compose.setContent { ViviTheme { Surface { VideosScreen(ChildPreferences(), onManageVideos = {}) } } }
        compose.onNodeWithTag("approved_videos_list").performScrollToNode(hasText("Buscar nos vídeos aprovados"))
        compose.onNodeWithText("Buscar nos vídeos aprovados").performTextInput("musica")
        compose.onNodeWithTag("approved_videos_list").performScrollToNode(hasText("Música do Simba"))
        compose.onNodeWithText("Música do Simba").assertExists()
        compose.onNodeWithText("Números para descobrir").assertDoesNotExist()
        compose.onNodeWithTag("approved_videos_list").performScrollToNode(hasText("Buscar nos vídeos aprovados"))
        compose.onNodeWithText("Buscar nos vídeos aprovados").performTextReplacement("nenhum resultado")
        compose.onNodeWithTag("approved_videos_list").performScrollToNode(hasText("Nenhum vídeo encontrado nessa lista."))
        compose.onNodeWithText("Música do Simba").assertDoesNotExist()
        compose.onNodeWithText("Números para descobrir").assertDoesNotExist()
    }

    @Test fun anAdultCanApproveAPastedLinkAndRemoveItWithoutOnlineSearch() {
        compose.setContent { ViviTheme { Surface { ManageVideosScreen() } } }
        compose.onNodeWithTag("manage_videos_list").performScrollToNode(hasText("Buscar no YouTube"))
        compose.onNodeWithText("Buscar no YouTube").assertIsNotEnabled()
        compose.onNodeWithTag("manage_videos_list").performScrollToNode(hasText("Título para a criança"))
        compose.onNodeWithText("Título para a criança").performTextInput("Música do Simba")
        compose.onNodeWithTag("manage_videos_list").performScrollToNode(hasText("Link do vídeo do YouTube"))
        compose.onNodeWithText("Link do vídeo do YouTube").performTextInput("https://youtu.be/AbC_dE-f123")
        compose.onNodeWithTag("manage_videos_list").performScrollToNode(hasText("Adicionar link à lista"))
        compose.onNodeWithText("Adicionar link à lista").performClick()
        compose.runOnIdle { assertEquals(listOf(ApprovedVideo("AbC_dE-f123", "Música do Simba")), ApprovedVideoLibrary(context).read()) }
        compose.onNodeWithTag("manage_videos_list").performScrollToNode(hasContentDescription("Remover vídeo Música do Simba"))
        compose.onNodeWithContentDescription("Remover vídeo Música do Simba").performClick()
        compose.runOnIdle { assertTrue(ApprovedVideoLibrary(context).read().isEmpty()) }
        compose.onNodeWithTag("manage_videos_list").performScrollToNode(hasText("Vídeos aprovados · 0/${ApprovedVideoLibrary.MAX_VIDEOS}"))
        compose.onNodeWithText("Vídeos aprovados · 0/${ApprovedVideoLibrary.MAX_VIDEOS}").assertExists()
    }
}
