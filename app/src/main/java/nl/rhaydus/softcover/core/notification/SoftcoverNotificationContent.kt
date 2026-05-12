package nl.rhaydus.softcover.core.notification

import android.app.PendingIntent

data class SoftcoverNotificationContent(
    val channel: SoftcoverNotificationChannel,
    val title: String,
    val body: String,
    val eyebrow: String? = null,
    val pendingIntent: PendingIntent? = null,
)
