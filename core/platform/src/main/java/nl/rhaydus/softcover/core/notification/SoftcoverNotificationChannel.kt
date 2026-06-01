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
    Session(
        id = "softcover.session",
        title = "Reading session",
        description = "The live timer and controls for your current reading session.",
        importance = NotificationManagerCompat.IMPORTANCE_LOW,
    ),
}
