package com.kazinehal.vibra.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.kazinehal.vibra.MainActivity

/**
 * Foreground media service that hosts the [MediaSession] for the shared [VibraPlayer].
 *
 * While a [androidx.media3.session.MediaController] (held by MainActivity) is bound, this
 * service exists; the moment playback starts, media3 promotes it to a mediaPlayback
 * foreground service and posts the MediaStyle notification (play/pause/next/prev + artwork)
 * — so music keeps playing with the screen off and controls stay in the lockscreen /
 * quick-settings media panel.
 *
 * The session wraps the singleton player's ExoPlayer. Per the media3 docs,
 * [MediaSession.release] does NOT release the underlying player, so onDestroy releases only
 * the session and lets the shared player outlive this service instance.
 */
class VibraMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = VibraPlayer.get(this)

        // Tapping the notification opens (or resumes) the app
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player.exoPlayer)
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        // Release ONLY the session — never the shared player (see class doc).
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
