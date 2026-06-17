package nl.rhaydus.softcover.core.notification

import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import java.awt.image.BufferedImage
import nl.rhaydus.softcover.core.domain.logging.AppLog

/**
 * Desktop notifier over AWT's [SystemTray]: posts native balloon notifications via
 * [TrayIcon.displayMessage]. A single tray icon is installed lazily on first use and reused for every
 * notification.
 *
 * Two desktop limitations are reflected here: AWT exposes no per-notification dismissal, so [cancel]
 * is a best-effort no-op (the `id` is used only for logging); and a headless / tray-less session has
 * no surface to post to, so the notifier degrades to a logged no-op there.
 */
internal class JvmSoftcoverNotifier : SoftcoverNotifier {
    private val trayIcon: TrayIcon? by lazy { installTrayIcon() }

    override fun hasPostPermission(): Boolean = SystemTray.isSupported()

    override fun notify(
        id: Int,
        content: SoftcoverNotificationContent,
    ) {
        val icon = trayIcon ?: run {
            AppLog.i("System tray unavailable; dropping notification id=$id.")

            return
        }

        val caption = content.eyebrow?.let { "$it — ${content.title}" } ?: content.title

        icon.displayMessage(
            caption,
            content.body,
            content.category.toMessageType(),
        )
    }

    // AWT balloon notifications cannot be dismissed programmatically; nothing to cancel.
    override fun cancel(id: Int) = Unit

    private fun installTrayIcon(): TrayIcon? {
        if (SystemTray.isSupported().not()) return null

        return runCatching {
            // A transparent image is enough — the icon exists only to host notifications.
            val image = BufferedImage(
                TRAY_ICON_SIZE,
                TRAY_ICON_SIZE,
                BufferedImage.TYPE_INT_ARGB,
            )
            val icon = TrayIcon(
                image,
                "Softcover",
            ).apply { isImageAutoSize = true }

            SystemTray.getSystemTray().add(icon)

            icon
        }.getOrElse { error ->
            AppLog.e(
                error,
                "Failed to install the system tray icon",
            )

            null
        }
    }

    private fun NotificationCategory.toMessageType(): MessageType = when (this) {
        NotificationCategory.Reading, NotificationCategory.Milestones -> MessageType.INFO
        NotificationCategory.Session -> MessageType.NONE
    }

    private companion object {
        const val TRAY_ICON_SIZE = 16
    }
}
