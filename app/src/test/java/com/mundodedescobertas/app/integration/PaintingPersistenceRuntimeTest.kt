package com.mundodedescobertas.app.integration

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.mundodedescobertas.app.painting.DrawingCatalog
import com.mundodedescobertas.app.painting.PaintingStore
import java.io.File
import java.io.IOException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Exercita PNG/Bitmap e arquivos reais, sem fingir que o MediaStore de um Samsung foi testado. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PaintingPersistenceRuntimeTest {
    private lateinit var context: Context

    @Before fun cleanPrivateAtelier() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, "atelier").deleteRecursively()
    }

    @Test fun changedPaintingSurvivesReopeningThePrivateDraftFile() {
        val first = PaintingStore(context)
        val id = DrawingCatalog.originals.first().id
        val bitmap = first.source(id)
        try {
            bitmap.setPixel(10, 10, Color.MAGENTA)
            first.writeDraft(id, bitmap)
            val second = PaintingStore(context)
            assertEquals(id, second.lastSelection())
            val restored = requireNotNull(second.readDraft(id))
            try {
                assertEquals(640, restored.width)
                assertEquals(640, restored.height)
                assertEquals(Color.MAGENTA, restored.getPixel(10, 10))
                assertTrue(bitmap.sameAs(restored))
            } finally { restored.recycle() }
        } finally { bitmap.recycle() }
    }

    @Test fun importedPngIsNormalizedAndAvailableToTheChildLibrary() {
        val source = Bitmap.createBitmap(160, 80, Bitmap.Config.ARGB_8888)
        source.eraseColor(Color.RED)
        val file = File(context.cacheDir, "adult-import.png")
        file.outputStream().use { assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        source.recycle()
        val store = PaintingStore(context)
        val imported = store.import(Uri.fromFile(file))
        assertEquals(listOf(imported), store.importedDrawings())
        val restored = store.source("import_${imported.nameWithoutExtension}")
        try {
            assertEquals(640, restored.width)
            assertEquals(640, restored.height)
            assertEquals(Color.RED, restored.getPixel(320, 320))
            assertEquals(Color.WHITE, restored.getPixel(0, 0))
        } finally { restored.recycle(); file.delete() }
    }

    @Test fun invalidImportCannotCreateABrokenDrawingOrLeaveTemporaryInput() {
        val source = File(context.cacheDir, "not-an-image.png").apply { writeText("not an image") }
        val store = PaintingStore(context)
        try {
            assertThrows(IOException::class.java) { store.import(Uri.fromFile(source)) }
            assertTrue(store.importedDrawings().isEmpty())
            assertFalse(store.root.listFiles().orEmpty().any { it.name.startsWith("incoming-") })
        } finally { source.delete() }
    }
}
