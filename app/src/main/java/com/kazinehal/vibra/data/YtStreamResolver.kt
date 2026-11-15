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

    private const val CONNECT_TIMEOUT = 4000
    private const val READ_TIMEOUT = 6000
    private const val WORKER_BASE = "https://vibra-api.kazimdnehal.workers.dev"

    fun resolve(videoId: String): YtStream? {
        if (videoId.isBlank()) return null

        // 1. Worker FIRST — own backend: fastest and most reliable path
        try {
            val stream = resolveWorker(videoId)
            if (stream != null && stream.audioUrl.isNotBlank()) {
                return stream
            }
        } catch (e: Exception) {
            android.util.Log.w("YtStreamResolver", "Worker failed: ${e.message}")
        }

        // 2. Invidious Ladder
        for (instance in INVIDIOUS_INSTANCES) {
            try {
                val stream = resolveInvidious(instance, videoId)
                if (stream != null && stream.audioUrl.isNotBlank()) {
                    // Proxy through worker to avoid mobile IP 403 on googlevideo.com
                    val proxiedUrl = proxyUrl(stream.audioUrl)
                    return stream.copy(audioUrl = proxiedUrl)
                }
            } catch (e: Exception) {
                // try next
            }
        }

        // 3. Piped Ladder
        for (instance in PIPED_INSTANCES) {
            try {
                val stream = resolvePiped(instance, videoId)
                if (stream != null && stream.audioUrl.isNotBlank()) {
                    val proxiedUrl = proxyUrl(stream.audioUrl)
                    return stream.copy(audioUrl = proxiedUrl)
                }
            } catch (e: Exception) {
                // try next
            }
        }

        return null
    }

    /**
     * Wraps a raw googlevideo.com URL in the worker's /api/audio relay.
     * This avoids 403 errors caused by IP mismatch between Cloudflare egress
     * (which resolved the URL) and the mobile device's IP.
     */
    private fun proxyUrl(rawUrl: String): String {
        if (rawUrl.contains("vibra-api.kazimdnehal.workers.dev")) return rawUrl
        val encoded = URLEncoder.encode(rawUrl, "UTF-8")
        return "$WORKER_BASE/api/audio?url=$encoded"
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
        val apiUrl = "$WORKER_BASE/api/stream?id=$encoded"
        val jsonStr = httpGet(apiUrl) ?: return null
        val root = JSONObject(jsonStr)

        // Worker returns {url, durationSeconds} or just {url}
        val url = root.optString("url")
        if (url.isNotBlank()) {
            val duration = root.optLong("durationSeconds", 0L)
            return YtStream(audioUrl = url, durationSeconds = duration, source = "Worker")
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
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
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
