package com.kazinehal.vibra.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kazinehal.vibra.data.MusicRepository
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

    var isShuffleOn by mutableStateOf(false)
    var isRepeatOn by mutableStateOf(false)

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
                    if (isRepeatOn) {
                        seekTo(0)
                        exoPlayer.play()
                    } else {
                        next()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("VibraPlayer", "ExoPlayer error: ${error.message}", error)
                // If stream failed (e.g. 403 expired), fallback to instant preview audio
                val track = currentTrack ?: return
                scope.launch {
                    val fallback = withContext(Dispatchers.IO) {
                        MusicRepository.findInstantAudio(track.title, track.artist)
                    }
                    if (fallback != null && fallback != track.audioUrl) {
                        startPlayback(fallback)
                    }
                }
            }
        })

        // Position ticker for real-time scrubber update
        scope.launch {
            while (true) {
                if (isPlaying) {
                    currentPositionMs = exoPlayer.currentPosition
                }
                delay(200)
            }
        }
    }

    fun playTrack(track: Track) {
        currentTrack = track

        // CRITICAL FIX: If track has a playable URL (iTunes preview or cached audio),
        // START PLAYBACK IMMEDIATELY! Do not make the user wait 10 seconds!
        if (track.audioUrl.isNotBlank()) {
            startPlayback(track.audioUrl)
        }

        // Background high-res stream resolution (YouTube Music InnerTube)
        if (track.videoId.isNotBlank()) {
            scope.launch {
                val resolved = withContext(Dispatchers.IO) {
                    YtMusicSource.resolveStream(track.videoId)
                }
                if (currentTrack?.id != track.id) return@launch
                if (resolved != null && resolved != track.audioUrl) {
                    val curPos = exoPlayer.currentPosition
                    val mediaItem = MediaItem.fromUri(resolved)
                    exoPlayer.setMediaItem(mediaItem, curPos)
                    exoPlayer.prepare()
                    exoPlayer.play()
                } else if (track.audioUrl.isBlank()) {
                    // Try instant iTunes audio fallback
                    val fallback = withContext(Dispatchers.IO) {
                        MusicRepository.findInstantAudio(track.title, track.artist)
                    }
                    if (currentTrack?.id == track.id && !fallback.isNullOrBlank()) {
                        startPlayback(fallback)
                    }
                }
            }
        } else if (track.audioUrl.isBlank()) {
            // Track has no audioUrl and no videoId -> fetch instant audio
            scope.launch {
                val fallback = withContext(Dispatchers.IO) {
                    MusicRepository.findInstantAudio(track.title, track.artist)
                }
                if (currentTrack?.id == track.id && !fallback.isNullOrBlank()) {
                    startPlayback(fallback)
                }
            }
        }
    }

    private fun startPlayback(url: String) {
        if (url.isBlank()) return
        try {
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            android.util.Log.e("VibraPlayer", "startPlayback failed: ${e.message}", e)
        }
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

    fun toggleShuffle() {
        isShuffleOn = !isShuffleOn
    }

    fun toggleRepeat() {
        isRepeatOn = !isRepeatOn
    }

    fun next() {
        if (queue.isEmpty()) return
        currentIndex = if (isShuffleOn && queue.size > 1) {
            (queue.indices - currentIndex).random()
        } else {
            (currentIndex + 1) % queue.size
        }
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
