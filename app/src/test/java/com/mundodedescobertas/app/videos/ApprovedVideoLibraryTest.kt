package com.mundodedescobertas.app.videos

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ApprovedVideoLibraryTest {
    private lateinit var context: Context

    @Before fun resetApprovedVideos() {
        context = ApplicationProvider.getApplicationContext()
        assertTrue(context.getSharedPreferences(ApprovedVideoLibrary.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit())
    }

    @Test fun approvedVideosAndRemovalsSurviveOpeningANewLibraryInstance() {
        val video = ApprovedVideo("AbC_dE-f123", "Música do Simba")
        assertTrue(ApprovedVideoLibrary(context).add(video))
        assertEquals(listOf(video), ApprovedVideoLibrary(context).read())
        ApprovedVideoLibrary(context).remove(video.id)
        assertTrue(ApprovedVideoLibrary(context).read().isEmpty())
    }

    @Test fun approvingTheSameVideoUpdatesItsTitleWithoutDuplicatingIt() {
        val library = ApprovedVideoLibrary(context)
        assertTrue(library.add(ApprovedVideo("AbC_dE-f123", "Primeiro título")))
        assertTrue(library.add(ApprovedVideo("AbC_dE-f123", "Título escolhido pelos responsáveis")))
        assertEquals(listOf(ApprovedVideo("AbC_dE-f123", "Título escolhido pelos responsáveis")), library.read())
    }

    @Test fun invalidIdsAndBlankTitlesCannotEnterTheChildLibrary() {
        val library = ApprovedVideoLibrary(context)
        assertFalse(library.add(ApprovedVideo("bad-id", "Vídeo inválido")))
        assertFalse(library.add(ApprovedVideo("AbC_dE-f123", " \n\t ")))
        assertTrue(library.read().isEmpty())
    }

    @Test fun corruptSavedDataDoesNotCrashAndANewApprovalCanRecoverTheLibrary() {
        val preferences = context.getSharedPreferences(ApprovedVideoLibrary.PREFS_NAME, Context.MODE_PRIVATE)
        assertTrue(preferences.edit().putString(ApprovedVideoLibrary.VIDEOS_KEY, "{not valid JSON").commit())
        val library = ApprovedVideoLibrary(context)
        assertTrue(library.read().isEmpty())
        val video = ApprovedVideo("AbC_dE-f123", "Uma nova escolha")
        assertTrue(library.add(video))
        assertEquals(listOf(video), ApprovedVideoLibrary(context).read())
    }

    @Test fun approvalLimitKeepsTheExistingLibraryIntact() {
        val library = ApprovedVideoLibrary(context)
        repeat(ApprovedVideoLibrary.MAX_VIDEOS) { index ->
            assertTrue(library.add(ApprovedVideo(index.toString().padStart(11, '0'), "Vídeo $index")))
        }
        assertFalse(library.add(ApprovedVideo("AbC_dE-f123", "Um vídeo a mais")))
        assertEquals(ApprovedVideoLibrary.MAX_VIDEOS, ApprovedVideoLibrary(context).read().size)
    }
}
