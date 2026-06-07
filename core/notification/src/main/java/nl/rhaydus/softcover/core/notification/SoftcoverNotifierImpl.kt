package nl.rhaydus.softcover.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import nl.rhaydus.softcover.core.domain.logging.AppLog

internal class SoftcoverNotifierImpl(
    private val context: Context,
    @param:DrawableRes private val smallIcon: Int,
    @param:ColorRes private val accentColor: Int,
) : SoftcoverNotifier {
    override fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        )

        return granted == PackageManager.PERMISSION_GRANTED
    }

    // The notify() call is guarded by the hasPostPermission() early-return below; lint can't trace
    // that custom check, so the runtime-permission warning is suppressed here rather than at the call.
    @SuppressLint("MissingPermission")
    override fun notify(
        id: Int,
        content: SoftcoverNotificationContent,
    ) {
        if (hasPostPermission().not()) {
            AppLog.w("Skipping notify(id=$id) — POST_NOTIFICATIONS not granted")

            return
        }

        val builder = NotificationCompat.Builder(
            context,
            content.channel.id,
        )
            .setSmallIcon(smallIcon)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(
                context,
                accentColor,
            ),)
            .setColorized(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (content.eyebrow != null) {
            builder.setSubText(content.eyebrow)
        }

        if (content.pendingIntent != null) {
            builder.setContentIntent(content.pendingIntent)
        }

        NotificationManagerCompat.from(context).notify(
            id,
            builder.build(),
        )
    }

    override fun cancel(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }
}
