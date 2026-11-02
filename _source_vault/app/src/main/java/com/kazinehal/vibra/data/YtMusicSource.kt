package com.kazinehal.vibra.data

import com.kazinehal.vibra.model.Track
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * YouTube Music backend data source for Riva.
 * Connects to YouTube Music InnerTube and community gateways.
 */
object YtMusicSource {

    const val API_BASE = "https://vibra-api.kazimdnehal.workers.dev"

    private const val CONNECT_TIMEOUT = 5000
    private const val READ_TIMEOUT = 6000

    /** Search YouTube Music for tracks */
    fun search(query: String, limit: Int = 25): List<Track> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        // Worker only — the community Piped/Invidious instances below were verified
        // dead (403/525) and cost 5-12s EACH in timeouts before failing. When the
        // Worker misses, fail fast so the caller can fall back (iTunes/local)
        // instead of stalling the search UI for half a minute.
        return try {
            searchWorker(q, limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Find videoId for a given title and artist */
    fun findVideoId(title: String, artist: String): String? {
        val cleanTitle = title.replace(Regex("""\(From "[^"]*"\)"""), "")
            .replace(Regex("""\(feat\.[^)]*\)"""), "")
            .replace(Regex("""\[Official[^\]]*\]"""), "")
            .trim()
        val query = "$cleanTitle $artist".trim()
        val results = search(query, 5)
        return results.firstOrNull()?.videoId
    }

    /** Resolve playable audio stream URL using YtStreamResolver */
    fun resolveStream(videoId: String): String? {
        return YtStreamResolver.resolve(videoId)?.audioUrl
    }

    private fun searchWorker(query: String, limit: Int): List<Track> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val body = httpGet("$API_BASE/api/search?q=$encoded&limit=$limit") ?: return emptyList()
        val items = parseWorkerSearchResponse(body)

        val out = mutableListOf<Track>()
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            val videoId = item.optString("videoId")
            val title = item.optString("title")
            if (videoId.isBlank() || title.isBlank()) continue

            out.add(
                Track(
                    id = item.optString("id").ifBlank { "yt-$videoId" },
                    title = title,
                    artist = item.optString("artist", "YouTube Artist"),
                    album = item.optString("album", "YouTube Music"),
                    genre = item.optString("genre", "Music"),
                    duration = item.optLong("duration", 210L).coerceAtLeast(1L),
                    artUrl = item.optString("art").ifBlank { "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" },
                    audioUrl = "",
                    videoId = videoId
                )
            )
        }
        return out
    }

    private fun searchPiped(instance: String, query: String, limit: Int): List<Track> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val body = httpGet("$instance/search?q=$encoded&filter=music_songs") ?: return emptyList()
        val root = JSONObject(body)
        val items = root.optJSONArray("items") ?: return emptyList()

        val out = mutableListOf<Track>()
        val max = minOf(items.length(), limit)
        for (i in 0 until max) {
            val item = items.optJSONObject(i) ?: continue
            val url = item.optString("url", "")
            val videoId = if (url.contains("v=")) url.substringAfter("v=") else url.removePrefix("/watch?v=")
            val title = item.optString("title")
            if (videoId.isBlank() || title.isBlank()) continue

            out.add(
                Track(
                    id = "yt-$videoId",
                    title = title,
                    artist = item.optString("uploaderName", "YouTube Artist"),
                    album = "YouTube Music",
                    genre = "Music",
                    duration = item.optLong("duration", 210L).coerceAtLeast(1L),
                    artUrl = item.optString("thumbnail").ifBlank { "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" },
                    audioUrl = "",
                    videoId = videoId
                )
            )
        }
        return out
    }

    private fun searchInvidious(instance: String, query: String, limit: Int): List<Track> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val body = httpGet("$instance/api/v1/search?q=$encoded&type=video") ?: return emptyList()
        val items = JSONArray(body)

        val out = mutableListOf<Track>()
        val max = minOf(items.length(), limit)
        for (i in 0 until max) {
            val item = items.optJSONObject(i) ?: continue
            val videoId = item.optString("videoId")
            val title = item.optString("title")
            if (videoId.isBlank() || title.isBlank()) continue

            out.add(
                Track(
                    id = "yt-$videoId",
                    title = title,
                    artist = item.optString("author", "YouTube Artist"),
                    album = "YouTube Music",
                    genre = "Music",
                    duration = item.optLong("lengthSeconds", 210L).coerceAtLeast(1L),
                    artUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                    audioUrl = "",
                    videoId = videoId
                )
            )
        }
        return out
    }

    private fun parseWorkerSearchResponse(body: String): JSONArray {
        val trimmed = body.trim()
        return if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("tracks") ?: JSONArray()
        }
    }

    private fun httpGet(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
