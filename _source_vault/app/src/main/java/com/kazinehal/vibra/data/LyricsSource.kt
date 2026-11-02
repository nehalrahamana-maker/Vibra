package com.kazinehal.vibra.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Lyrics from LRCLIB (https://lrclib.net) — free, no API key, no rate-limit signup.
 *
 * 1. /api/get        — exact match using track + artist + duration
 * 2. /api/search     — fuzzy fallback when the exact lookup 404s
 *
 * Prefers plain lyrics; falls back to synced (LRC) lyrics with the timestamps stripped,
 * since the APK renders a simple scrolling block, not word-by-word sync (yet).
 */
object LyricsSource {

    /** Fetch lyrics for a track. Returns null when LRCLIB has nothing. Blocking — call on IO. */
    fun fetch(title: String, artist: String, durationSec: Long = 0L): String? {
        val t = title.trim()
        val a = artist.trim()
        if (t.isEmpty()) return null

        // 1. Exact lookup
        try {
            var q = "track_name=${enc(t)}&artist_name=${enc(a)}"
            if (durationSec > 0) q += "&duration=$durationSec"
            val body = httpGet("https://lrclib.net/api/get?$q")
            if (body != null) {
                val lyrics = pickLyrics(JSONObject(body))
                if (!lyrics.isNullOrBlank()) return lyrics
            }
        } catch (_: Exception) {
            // fall through to search
        }

        // 2. Fuzzy search fallback
        try {
            val body = httpGet("https://lrclib.net/api/search?track_name=${enc(t)}&artist_name=${enc(a)}")
            if (body != null) {
                val arr = JSONArray(body)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val lyrics = pickLyrics(obj)
                    if (!lyrics.isNullOrBlank()) return lyrics
                }
            }
        } catch (_: Exception) {
            // no lyrics — acceptable
        }

        return null
    }

    private fun pickLyrics(o: JSONObject): String? {
        val plain = o.optString("plainLyrics")
        if (!plain.isNullOrBlank()) return plain.trim()

        val synced = o.optString("syncedLyrics")
        if (!synced.isNullOrBlank()) {
            // LRC format: "[00:12.34] line text" → strip timestamps, keep the words
            return synced.lines()
                .map { line -> line.replace(Regex("""\[\d{1,2}:\d{2}(\.\d+)?]"""), "").trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
                .trim()
        }
        return null
    }

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

    private fun httpGet(urlString: String): String? {
        return try {
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "Riva/2.0 (Android lyrics client)")
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
