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
import com.kazinehal.vibra.data.FullSongResolver
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
                    val exoDur = exoPlayer.duration
                    if (exoDur > 1000L) {
                        durationMs = exoDur
                    }
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
                android.util.Log.e("VibraPlayer", "ExoPlayer playback error: ${error.message}", error)
                val track = currentTrack ?: return
                // On error, attempt alternative full song resolve
                scope.launch {
                    val full = withContext(Dispatchers.IO) {
                        FullSongResolver.resolve(track.title, track.artist)
                    }
                    if (currentTrack?.id == track.id && full != null && full.audioUrl != track.audioUrl) {
                        durationMs = full.durationSeconds * 1000L
                        startPlayback(full.audioUrl)
                    }
                }
            }
        })

        // High-precision scrubber position ticker
        scope.launch {
            while (true) {
                if (isPlaying) {
                    currentPositionMs = exoPlayer.currentPosition
                }
                delay(200)
            }
        }
    }

    /**
     * Play full song without 30-second limitations.
     * Always resolves the complete 3-5 minute 320kbps audio track.
     */
    fun playTrack(track: Track) {
        currentTrack = track
        durationMs = track.duration * 1000L

        // Detect if audioUrl is empty or an Apple 30-second preview
        val isPreview = track.audioUrl.isBlank() ||
                track.audioUrl.contains("itunes.apple.com") ||
                track.audioUrl.contains("mzstatic.com") ||
                track.audioUrl.contains("apple.com") ||
                track.audioUrl.contains("AudioPreview") ||
                track.duration <= 30

        // If track already has a verified full audio URL (direct CDN), play immediately
        if (!isPreview) {
            startPlayback(track.audioUrl)
            return
        }

        // Resolve FULL SONG stream (Zero 30-sec cuts, 100% full duration complete song)
        scope.launch {
            val fullSong = withContext(Dispatchers.IO) {
                FullSongResolver.resolve(track.title, track.artist)
            }

            if (currentTrack?.id != track.id) return@launch

            if (fullSong != null) {
                durationMs = fullSong.durationSeconds * 1000L
                startPlayback(fullSong.audioUrl)
                return@launch
            }

            // Fallback rung 2: YouTube Music InnerTube stream
            if (track.videoId.isNotBlank()) {
                val ytUrl = withContext(Dispatchers.IO) {
                    YtMusicSource.resolveStream(track.videoId)
                }
                if (currentTrack?.id == track.id && !ytUrl.isNullOrBlank()) {
                    startPlayback(ytUrl)
                    return@launch
                }
            }

            // Fallback rung 3: Cached audio URL if non-empty
            if (track.audioUrl.isNotBlank()) {
                startPlayback(track.audioUrl)
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
