package com.kazinehal.vibra.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class YtStream(
    val audioUrl: String,
    val durationSeconds: Long = 0L,
    val source: String = "YouTube"
)

/**
 * 100% Genuine YouTube Music Audio Stream Resolver.
 * Resolves full-length Opus/AAC streams for any YouTube videoId with multi-instance redundancy.
 */
object YtStreamResolver {

    private val INVIDIOUS_INSTANCES = listOf(
        "https://inv.tux.pizza",
        "https://vid.puffyan.us",
        "https://invidious.nerdvpn.de",
        "https://iv.ggtyler.dev",
        "https://invidious.jing.rocks",
        "https://y.com.sb"
    )

    private val PIPED_INSTANCES = listOf(
        "https://pipedapi.adminforge.de",
        "https://api.piped.private.coffee",
        "https://pipedapi.tokhmi.xyz",
        "https://piped-api.garudalinux.org"
    )

    private const val CONNECT_TIMEOUT = 5000
    private const val READ_TIMEOUT = 7000

    fun resolve(videoId: String): YtStream? {
        if (videoId.isBlank()) return null

        // 1. Invidious Ladder
        for (instance in INVIDIOUS_INSTANCES) {
            try {
                val stream = resolveInvidious(instance, videoId)
                if (stream != null && stream.audioUrl.isNotBlank()) {
                    return stream
                }
            } catch (e: Exception) {
                // try next
            }
        }

        // 2. Piped Ladder
        for (instance in PIPED_INSTANCES) {
            try {
                val stream = resolvePiped(instance, videoId)
                if (stream != null && stream.audioUrl.isNotBlank()) {
                    return stream
                }
            } catch (e: Exception) {
                // try next
            }
        }

        // 3. Worker Relay
        try {
            val stream = resolveWorker(videoId)
            if (stream != null && stream.audioUrl.isNotBlank()) {
                return stream
            }
        } catch (e: Exception) {
            // ignore
        }

        return null
    }

    private fun resolveInvidious(instance: String, videoId: String): YtStream? {
        val apiUrl = "$instance/api/v1/videos/$videoId"
        val jsonStr = httpGet(apiUrl) ?: return null
        val root = JSONObject(jsonStr)

        val duration = root.optLong("lengthSeconds", 0L)
        val formats = root.optJSONArray("adaptiveFormats") ?: return null

        var bestUrl: String? = null
        var maxBitrate = 0L

        for (i in 0 until formats.length()) {
            val format = formats.optJSONObject(i) ?: continue
            val type = format.optString("type", "")
            if (!type.startsWith("audio/")) continue

            val url = format.optString("url")
            if (url.isBlank()) continue

            val bitrate = format.optLong("bitrate", 0L)
            if (bitrate > maxBitrate || bestUrl == null) {
                maxBitrate = bitrate
                bestUrl = url
            }
        }

        if (bestUrl != null) {
            val finalUrl = if (bestUrl.startsWith("/")) "$instance$bestUrl" else bestUrl
            return YtStream(
                audioUrl = finalUrl,
                durationSeconds = duration,
                source = "Invidious"
            )
        }
        return null
    }

    private fun resolvePiped(instance: String, videoId: String): YtStream? {
        val apiUrl = "$instance/streams/$videoId"
        val jsonStr = httpGet(apiUrl) ?: return null
        val root = JSONObject(jsonStr)

        val duration = root.optLong("duration", 0L)
        val streams = root.optJSONArray("audioStreams") ?: return null

        var bestUrl: String? = null
        var maxBitrate = 0L

        for (i in 0 until streams.length()) {
            val stream = streams.optJSONObject(i) ?: continue
            val url = stream.optString("url")
            if (url.isBlank()) continue

            val bitrate = stream.optLong("bitrate", 0L)
            if (bitrate > maxBitrate || bestUrl == null) {
                maxBitrate = bitrate
                bestUrl = url
            }
        }

        if (bestUrl != null) {
            return YtStream(
                audioUrl = bestUrl,
                durationSeconds = duration,
                source = "Piped"
            )
        }
        return null
    }

    private fun resolveWorker(videoId: String): YtStream? {
        val encoded = URLEncoder.encode(videoId, "UTF-8")
        val apiUrl = "https://vibra-api.kazimdnehal.workers.dev/api/stream?id=$encoded"
        val jsonStr = httpGet(apiUrl) ?: return null
        val root = JSONObject(jsonStr)
        val url = root.optString("url")
        if (url.isNotBlank()) {
            return YtStream(audioUrl = url, source = "Worker")
        }
        return null
    }

    private fun httpGet(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
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
