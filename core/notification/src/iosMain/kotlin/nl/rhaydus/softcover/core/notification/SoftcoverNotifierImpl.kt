package nl.rhaydus.softcover.core.notification

import kotlin.concurrent.Volatile
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import nl.rhaydus.common.AppLog

/**
 * iOS notifier over `UNUserNotificationCenter`. Notifications post immediately (no trigger); the
 * logical [NotificationCategory] is carried as the request's `categoryIdentifier` so the system can
 * map it to a `UNNotificationCategory`.
 *
 * `UNUserNotificationCenter` reports authorization asynchronously, but [hasPostPermission] is
 * synchronous, so the granted state is cached and refreshed (on the main queue) at construction and
 * after each authorization request. The cache is therefore eventually consistent: a brand-new grant
 * may not be reflected until the next refresh.
 */
internal class SoftcoverNotifierImpl : SoftcoverNotifier {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    // Written on the main queue after the async settings query (see refreshAuthorization); @Volatile so a
    // synchronous hasPostPermission() read from any dispatcher sees the latest value.
    @Volatile
    private var cachedGranted = false

    init {
        refreshAuthorization()
    }

    override fun hasPostPermission(): Boolean = cachedGranted

    // Unlike the Android notifier, this does not pre-guard on hasPostPermission(): iOS silently drops an
    // unauthorized notification at the system level, so gating on the eventually-consistent cache would
    // only risk suppressing a legitimate notification in the startup / just-granted window.
    override fun notify(
        id: Int,
        content: SoftcoverNotificationContent,
    ) {
        val notification = UNMutableNotificationContent().apply {
            setTitle(content.title)
            setBody(content.body)
            content.eyebrow?.let { setSubtitle(it) }
            setCategoryIdentifier(content.category.name)
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id.toString(),
            content = notification,
            trigger = null,
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                AppLog.w("Failed to post notification id=$id: ${error.localizedDescription}")
            }
        }
    }

    override fun cancel(id: Int) {
        val identifiers = listOf(id.toString())

        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    private fun refreshAuthorization() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val granted = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral,
                -> true

                else -> false
            }

            dispatch_async(dispatch_get_main_queue()) {
                cachedGranted = granted
            }
        }
    }
}
