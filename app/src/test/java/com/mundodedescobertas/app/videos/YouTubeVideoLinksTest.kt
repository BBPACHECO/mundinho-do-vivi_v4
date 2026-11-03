package com.mundodedescobertas.app.videos

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class YouTubeVideoLinksTest {
    @Test fun officialWatchShortAndEmbedLinksResolveToTheSameVideo() {
        val id = "AbC_dE-f123"
        listOf(
            "https://www.youtube.com/watch?v=$id&list=example&t=5s",
            "https://youtube.com/watch?v=$id",
            "https://m.youtube.com/watch?v=$id",
            "https://youtu.be/$id?si=example",
            "https://www.youtube.com/shorts/$id",
            "https://www.youtube.com/embed/$id",
        ).forEach { url -> assertEquals(url, id, YouTubeVideoLinks.videoId(url)) }
    }

    @Test fun linksCannotSmuggleAnotherHostCredentialsOrProtocol() {
        val id = "AbC_dE-f123"
        listOf(
            "https://youtube.com.example.org/watch?v=$id",
            "https://example.org/youtube.com/watch?v=$id",
            "https://youtube.com@example.org/watch?v=$id",
            "https://example.org@youtube.com/watch?v=$id",
            "https://evil.youtube.com/watch?v=$id",
            "https://www.youtube.com:8443/watch?v=$id",
            "http://www.youtube.com/watch?v=$id",
            "javascript:alert('$id')",
            "file:///watch?v=$id",
        ).forEach { url -> assertNull(url, YouTubeVideoLinks.videoId(url)) }
    }

    @Test fun missingMalformedOrOverlongVideoIdsAreRejected() {
        listOf(
            "", "not a URL", "AbC_dE-f123", "https://www.youtube.com/results?search_query=simba",
            "https://www.youtube.com/watch", "https://www.youtube.com/watch?v=short",
            "https://youtu.be/AbC_dE-f1234", "https://youtu.be/AbC.dE-f123",
            "https://youtu.be/AbC_dE-f123/another-path",
        ).forEach { url -> assertNull(url, YouTubeVideoLinks.videoId(url)) }
    }

    @Test fun librarySearchMatchesPortugueseAccentsAndLeavesOtherTitlesOut() {
        val music = ApprovedVideo("AbC_dE-f123", "Música do Simba")
        val numbers = ApprovedVideo("XyZ_12-3456", "Números para descobrir")
        val videos = listOf(music, numbers)
        assertEquals(listOf(music), filterApprovedVideos(videos, "MUSICA"))
        assertEquals(listOf(numbers), filterApprovedVideos(videos, "numeros"))
        assertEquals(videos, filterApprovedVideos(videos, "  "))
        assertTrue(filterApprovedVideos(videos, "um título inexistente").isEmpty())
    }
}
