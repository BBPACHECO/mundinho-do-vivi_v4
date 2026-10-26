package com.mundodedescobertas.app.integration

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import java.io.File
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.mundodedescobertas.app.MainActivity
import com.mundodedescobertas.app.ViviApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Usa a Activity, o DataStore, o ViewModel e a navegação de produção, sem equipamento físico. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = ViviApplication::class, qualifiers = "w393dp-h852dp-port")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActivityNavigationRuntimeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun fiveNativeRoutesOpenAndTopLeftBackReturnsHome() {
        compose.waitUntil(30_000) {
            compose.onAllNodesWithTag("home_areas").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("home_areas").performScrollToIndex(0)
        capture("home")
        val names = mapOf("Meu Ateliê" to "atelier", "Meu DJ" to "dj", "Descobrir" to "discover", "Meus Joguinhos" to "games", "Caretas Divertidas" to "camera")
        listOf("Meu Ateliê", "Meu DJ", "Descobrir", "Meus Joguinhos", "Caretas Divertidas").forEach { title ->
            compose.onNodeWithTag("home_areas").performScrollToNode(hasText(title))
            compose.onNodeWithText(title).performClick()
            compose.onNodeWithTag("home_areas").assertDoesNotExist()
            if (title == "Meu Ateliê") compose.waitUntil(30_000) {
                compose.onAllNodesWithContentDescription("Desenho para pintar. Um dedo pinta; dois dedos ampliam e movem.").fetchSemanticsNodes().isNotEmpty()
            }
            if (title == "Meu DJ") compose.waitUntil(30_000) {
                compose.onAllNodesWithText("Preparando os sons offline…").fetchSemanticsNodes().isEmpty()
            }
            capture(names.getValue(title))
            compose.onAllNodesWithText("Voltar").onFirst().performClick()
            compose.onNodeWithTag("home_areas").assertExists()
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        // PixelCopy's forceRedraw handshake is unavailable in Robolectric. Draw the production
        // Activity's actual native view tree on its UI thread instead of replacing app content.
        val image = compose.runOnIdle {
            val view = compose.activity.window.decorView
            check(view.width > 0 && view.height > 0) { "A janela nativa ainda não foi medida" }
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                view.draw(Canvas(bitmap))
            }
        }
        val directory = File("build/reports/ui")
        check(directory.isDirectory || directory.mkdirs()) { "Não foi possível preparar relatório visual" }
        try {
            directory.resolve("$name.png").outputStream().use { output ->
                check(image.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Não foi possível gravar prévia nativa" }
            }
        } finally { image.recycle() }
    }
}
