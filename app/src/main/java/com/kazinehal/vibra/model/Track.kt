package com.kazinehal.vibra.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "Vibra Master",
    val genre: String = "Synthwave",
    val duration: Long = 210, // in seconds
    val artUrl: String,
    val audioUrl: String,
    val lyrics: String = "",
    // YouTube Music: videoId is stored, the stream URL is resolved lazily at play
    // time (YouTube URLs expire in ~4h — never persist them)
    val videoId: String = ""
)
