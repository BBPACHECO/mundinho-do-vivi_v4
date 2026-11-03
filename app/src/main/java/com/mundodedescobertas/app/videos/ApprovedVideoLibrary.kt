package com.mundodedescobertas.app.videos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.text.Normalizer
import java.util.Locale

data class ApprovedVideo(val id: String, val title: String)

/** Recognizes video identifiers, never arbitrary web destinations or stream URLs. */
object YouTubeVideoLinks {
    private val idPattern = Regex("[A-Za-z0-9_-]{11}")
    private val hosts = setOf("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be")

    fun isValidId(id: String): Boolean = idPattern.matches(id)

    fun videoId(raw: String): String? = runCatching {
        if (raw.length > 2048) return null
        val uri = URI(raw.trim())
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true) || host !in hosts || uri.rawUserInfo != null || uri.port !in listOf(-1, 443)) return null
        val parts = uri.path.orEmpty().split('/').filter(String::isNotEmpty)
        val id = when {
            host == "youtu.be" && parts.size == 1 -> parts.single()
            host != "youtu.be" && uri.path == "/watch" -> {
                val values = uri.rawQuery.orEmpty().split('&').mapNotNull { pair ->
                    val entry = pair.split('=', limit = 2)
                    if (entry.size == 2 && URLDecoder.decode(entry[0], "UTF-8") == "v") URLDecoder.decode(entry[1], "UTF-8") else null
                }
                values.singleOrNull()
            }
            host != "youtu.be" && parts.size == 2 && parts[0] in setOf("shorts", "embed") -> parts[1]
            else -> null
        }
        id?.takeIf(::isValidId)
    }.getOrNull()

    fun watchUrl(id: String): String {
        require(isValidId(id))
        return "https://www.youtube.com/watch?v=$id"
    }
}

/** Only approved identifiers and titles are retained; no history, cookies or credentials. */
class ApprovedVideoLibrary(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): List<ApprovedVideo> = runCatching {
        val raw = preferences.getString(VIDEOS_KEY, null) ?: return emptyList()
        if (raw.length > 65_536) return emptyList()
        val array = JSONArray(raw)
        val videos = linkedMapOf<String, ApprovedVideo>()
        for (index in 0 until minOf(array.length(), MAX_VIDEOS * 2)) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val title = cleanTitle(item.optString("title"))
            if (YouTubeVideoLinks.isValidId(id) && title.isNotBlank()) videos[id] = ApprovedVideo(id, title)
            if (videos.size >= MAX_VIDEOS) break
        }
        videos.values.toList()
    }.getOrDefault(emptyList())

    @Synchronized
    fun add(video: ApprovedVideo): Boolean {
        val title = cleanTitle(video.title)
        if (!YouTubeVideoLinks.isValidId(video.id) || title.isBlank()) return false
        val videos = read().toMutableList()
        val previous = videos.indexOfFirst { it.id == video.id }
        if (previous >= 0) videos[previous] = video.copy(title = title)
        else {
            if (videos.size >= MAX_VIDEOS) return false
            videos.add(video.copy(title = title))
        }
        return persist(videos)
    }

    @Synchronized
    fun remove(videoId: String): Boolean = persist(read().filterNot { it.id == videoId })

    fun observe(onChange: () -> Unit): () -> Unit {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == VIDEOS_KEY) onChange()
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        return { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun persist(videos: List<ApprovedVideo>): Boolean {
        val json = JSONArray()
        videos.forEach { json.put(JSONObject().put("id", it.id).put("title", it.title)) }
        return preferences.edit().putString(VIDEOS_KEY, json.toString()).commit()
    }

    companion object {
        const val PREFS_NAME = "approved_youtube_videos"
        const val VIDEOS_KEY = "videos_json"
        const val MAX_VIDEOS = 100
        const val MAX_TITLE_LENGTH = 100
        private fun cleanTitle(title: String): String = title.filterNot(Char::isISOControl).trim().take(MAX_TITLE_LENGTH)
    }
}

/** Child search is local and can only return titles already approved by an adult. */
fun filterApprovedVideos(videos: List<ApprovedVideo>, query: String): List<ApprovedVideo> {
    fun folded(text: String) = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
    val requested = folded(query.take(100).trim())
    return if (requested.isEmpty()) videos else videos.filter { requested in folded(it.title) }
}
