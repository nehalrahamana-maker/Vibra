package com.kazinehal.vibra.update

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** A newer release published on GitHub. */
data class AppUpdate(
    val version: String,   // e.g. "2.1.0" (tag without leading v)
    val htmlUrl: String,   // release page
    val notes: String,     // release body (changelog)
    val apkUrl: String?    // first .apk asset, when attached
)

/**
 * Zero-cost auto-update: queries the public GitHub Releases API (no key, no
 * backend) once per day and reports whether a newer version than the installed
 * one exists. All calls are blocking — run them on IO.
 */
object UpdateChecker {

    private const val REPO = "nehalrahamana-maker/Vibra"
    private const val CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000 // one check / day
    private const val PREFS = "vibra_update"

    /** Blocking network check. Returns null when up-to-date or on any failure. */
    fun checkNow(currentVersion: String): AppUpdate? {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL("https://api.github.com/repos/$REPO/releases/latest")
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "Vibra-Updater")

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)

            val tag = json.optString("tag_name").trimStart('v')
            if (!isNewer(tag, currentVersion)) return null

            // Prefer a direct .apk asset so "Update" starts the download at once.
            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.optString("name").endsWith(".apk")) {
                        apkUrl = a.optString("browser_download_url")
                        break
                    }
                }
            }

            AppUpdate(
                version = tag,
                htmlUrl = json.optString("html_url"),
                notes = json.optString("body"),
                apkUrl = apkUrl
            )
        } catch (_: Exception) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    /**
     * Numeric segment-wise compare ("2.1.0" > "2.0.0", "10.0.0" > "9.9.9").
     * Non-numeric segments count as 0; equal versions are not "newer".
     */
    fun isNewer(remote: String, current: String): Boolean {
        val r = remote.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        val c = current.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        val len = maxOf(r.size, c.size)
        for (i in 0 until len) {
            val rv = r.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (rv != cv) return rv > cv
        }
        return false
    }

    /** True at most once per [CHECK_INTERVAL_MS] so the API is never hammered. */
    fun shouldCheck(context: Context): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return System.currentTimeMillis() - p.getLong("last_check", 0L) >= CHECK_INTERVAL_MS
    }

    fun markChecked(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong("last_check", System.currentTimeMillis())
            .apply()
    }
}
