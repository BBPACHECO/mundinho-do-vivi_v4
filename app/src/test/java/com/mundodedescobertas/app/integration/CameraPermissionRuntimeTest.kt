package com.mundodedescobertas.app.integration

import android.Manifest
import android.app.Application
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.mundodedescobertas.app.camera.CameraScreen
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.ui.theme.ViviTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Valida a tela sem autorização; não tenta criar uma câmera virtual nem detectar rostos falsos. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CameraPermissionRuntimeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cameraStaysOffUntilExplicitPermissionAction() {
        shadowOf(ApplicationProvider.getApplicationContext<Application>()).denyPermissions(Manifest.permission.CAMERA)
        compose.setContent { ViviTheme { Surface { CameraScreen(ChildPreferences(reducedStimuli = true)) } } }
        compose.onNodeWithText("A câmera está desligada. Ligue apenas quando quiser brincar.").assertExists()
        compose.onNodeWithText("Ligar câmera").assertIsEnabled()
        compose.onNodeWithText("Fotografar").assertDoesNotExist()
        compose.onNodeWithText("Trocar câmera").assertDoesNotExist()
    }
}
