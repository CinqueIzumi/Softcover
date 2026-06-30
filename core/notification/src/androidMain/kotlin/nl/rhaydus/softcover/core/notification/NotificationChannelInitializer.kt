package nl.rhaydus.softcover.core.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

class NotificationChannelInitializer(private val context: Context) {
    fun initialize() {
        val manager = NotificationManagerCompat.from(context)

        val channels = SoftcoverNotificationChannel.entries.map { channel ->
            NotificationChannelCompat.Builder(
                channel.id,
                channel.importance,
            )
                .setName(channel.title)
                .setDescription(channel.description)
                .setShowBadge(true)
                .build()
        }

        manager.createNotificationChannelsCompat(channels)

        // Prune channels this app no longer lists — chiefly the legacy `softcover.session`, replaced
        // by `softcover.session.v2` so its immutable LOW importance can't pin the old behaviour. Runs
        // after the create so the device never sits without a valid session channel.
        manager.deleteUnlistedNotificationChannels(
            SoftcoverNotificationChannel.entries.map { it.id },
        )
    }
}
