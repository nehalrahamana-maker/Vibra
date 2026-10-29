package com.kazinehal.vibra.data

import android.content.Context
import android.content.SharedPreferences
import com.kazinehal.vibra.model.Track
import org.json.JSONArray
import org.json.JSONObject

object UserLibraryManager {

    private const val PREF_NAME = "riva_library_prefs"
    private const val KEY_LIKED = "key_liked_tracks"
    private const val KEY_HISTORY = "key_history_tracks"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isLiked(trackId: String): Boolean {
        return getLikedTracks().any { it.id == trackId }
    }

    fun toggleLike(track: Track): Boolean {
        val list = getLikedTracks().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == track.id }
        val nowLiked: Boolean
        if (existingIndex >= 0) {
            list.removeAt(existingIndex)
            nowLiked = false
        } else {
            list.add(0, track)
            nowLiked = true
        }
        saveTracks(KEY_LIKED, list)
        return nowLiked
    }

    fun getLikedTracks(): List<Track> {
        return loadTracks(KEY_LIKED)
    }

    fun addToHistory(track: Track) {
        val list = loadTracks(KEY_HISTORY).toMutableList()
        list.removeAll { it.id == track.id }
        list.add(0, track)
        if (list.size > 50) {
            list.removeLast()
        }
        saveTracks(KEY_HISTORY, list)
    }

    fun getHistoryTracks(): List<Track> {
        return loadTracks(KEY_HISTORY)
    }

    private fun saveTracks(key: String, tracks: List<Track>) {
        val p = prefs ?: return
        val array = JSONArray()
        for (t in tracks) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("artist", t.artist)
                put("album", t.album)
                put("genre", t.genre)
                put("duration", t.duration)
                put("artUrl", t.artUrl)
                put("videoId", t.videoId)
            }
            array.put(obj)
        }
        p.edit().putString(key, array.toString()).apply()
    }

    private fun loadTracks(key: String): List<Track> {
        val p = prefs ?: return emptyList()
        val raw = p.getString(key, null) ?: return emptyList()
        val list = mutableListOf<Track>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    Track(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        artist = obj.optString("artist"),
                        album = obj.optString("album", "YouTube Music"),
                        genre = obj.optString("genre", "Music"),
                        duration = obj.optLong("duration", 210L),
                        artUrl = obj.optString("artUrl"),
                        audioUrl = "",
                        videoId = obj.optString("videoId")
                    )
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        return list
    }
}

