package com.kazinehal.vibra.player

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.kazinehal.vibra.data.UserLibraryManager
import com.kazinehal.vibra.data.YtMusicSource
import com.kazinehal.vibra.data.YtStreamResolver
import com.kazinehal.vibra.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VibraPlayer private constructor(context: Context) {

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .setConnectTimeoutMs(8000)
        .setReadTimeoutMs(10000)
        .setAllowCrossProtocolRedirects(true)

    private val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    internal val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val scope = CoroutineScope(Dispatchers.Main)

    var currentTrack by mutableStateOf<Track?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var currentPositionMs by mutableLongStateOf(0L)
        private set

    var durationMs by mutableLongStateOf(1L)
        private set

    var isShuffleOn by mutableStateOf(false)
    var isRepeatOn by mutableStateOf(false)

    var queue = mutableListOf<Track>()
    private var currentIndex = 0

    /** Remaining minutes of the sleep timer; 0 = off. */
    var sleepTimerMinutes by mutableStateOf(0)
        private set

    private var sleepJob: Job? = null
    private var watchdogJob: Job? = null

    var shouldPlayWhenReady by mutableStateOf(true)
        private set

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldPlayWhenReady = playWhenReady
                if (!playWhenReady) {
                    isPlaying = false
                    isLoading = false
                    stopBufferingWatchdog()
                } else if (exoPlayer.playbackState == Player.STATE_READY) {
                    isPlaying = true
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    isLoading = false
                    stopBufferingWatchdog()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isLoading = true
                        startBufferingWatchdog()
                    }
                    Player.STATE_READY -> {
                        isLoading = false
                        stopBufferingWatchdog()
                        val dur = exoPlayer.duration
                        if (dur > 1000L) {
                            durationMs = dur
                        }
                    }
                    Player.STATE_ENDED -> {
                        isLoading = false
                        stopBufferingWatchdog()
                        if (isRepeatOn) {
                            seekTo(0)
                            exoPlayer.play()
                        } else {
                            next()
                        }
                    }
                    Player.STATE_IDLE -> {
                        stopBufferingWatchdog()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("VibraPlayer", "ExoPlayer error: ${error.message}", error)
                isLoading = false
                stopBufferingWatchdog()

                // Auto-skip to next on error — don't silently hang
                scope.launch {
                    delay(500)
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

    /** Start an 8-second timeout watchdog. If still buffering after 8s, skip to next. */
    private fun startBufferingWatchdog() {
        stopBufferingWatchdog()
        watchdogJob = scope.launch {
            delay(8000)
            if (isLoading && !isPlaying) {
                android.util.Log.w("VibraPlayer", "Watchdog: buffering timeout — skipping to next")
                next()
            }
        }
    }

    private fun stopBufferingWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = null
    }

    /**
     * Play a track. Syncs currentIndex in queue so next()/previous() always work correctly.
     */
    fun playTrack(track: Track) {
        currentTrack = track
        durationMs = if (track.duration > 0) track.duration * 1000L else 1L
        isLoading = true
        shouldPlayWhenReady = true

        // Sync currentIndex so next() / previous() navigate from the right position
        val foundIdx = queue.indexOfFirst { it.id == track.id }
        if (foundIdx >= 0) {
            currentIndex = foundIdx
        }

        // Add to persistent user history
        UserLibraryManager.addToHistory(track)

        scope.launch {
            try {
                // If track already has a valid direct audio URL, play it immediately
                if (track.audioUrl.isNotBlank()
                    && !track.audioUrl.contains("itunes")
                    && !track.audioUrl.contains("mzstatic")
                ) {
                    startPlayback(track.audioUrl)
                    return@launch
                }

                // Resolve videoId if empty
                var videoId = track.videoId
                if (videoId.isBlank()) {
                    val foundVid = withContext(Dispatchers.IO) {
                        YtMusicSource.findVideoId(track.title, track.artist)
                    }
                    videoId = foundVid ?: ""
                }

                // Resolve YouTube stream URL
                if (videoId.isNotBlank()) {
                    val stream = withContext(Dispatchers.IO) {
                        YtStreamResolver.resolve(videoId)
                    }
                    if (currentTrack?.id == track.id && stream != null && stream.audioUrl.isNotBlank()) {
                        if (stream.durationSeconds > 0) {
                            durationMs = stream.durationSeconds * 1000L
                        }
                        startPlayback(stream.audioUrl)
                        return@launch
                    }
                }

                // Fallback: try cached audioUrl
                if (track.audioUrl.isNotBlank()) {
                    startPlayback(track.audioUrl)
                    return@launch
                }

                // No stream found — skip to next so the queue keeps moving
                android.util.Log.w("VibraPlayer", "No stream found for ${track.title} — skipping")
                delay(300)
                next()

            } catch (e: Exception) {
                android.util.Log.e("VibraPlayer", "playTrack failed: ${e.message}", e)
                delay(300)
                next()
            } finally {
                isLoading = false
            }
        }
    }

    private fun startPlayback(url: String) {
        if (url.isBlank()) return
        try {
            val t = currentTrack
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t?.title ?: "Riva")
                        .setArtist(t?.artist ?: "")
                        .setAlbumTitle(t?.album?.takeIf { it.isNotBlank() })
                        .setArtworkUri(t?.artUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
                        .build()
                )
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (shouldPlayWhenReady) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
                isPlaying = true
            } else {
                exoPlayer.playWhenReady = false
                exoPlayer.pause()
                isPlaying = false
            }
        } catch (e: Exception) {
            android.util.Log.e("VibraPlayer", "startPlayback failed: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        if (isPlaying || (exoPlayer.playWhenReady && exoPlayer.playbackState != Player.STATE_ENDED)) {
            pause()
        } else {
            play()
        }
    }

    fun pause() {
        shouldPlayWhenReady = false
        exoPlayer.playWhenReady = false
        exoPlayer.pause()
        isPlaying = false
        isLoading = false
        stopBufferingWatchdog()
    }

    fun play() {
        shouldPlayWhenReady = true
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        isPlaying = true
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

    /** Arm (minutes > 0) or cancel (0) the sleep timer — pauses playback when it fires. */
    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        sleepJob = null
        sleepTimerMinutes = minutes.coerceAtLeast(0)
        if (sleepTimerMinutes == 0) return
        sleepJob = scope.launch {
            delay(sleepTimerMinutes * 60_000L)
            exoPlayer.pause()
            sleepTimerMinutes = 0
        }
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

    companion object {
        @Volatile
        private var instance: VibraPlayer? = null

        fun get(context: Context): VibraPlayer {
            return instance ?: synchronized(this) {
                instance ?: VibraPlayer(context.applicationContext).also { instance = it }
            }
        }
    }
}
