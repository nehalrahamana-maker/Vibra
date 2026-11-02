package com.kazinehal.vibra.data

import com.kazinehal.vibra.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MusicRepository {

    /**
     * Curated trending YouTube Music hits with verified YouTube videoIds.
     * All audio streams resolve live from YouTube Music.
     */
    val trendingTracks = listOf(
        Track(
            id = "yt-BddP6PYo2gs",
            title = "Kesariya",
            artist = "Pritam, Arijit Singh",
            album = "Brahmastra",
            genre = "Bollywood",
            duration = 268,
            artUrl = "https://i.ytimg.com/vi/BddP6PYo2gs/hqdefault.jpg",
            audioUrl = "",
            videoId = "BddP6PYo2gs"
        ),
        Track(
            id = "yt-34Na4j8AVgA",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            genre = "Global Pop",
            duration = 230,
            artUrl = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
            audioUrl = "",
            videoId = "34Na4j8AVgA"
        ),
        Track(
            id = "yt-ElZfdU54Cp8",
            title = "Apna Bana Le",
            artist = "Arijit Singh, Sachin-Jigar",
            album = "Bhediya",
            genre = "Bollywood",
            duration = 262,
            artUrl = "https://i.ytimg.com/vi/ElZfdU54Cp8/hqdefault.jpg",
            audioUrl = "",
            videoId = "ElZfdU54Cp8"
        ),
        Track(
            id = "yt-7wtfhZwyrcc",
            title = "Believer",
            artist = "Imagine Dragons",
            album = "Evolve",
            genre = "Rock/Pop",
            duration = 204,
            artUrl = "https://i.ytimg.com/vi/7wtfhZwyrcc/hqdefault.jpg",
            audioUrl = "",
            videoId = "7wtfhZwyrcc"
        ),
        Track(
            id = "yt-VNs_cCtdbPc",
            title = "Brown Munde",
            artist = "AP Dhillon, Gurinder Gill",
            album = "Brown Munde",
            genre = "Punjabi",
            duration = 254,
            artUrl = "https://i.ytimg.com/vi/VNs_cCtdbPc/hqdefault.jpg",
            audioUrl = "",
            videoId = "VNs_cCtdbPc"
        ),
        Track(
            id = "yt-JGwWNGJdvx8",
            title = "Shape of You",
            artist = "Ed Sheeran",
            album = "Divide",
            genre = "Global Pop",
            duration = 234,
            artUrl = "https://i.ytimg.com/vi/JGwWNGJdvx8/hqdefault.jpg",
            audioUrl = "",
            videoId = "JGwWNGJdvx8"
        ),
        Track(
            id = "yt-IJq0yyWug1k",
            title = "Tum Hi Ho",
            artist = "Arijit Singh",
            album = "Aashiqui 2",
            genre = "Bollywood",
            duration = 262,
            artUrl = "https://i.ytimg.com/vi/IJq0yyWug1k/hqdefault.jpg",
            audioUrl = "",
            videoId = "IJq0yyWug1k"
        ),
        Track(
            id = "yt-5p_PZ22n4pE",
            title = "Morni",
            artist = "Diljit Dosanjh",
            album = "The Call of Panjab",
            genre = "Punjabi",
            duration = 206,
            artUrl = "https://i.ytimg.com/vi/5p_PZ22n4pE/hqdefault.jpg",
            audioUrl = "",
            videoId = "5p_PZ22n4pE"
        ),
        Track(
            id = "yt-TUVcZfQe-Kw",
            title = "Levitating",
            artist = "Dua Lipa",
            album = "Future Nostalgia",
            genre = "Global Pop",
            duration = 203,
            artUrl = "https://i.ytimg.com/vi/TUVcZfQe-Kw/hqdefault.jpg",
            audioUrl = "",
            videoId = "TUVcZfQe-Kw"
        ),
        Track(
            id = "yt-sK7riqg2mr4",
            title = "Agar Tum Saath Ho",
            artist = "Alka Yagnik, Arijit Singh",
            album = "Tamasha",
            genre = "Bollywood",
            duration = 341,
            artUrl = "https://i.ytimg.com/vi/sK7riqg2mr4/hqdefault.jpg",
            audioUrl = "",
            videoId = "sK7riqg2mr4"
        ),
        Track(
            id = "yt-4NRXx6U8ABQ",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            genre = "Global Pop",
            duration = 200,
            artUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            audioUrl = "",
            videoId = "4NRXx6U8ABQ"
        ),
        Track(
            id = "yt-kTJczUoc26U",
            title = "Stay",
            artist = "The Kid LAROI, Justin Bieber",
            album = "F*CK LOVE 3",
            genre = "Global Pop",
            duration = 141,
            artUrl = "https://i.ytimg.com/vi/kTJczUoc26U/hqdefault.jpg",
            audioUrl = "",
            videoId = "kTJczUoc26U"
        ),
        Track(
            id = "yt-mRD0-GxqHVo",
            title = "Heat Waves",
            artist = "Glass Animals",
            album = "Dreamland",
            genre = "Global Pop",
            duration = 239,
            artUrl = "https://i.ytimg.com/vi/mRD0-GxqHVo/hqdefault.jpg",
            audioUrl = "",
            videoId = "mRD0-GxqHVo"
        ),
        Track(
            id = "yt-4tywp83zkmk",
            title = "Cheques",
            artist = "Shubh",
            album = "Still Rollin",
            genre = "Punjabi",
            duration = 183,
            artUrl = "https://i.ytimg.com/vi/4tywp83zkmk/hqdefault.jpg",
            audioUrl = "",
            videoId = "4tywp83zkmk"
        ),
        Track(
            id = "yt-_XBVWlI8TsQ",
            title = "Kahani Suno 2.0",
            artist = "Kaifi Khalil",
            album = "Kahani Suno",
            genre = "Chill",
            duration = 174,
            artUrl = "https://i.ytimg.com/vi/_XBVWlI8TsQ/hqdefault.jpg",
            audioUrl = "",
            videoId = "_XBVWlI8TsQ"
        ),
        Track(
            id = "yt-5Eqb_-j3FDA",
            title = "Pasoori",
            artist = "Ali Sethi, Shae Gill",
            album = "Coke Studio Season 14",
            genre = "Global Pop",
            duration = 224,
            artUrl = "https://i.ytimg.com/vi/5Eqb_-j3FDA/hqdefault.jpg",
            audioUrl = "",
            videoId = "5Eqb_-j3FDA"
        ),
        Track(
            id = "yt-pX2lUr3k_yM",
            title = "Still Rollin",
            artist = "Shubh",
            album = "Still Rollin",
            genre = "Punjabi",
            duration = 174,
            artUrl = "https://i.ytimg.com/vi/pX2lUr3k_yM/hqdefault.jpg",
            audioUrl = "",
            videoId = "pX2lUr3k_yM"
        ),
        Track(
            id = "yt-tQ0yjYUFKAE",
            title = "Peaches",
            artist = "Justin Bieber",
            album = "Justice",
            genre = "Global Pop",
            duration = 198,
            artUrl = "https://i.ytimg.com/vi/tQ0yjYUFKAE/hqdefault.jpg",
            audioUrl = "",
            videoId = "tQ0yjYUFKAE"
        ),
        Track(
            id = "yt-60ItHLz5WEA",
            title = "Faded",
            artist = "Alan Walker",
            album = "Different World",
            genre = "Electronic",
            duration = 212,
            artUrl = "https://i.ytimg.com/vi/60ItHLz5WEA/hqdefault.jpg",
            audioUrl = "",
            videoId = "60ItHLz5WEA"
        ),
        Track(
            id = "yt-D_kUqG9C2jU",
            title = "Mi Amor",
            artist = "Sharn, 407Sound",
            album = "Mi Amor",
            genre = "Punjabi",
            duration = 208,
            artUrl = "https://i.ytimg.com/vi/D_kUqG9C2jU/hqdefault.jpg",
            audioUrl = "",
            videoId = "D_kUqG9C2jU"
        )
    )

    /** Search YouTube Music online */
    suspend fun searchOnline(query: String): List<Track> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext trendingTracks

        try {
            val results = YtMusicSource.search(q, 30)
            if (results.isNotEmpty()) {
                return@withContext results
            }
        } catch (e: Exception) {
            // fall through
        }

        // Fallback: iTunes live search — free 30s previews, no key, no dead instances
        try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$encoded&media=music&entity=song&limit=25"
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Vibra)")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val root = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val arr = root.optJSONArray("results")
                val out = mutableListOf<Track>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val item = arr.optJSONObject(i) ?: continue
                        val previewUrl = item.optString("previewUrl")
                        if (previewUrl.isNullOrBlank()) continue
                        out.add(
                            Track(
                                id = "online-${item.optLong("trackId", System.currentTimeMillis() + i)}",
                                title = item.optString("trackName", "Unknown Track"),
                                artist = item.optString("artistName", "Unknown Artist"),
                                album = item.optString("collectionName", "Single"),
                                genre = item.optString("primaryGenreName", "Pop"),
                                duration = item.optLong("trackTimeMillis", 180000L) / 1000,
                                artUrl = item.optString("artworkUrl100").replace("100x100bb", "600x600bb"),
                                audioUrl = previewUrl
                            )
                        )
                    }
                }
                if (out.isNotEmpty()) return@withContext out
            }
        } catch (e: Exception) {
            // fall through to local
        }

        // Fallback filter on local seed tracks if offline
        return@withContext trendingTracks.filter {
            it.title.contains(q, ignoreCase = true) || it.artist.contains(q, ignoreCase = true)
        }
    }
}
