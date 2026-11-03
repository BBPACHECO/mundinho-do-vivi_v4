package com.mundodedescobertas.app.videos

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class YouTubeNavigationPolicyTest {
    @Test fun adultSearchKeepsNavigationOnOfficialHttpsYouTubeHosts() {
        listOf(
            "https://m.youtube.com/results?search_query=simba",
            "https://www.youtube.com/watch?v=AbC_dE-f123",
            "https://youtube.com/shorts/AbC_dE-f123",
            "https://youtu.be/AbC_dE-f123",
            "https://www.youtube-nocookie.com/embed/AbC_dE-f123",
        ).forEach { assertTrue(it, isAllowedYouTubeNavigation(it)) }
        listOf(
            "http://m.youtube.com/results?search_query=simba",
            "https://youtube.com.attacker.example/results",
            "https://attacker.youtube.com/results",
            "https://youtube.com@attacker.example/results",
            "https://attacker.example@youtube.com/results",
            "https://www.youtube.com:8443/results",
            "https://accounts.google.com/",
            "intent://youtube.com/watch?v=AbC_dE-f123",
            "javascript:alert(1)",
            "file:///android_asset/video.html",
        ).forEach { assertFalse(it, isAllowedYouTubeNavigation(it)) }
    }

    @Test fun childPlayerOnlyAllowsItsWrapperAndTheSelectedVideoEmbed() {
        val id = "AbC_dE-f123"
        val origin = "https://com.mundodedescobertas.app.debug"
        listOf(
            origin,
            "$origin/",
            "https://www.youtube.com/embed/$id?enablejsapi=1",
            "https://www.youtube-nocookie.com/embed/$id?playsinline=1",
        ).forEach { assertTrue(it, isAllowedPlayerNavigation(it, id, origin)) }
        listOf(
            "https://www.youtube.com/embed/XyZ_12-3456",
            "https://www.youtube.com/watch?v=$id",
            "https://m.youtube.com/results?search_query=anything",
            "https://youtu.be/$id",
            "http://com.mundodedescobertas.app.debug/",
            "$origin/another-page",
            "$origin/?navigate=anything",
            "$origin/#another-page",
            "https://com.mundodedescobertas.app.debug.attacker.example/",
            "https://attacker.example@com.mundodedescobertas.app.debug/",
            "https://com.mundodedescobertas.app.debug:8443/",
            "https://youtube.com.attacker.example/embed/$id",
        ).forEach { assertFalse(it, isAllowedPlayerNavigation(it, id, origin)) }
        assertFalse(isAllowedPlayerNavigation(origin, "bad-id", origin))
    }

    @Test fun aCraftedWrapperOriginCannotAuthorizeATopLevelPage() {
        val id = "AbC_dE-f123"
        val wrapper = "https://com.mundodedescobertas.app.debug/"
        listOf(
            "http://com.mundodedescobertas.app.debug",
            "file:///com.mundodedescobertas.app.debug",
            "https://attacker.example@com.mundodedescobertas.app.debug",
            "https://com.mundodedescobertas.app.debug:8443",
            "https://com.mundodedescobertas.app.debug/unexpected-path",
            "https://com.mundodedescobertas.app.debug?unexpected=query",
            "https://com.mundodedescobertas.app.debug#unexpected-fragment",
        ).forEach { assertFalse(it, isAllowedPlayerNavigation(wrapper, id, it)) }
    }
}
