package nl.rhaydus.softcover.core.notification

import androidx.core.app.NotificationManagerCompat

enum class SoftcoverNotificationChannel(
    val id: String,
    val title: String,
    val description: String,
    val importance: Int,
) {
    Reading(
        id = "softcover.reading",
        title = "Reading reminders",
        description = "Deadlines, daily nudges, and release-day alerts for books you're following.",
        importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
    ),
    Milestones(
        id = "softcover.milestones",
        title = "Milestones",
        description = "Celebrations when you finish a series, hit a streak, or complete a yearly goal.",
        importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
    ),
    // DEFAULT, not LOW: One UI only surfaces *alerting* notifications on the lock screen, and a LOW
    // channel never alerts. The session notification pairs this with setOnlyAlertOnce in the builder,
    // so it chimes once when a session starts and stays quiet on every timer update. The id carries a
    // suffix because importance is immutable once a channel exists, so existing installs need a fresh
    // channel; the stale `softcover.session` is pruned in NotificationChannelInitializer.
    Session(
        id = "softcover.session.v2",
        title = "Reading session",
        description = "The live timer and controls for your current reading session.",
        importance = NotificationManagerCompat.IMPORTANCE_DEFAULT,
    ),
    ;

    companion object {
        /** The delivery channel for a logical [NotificationCategory]. */
        fun forCategory(category: NotificationCategory): SoftcoverNotificationChannel =
            when (category) {
                NotificationCategory.Reading -> Reading
                NotificationCategory.Milestones -> Milestones
                NotificationCategory.Session -> Session
            }
    }
}
