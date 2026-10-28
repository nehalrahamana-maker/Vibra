package com.kazinehal.vibra.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kazinehal.vibra.data.YtMusicSource
import com.kazinehal.vibra.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VibraPlayer(context: Context) {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main)

    var currentTrack by mutableStateOf<Track?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentPositionMs by mutableLongStateOf(0L)
        private set

    var durationMs by mutableLongStateOf(1L)
        private set

    var queue = mutableListOf<Track>()
    private var currentIndex = 0

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(1L)
                } else if (playbackState == Player.STATE_ENDED) {
                    next()
                }
            }
        })

        // Position ticker
        scope.launch {
            while (true) {
                if (isPlaying) {
                    currentPositionMs = exoPlayer.currentPosition
                }
                delay(250)
            }
        }
    }

    fun playTrack(track: Track) {
        currentTrack = track

        // Always resolve full YouTube Music stream (InnerTube ladder via Worker)
        if (track.videoId.isNotBlank() || track.audioUrl.contains("itunes.apple.com") || track.audioUrl.isBlank()) {
            scope.launch {
                val vId = if (track.videoId.isNotBlank()) {
                    track.videoId
                } else {
                    val cleanQuery = track.title.replace(Regex("""\(From "[^"]*"\)"""), "").trim()
                    val searchResults = withContext(Dispatchers.IO) {
                        YtMusicSource.search("$cleanQuery ${track.artist}", 1)
                    }
                    searchResults.firstOrNull()?.videoId ?: ""
                }

                if (vId.isNotBlank()) {
                    val resolved = withContext(Dispatchers.IO) {
                        YtMusicSource.resolveStream(vId)
                    }
                    // Ignore if user already switched to another track
                    if (currentTrack?.id != track.id) return@launch
                    if (resolved != null) {
                        startPlayback(resolved)
                        return@launch
                    }
                }

                // Fallback to preview or cached audioUrl if worker stream resolution was blocked
                if (track.audioUrl.isNotBlank()) {
                    startPlayback(track.audioUrl)
                } else {
                    android.util.Log.w("VibraPlayer", "Stream resolve failed for ${track.title}")
                }
            }
            return
        }

        startPlayback(track.audioUrl)
    }

    private fun startPlayback(url: String) {
        if (url.isBlank()) return
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        currentPositionMs = positionMs
    }

    fun next() {
        if (queue.isEmpty()) return
        currentIndex = (currentIndex + 1) % queue.size
        playTrack(queue[currentIndex])
    }

    fun previous() {
        if (currentPositionMs > 3000) {
            seekTo(0)
            return
        }
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) queue.size - 1 else currentIndex - 1
        playTrack(queue[currentIndex])
    }

    fun release() {
        exoPlayer.release()
    }
}
