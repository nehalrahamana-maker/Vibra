package com.kazinehal.vibra.data

import com.kazinehal.vibra.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MusicRepository {

    // Curated real-world trending hits (Bollywood, Global Pop, Punjabi, Hip-Hop, R&B)
    val trendingTracks = listOf(
        Track(
            id = "real-1635014240",
            title = "Kesariya (From \"Brahmastra\")",
            artist = "Pritam, Arijit Singh & Amitabh Bhattacharya",
            album = "Kesariya (From \"Brahmastra\") - Single",
            genre = "Bollywood",
            duration = 268,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/9f/13/ca/9f13ca3b-e533-03e0-f19a-f0aaa774581d/196589311191.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/38/4c/5c/384c5c8f-3ff8-e457-b2f7-3158ce108649/mzaf_12389299033886433185.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1653219849",
            title = "Apna Bana Le (From \"Bhediya\")",
            artist = "Arijit Singh, Sachin-Jigar & Amitabh Bhattacharya",
            album = "Apna Bana Le (From \"Bhediya\") - Single",
            genre = "Bollywood",
            duration = 262,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/2e/0b/c0/2e0bc070-112f-a827-6ad8-6bc64f7caaff/840214460180.png/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/eb/27/61/eb2761c7-d606-0912-dff0-2dc6b69974bd/mzaf_2023722930851223219.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1440870375",
            title = "Starboy (feat. Daft Punk)",
            artist = "The Weeknd",
            album = "Starboy",
            genre = "R&B/Soul",
            duration = 230,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/b5/92/bb/b592bb72-52e3-e756-9b26-9f56d08f47ab/16UMGIM67864.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/11/71/d6/1171d6ad-3c96-e027-2af6-58028426588c/mzaf_15137631797407745471.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1894792753",
            title = "Morni",
            artist = "Diljit Dosanjh, Chani Nattan & Tru-Skool",
            album = "The Call of Panjab - EP",
            genre = "Worldwide",
            duration = 206,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/97/86/86/97868694-9413-a543-514a-a6374469ff97/859736427250_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/f5/6e/e4/f56ee414-2027-b605-7455-a24fbc433373/mzaf_3937962578481238111.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1193701392",
            title = "Shape of You",
            artist = "Ed Sheeran",
            album = "÷ (Deluxe)",
            genre = "Pop",
            duration = 234,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/15/e6/e8/15e6e8a4-4190-6a8b-86c3-ab4a51b88288/190295851286.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/44/c7/4f/44c74f0d-72dc-6143-d4d0-ba14d661ca0d/mzaf_9566898362556366703.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1193069189",
            title = "Tum Hi Ho (From \"Aashiqui 2\")",
            artist = "Arijit Singh",
            album = "Best of Arijit Singh",
            genre = "Bollywood",
            duration = 262,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/2d/11/b9/2d11b994-b4fa-19eb-953d-70b472165e95/8903431566911_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/77/59/c5/7759c5b2-b044-0e68-fea5-1fc4b84f312a/mzaf_3185920023072222766.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1411628233",
            title = "Believer",
            artist = "Imagine Dragons",
            album = "Evolve",
            genre = "Alternative",
            duration = 204,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/11/7a/b8/117ab805-6811-8929-18b9-0fad7baf0c25/17UMGIM98210.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/c0/3f/36/c03f367a-b66b-fd0a-a54c-30f8250c4410/mzaf_12768434238801682952.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1531596345",
            title = "Brown Munde",
            artist = "AP Dhillon, Gurinder Gill & Shinda Kahlon",
            album = "Brown Munde - Single",
            genre = "Hip-Hop/Rap",
            duration = 254,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/26/a3/ac/26a3ac64-69e4-95ec-80ab-1f5a477537d2/859742042973_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/97/74/69/977469be-a9d5-35a7-80ad-ebe12a799ccc/mzaf_804867738726203367.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1538003843",
            title = "Levitating",
            artist = "Dua Lipa",
            album = "Future Nostalgia",
            genre = "Pop",
            duration = 204,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/6c/11/d6/6c11d681-aa3a-d59e-4c2e-f77e181026ab/190295092665.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/59/dc/4d/59dc4dda-93ff-8f1c-c536-f005f6ea6af5/mzaf_3066686759813252385.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1071074558",
            title = "Agar Tum Saath Ho",
            artist = "Alka Yagnik & Arijit Singh",
            album = "Tamasha (Soundtrack)",
            genre = "Bollywood",
            duration = 341,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/cb/ac/67/cbac673a-9213-7118-bb4e-2440273afb03/8902894357470_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/75/a8/7d/75a87dcc-5b69-795d-7dcc-27d1c728f31f/mzaf_18055325784732588932.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1615585008",
            title = "As It Was",
            artist = "Harry Styles",
            album = "Harry's House",
            genre = "Pop",
            duration = 167,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/2a/19/fb/2a19fb85-2f70-9e44-f2a9-82abe679b88e/886449990061.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/67/10/16/67101606-3869-ca44-6c03-e13d6322cb51/mzaf_1135399237022217274.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1580955743",
            title = "Raataan Lambiyan",
            artist = "Tanishk Bagchi, Jubin Nautiyal & Asees Kaur",
            album = "Shershaah (Soundtrack)",
            genre = "Bollywood",
            duration = 230,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/61/65/ae/6165aee9-8bb9-0bd4-02b0-5d0f1e6257a3/886449510238.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/99/0c/38/990c381b-0530-8c0d-87a9-18b050b97f0a/mzaf_10418866714500530894.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1574968888",
            title = "STAY",
            artist = "The Kid LAROI & Justin Bieber",
            album = "STAY - Single",
            genre = "Pop",
            duration = 142,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/89/59/6a/89596ab9-fa3c-8d08-4d95-a6450fa2013c/886449400515.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/d7/4a/84/d74a84d5-9afa-761e-b632-baab55c2a23b/mzaf_11865500880477235553.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1169015785",
            title = "Channa Mereya",
            artist = "Pritam & Arijit Singh",
            album = "Ae Dil Hai Mushkil (Soundtrack)",
            genre = "Bollywood",
            duration = 289,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/bc/6e/4d/bc6e4d0c-adec-b431-7b60-16f5689f9664/886446201597.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/d5/f9/98/d5f998a7-0090-ee2d-03f8-557ad6c5bf65/mzaf_14251357991592637728.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1445949267",
            title = "Sunflower",
            artist = "Post Malone & Swae Lee",
            album = "Spider-Man: Into the Spider-Verse",
            genre = "Hip-Hop/Rap",
            duration = 158,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/4b/30/2c/4b302cb6-7a14-5464-4e97-0577e9d0be49/18UMGIM82277.rgb.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/98/f0/d6/98f0d67e-f8bf-762d-cac7-1c6b3b6b35dd/mzaf_4543283896248560946.plus.aac.p.m4a"
        )
    )

    /**
     * Live Online Search across millions of songs via iTunes Music Catalog.
     * Free, fast, zero ads, no API key required.
     */
    suspend fun searchOnline(query: String): List<Track> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext trendingTracks

        try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$encoded&media=music&entity=song&limit=30"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Vibra)")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val root = JSONObject(sb.toString())
                val results = root.optJSONArray("results") ?: return@withContext fallbackSearch(q)
                val out = mutableListOf<Track>()

                for (i in 0 until results.length()) {
                    val item = results.optJSONObject(i) ?: continue
                    val previewUrl = item.optString("previewUrl")
                    if (previewUrl.isNullOrBlank()) continue

                    val trackId = item.optLong("trackId", System.currentTimeMillis() + i)
                    val trackName = item.optString("trackName", "Unknown Track")
                    val artistName = item.optString("artistName", "Unknown Artist")
                    val albumName = item.optString("collectionName", "Single")
                    val genre = item.optString("primaryGenreName", "Pop")
                    val duration = item.optLong("trackTimeMillis", 180000L) / 1000
                    val art = item.optString("artworkUrl100").replace("100x100bb", "600x600bb")

                    out.add(
                        Track(
                            id = "online-$trackId",
                            title = trackName,
                            artist = artistName,
                            album = albumName,
                            genre = genre,
                            duration = duration,
                            artUrl = art.ifBlank { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80" },
                            audioUrl = previewUrl
                        )
                    )
                }

                if (out.isNotEmpty()) out else fallbackSearch(q)
            } else {
                fallbackSearch(q)
            }
        } catch (e: Exception) {
            fallbackSearch(q)
        }
    }

    private fun fallbackSearch(query: String): List<Track> {
        val q = query.lowercase()
        return trendingTracks.filter {
            it.title.lowercase().contains(q) ||
                    it.artist.lowercase().contains(q) ||
                    it.genre.lowercase().contains(q) ||
                    it.album.lowercase().contains(q)
        }
    }
}
