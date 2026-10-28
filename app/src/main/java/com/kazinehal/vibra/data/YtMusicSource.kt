package com.kazinehal.vibra.data

import com.kazinehal.vibra.model.Track
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * YouTube Music backend for the native app (Riva engine).
 *
 * Talks to the Vibra Cloudflare Worker:
 *   GET /api/search?q=...      → InnerTube search (WEB_REMIX → ANDROID client ladder)
 *   GET /api/stream?id=...     → fresh audio stream URL (ANDROID → IOS → WEB_REMIX ladder
 *                                + 403 pre-check)
 *
 * Only the videoId is stored on a Track — stream URLs expire in ~4 hours, so they are
 * resolved lazily right before playback.
 */
object YtMusicSource {

    const val API_BASE = "https://vibra-api.kazimdnehal.workers.dev"

    private const val CONNECT_TIMEOUT = 7000
    private const val READ_TIMEOUT = 7000

    /** Search YT Music. Throws on network/HTTP failure so callers can fall back. */
    fun search(query: String, limit: Int = 30): List<Track> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val body = httpGet("$API_BASE/api/search?q=$encoded&limit=$limit")
        val items = parseSearchResponse(body)

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
                    genre = item.optString("genre", "Online"),
                    duration = item.optLong("duration", 210L).coerceAtLeast(1L),
                    artUrl = item.optString("art")
                        .ifBlank { "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" },
                    audioUrl = "", // resolved lazily in VibraPlayer.playTrack
                    videoId = videoId
                )
            )
        }
        return out
    }

    /**
     * Resolve a fresh, verified audio stream URL for a videoId.
     * Returns null when every rung of the Worker's client ladder failed.
     */
    fun resolveStream(videoId: String): String? {
        if (videoId.isBlank()) return null
        val encoded = URLEncoder.encode(videoId, "UTF-8")
        return try {
            val body = httpGet("$API_BASE/api/stream?id=$encoded")
            val url = JSONObject(body).optString("url")
            url.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    /** New shape is {tracks:[...]}, legacy deployed Worker returns a bare array. */
    private fun parseSearchResponse(body: String): JSONArray {
        val trimmed = body.trim()
        return if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("tracks") ?: JSONArray()
        }
    }

    private fun httpGet(urlString: String): String {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = READ_TIMEOUT
            setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Vibra)")
        }
        try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw java.io.IOException("HTTP ${conn.responseCode}")
            }
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
