package nl.rhaydus.softcover.feature.session.presentation.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.component.resolveEditionImageSource
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.core.platform.notification.SoftcoverNotificationChannel
import java.time.Duration

/**
 * Foreground service that surfaces the active reading session as a persistent, ongoing notification:
 * the book's edition cover (large, via [NotificationCompat.BigPictureStyle]), the running timer, and
 * pause / resume / stop plus an inline "update page" reply that lets the page be changed without
 * opening the app. It is a plain notification — deliberately NOT a media/`MediaSession` notification —
 * so it never competes with a real audio app (Spotify etc.) for the media slot, never shows a speaker
 * chip, and never touches media buttons. Being a foreground service makes it non-dismissable; it is
 * removed only when the session ends (the [controller]'s active session goes null).
 */
class ReadingSessionService : Service() {

    private val controller: ActiveSessionController by inject()

    private val appEntryPoint: AppEntryPoint by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var collecting = false

    private var coverBitmap: Bitmap? = null

    private var coverSource: Any? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> controller.pause()

            ACTION_RESUME -> controller.resume()

            ACTION_STOP -> controller.stop()

            ACTION_UPDATE_PAGE -> handleUpdatePage(intent = intent)
        }

        // ACTION_RESHOW (the notification's deleteIntent) re-posts after a swipe: Android 14+ ignores
        // setOngoing for FGS notifications, so the only way to keep it persistent is to bring it back.
        // If the session has ended, the collector below removes it instead.
        startForeground(NOTIFICATION_ID, buildNotification(active = controller.activeSession.value))

        startCollecting()

        return START_STICKY
    }

    private fun startCollecting() {
        if (collecting) return

        collecting = true

        controller.activeSession
            .map { active -> active to (active?.session?.id to active?.session?.isPaused) }
            .distinctUntilChanged()
            .map { it.first }
            .onEach { active ->
                runCatching {
                    if (active == null) {
                        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)

                        stopSelf()

                        return@onEach
                    }

                    ensureCover(active = active)

                    startForeground(NOTIFICATION_ID, buildNotification(active = active))
                }.onFailure { error ->
                    Timber.e("$error")
                }
            }
            .launchIn(serviceScope)
    }

    private fun ensureCover(active: ActiveSession) {
        val source = resolveEditionImageSource(
            edition = active.book.currentEdition,
            defaultEdition = active.book.defaultEdition,
            fallbackCoverUrl = active.book.coverUrl.takeIf { it.isNotBlank() },
        )

        if (source == coverSource && coverBitmap != null) return

        coverSource = source

        if (source == null) {
            coverBitmap = null

            return
        }

        serviceScope.launch {
            val request = ImageRequest.Builder(this@ReadingSessionService)
                .data(source)
                .allowHardware(false)
                .size(COVER_SIZE_PX)
                .build()

            val bitmap = (applicationContext.imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
                ?: return@launch

            coverBitmap = bitmap

            controller.activeSession.value?.let { current ->
                startForeground(NOTIFICATION_ID, buildNotification(active = current))
            }
        }
    }

    private fun handleUpdatePage(intent: Intent) {
        val input = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(KEY_PAGE_INPUT)
            ?.toString()
            ?.trim()
            ?: return

        val newPage = input.toIntOrNull() ?: return

        controller.updatePage(newPage = newPage)
    }

    private fun buildNotification(active: ActiveSession?): Notification {
        val title = active?.book?.title ?: getString(R.string.session_notification_default_title)
        val author = active?.book?.authors?.firstOrNull()?.name.orEmpty()
        val isPaused = active?.session?.isPaused == true
        val elapsed = active?.session?.readingDuration() ?: Duration.ZERO

        val builder = NotificationCompat.Builder(this, SoftcoverNotificationChannel.Session.id)
            .setSmallIcon(R.drawable.ic_reading)
            .setContentTitle(title)
            .setContentText(if (isPaused) pausedText(elapsed = elapsed) else author)
            .setColor(ContextCompat.getColor(this, R.color.notification_accent))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(focusModePendingIntent())
            .setDeleteIntent(servicePendingIntent(action = ACTION_RESHOW))
            .setLargeIcon(coverBitmap)

        active?.progressSubtitle()?.let { builder.setSubText(it) }

        if (isPaused) {
            builder.setShowWhen(false)

            builder.setUsesChronometer(false)
        } else {
            builder.setShowWhen(true)

            builder.setUsesChronometer(true)

            builder.setWhen(System.currentTimeMillis() - elapsed.toMillis())
        }

        if (isPaused) {
            builder.addAction(
                R.drawable.ic_play,
                getString(R.string.session_action_resume),
                servicePendingIntent(action = ACTION_RESUME),
            )
        } else {
            builder.addAction(
                R.drawable.ic_pause,
                getString(R.string.session_action_pause),
                servicePendingIntent(action = ACTION_PAUSE),
            )
        }

        builder.addAction(
            R.drawable.ic_stop,
            getString(R.string.session_action_stop),
            servicePendingIntent(action = ACTION_STOP),
        )

        builder.addAction(updatePageAction())

        coverBitmap?.let { cover ->
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(cover)
                    .bigLargeIcon(null as Bitmap?),
            )
        }

        return builder.build()
    }

    private fun updatePageAction(): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(KEY_PAGE_INPUT)
            .setLabel(getString(R.string.session_action_update_page))
            .build()

        return NotificationCompat.Action.Builder(
            R.drawable.ic_edit,
            getString(R.string.session_action_update),
            servicePendingIntent(action = ACTION_UPDATE_PAGE, mutable = true),
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(false)
            .build()
    }

    private fun servicePendingIntent(action: String, mutable: Boolean = false): PendingIntent {
        val intent = Intent(this, ReadingSessionService::class.java).setAction(action)

        val flags = if (mutable) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getService(this, action.hashCode(), intent, flags)
    }

    private fun focusModePendingIntent(): PendingIntent {
        val intent = appEntryPoint.focusModeIntent(context = this)

        return PendingIntent.getActivity(
            this,
            REQUEST_FOCUS_MODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun pausedText(elapsed: Duration): String =
        getString(R.string.session_notification_paused, formatSessionElapsed(elapsed = elapsed))

    override fun onDestroy() {
        serviceScope.cancel()

        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 4_201
        private const val REQUEST_FOCUS_MODE = 4_202
        private const val COVER_SIZE_PX = 1_024

        const val KEY_PAGE_INPUT = "nl.rhaydus.softcover.session.PAGE_INPUT"

        private const val ACTION_PAUSE = "nl.rhaydus.softcover.session.PAUSE"
        private const val ACTION_RESUME = "nl.rhaydus.softcover.session.RESUME"
        private const val ACTION_STOP = "nl.rhaydus.softcover.session.STOP"
        private const val ACTION_UPDATE_PAGE = "nl.rhaydus.softcover.session.UPDATE_PAGE"
        private const val ACTION_RESHOW = "nl.rhaydus.softcover.session.RESHOW"

        fun start(context: Context) {
            val intent = Intent(context, ReadingSessionService::class.java)

            ContextCompat.startForegroundService(context, intent)
        }
    }
}

private fun ActiveSession.progressSubtitle(): String? {
    val edition = book.currentEdition

    if (edition?.isAudiobook == true) return null

    val currentPage = book.userBookRead?.currentPage ?: return null
    val percent = book.userBookRead?.progress?.let { "${it.toInt()}%" }

    return if (percent != null) "Page $currentPage · $percent" else "Page $currentPage"
}
