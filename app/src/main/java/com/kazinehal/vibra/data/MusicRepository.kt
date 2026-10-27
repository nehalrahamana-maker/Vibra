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
        ),
        // Tamil Hits (As requested by user)
        Track(
            id = "real-1712662876",
            title = "Naa Ready (From \"Leo\")",
            artist = "Anirudh Ravichander, Thalapathy Vijay & Asal Kolaar",
            album = "Leo (Soundtrack)",
            genre = "Tamil",
            duration = 248,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/13/a8/70/13a87001-28ba-1bc7-0ca6-52cf10dd6f52/196871556415.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/2b/08/0d/2b080da8-45f6-ef77-ad5c-79ca355e6cda/mzaf_6699968492299283465.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1699735311",
            title = "Kaavaalaa (From \"Jailer\")",
            artist = "Anirudh Ravichander, Shilpa Rao & Arunraja Kamaraj",
            album = "Jailer (Soundtrack)",
            genre = "Tamil",
            duration = 191,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/2c/df/14/2cdf140e-6d11-a98d-bfbf-bc5e30c3c4a1/197189528187.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/40/11/6e/40116ec0-3f0e-86f3-ec06-f4153e66264b/mzaf_13483424612004799224.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1609971105",
            title = "Arabic Kuthu (From \"Beast\")",
            artist = "Anirudh Ravichander & Jonita Gandhi",
            album = "Beast (Soundtrack)",
            genre = "Tamil",
            duration = 280,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/e9/19/b9/e919b921-d5a8-9e9a-8508-3551da375aee/196626458629.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/58/ac/9a/58ac9a48-9e3e-15c9-5085-3a13a6187992/mzaf_12322264242631159198.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1708912590",
            title = "Tum Tum (From \"Enemy\")",
            artist = "S.S. Thaman, Sri Vardhini & Aditi Bhavaraju",
            album = "Enemy (Soundtrack)",
            genre = "Tamil",
            duration = 229,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/0c/d7/2b/0cd72bbd-b40d-6e85-5ad1-124d6892525f/8904337225117.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/29/c6/1a/29c61a3a-3f49-652e-d6ec-b064d2cf209f/mzaf_15465581418525227081.plus.aac.p.m4a"
        ),
        // Telugu & Punjabi Hits
        Track(
            id = "real-1594544794",
            title = "Naatu Naatu (From \"RRR\")",
            artist = "Rahul Sipligunj, Kaala Bhairava & M.M. Keeravaani",
            album = "RRR (Soundtrack)",
            genre = "Telugu",
            duration = 214,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/dd/39/14/dd3914e5-a2f3-b355-51f3-9a1f0e3ca246/8903431853592_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/8e/dd/a4/8edda474-3fe1-3fe6-43d3-765db520a29b/mzaf_11740310005222997767.plus.aac.p.m4a"
        ),
        Track(
            id = "real-1826848930",
            title = "295",
            artist = "Sidhu Moose Wala",
            album = "Moosetape",
            genre = "Hip-Hop/Rap",
            duration = 270,
            artUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/97/69/58/976958ae-725e-bd41-6755-f0921c697840/810063889609_cover.jpg/600x600bb.jpg",
            audioUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/7f/f3/6d/7ff36d63-b933-3993-cd2f-f3fd770c3763/mzaf_12675758250838366519.plus.aac.p.m4a"
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
