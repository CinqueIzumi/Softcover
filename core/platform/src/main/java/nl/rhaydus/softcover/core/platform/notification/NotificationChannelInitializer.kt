package nl.rhaydus.softcover.core.platform.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

class NotificationChannelInitializer(private val context: Context) {

    fun initialize() {
        val manager = NotificationManagerCompat.from(context)

        val channels = SoftcoverNotificationChannel.entries.map { channel ->
            NotificationChannelCompat.Builder(channel.id, channel.importance)
                .setName(channel.title)
                .setDescription(channel.description)
                .setShowBadge(true)
                .build()
        }

        manager.createNotificationChannelsCompat(channels)
    }
}
