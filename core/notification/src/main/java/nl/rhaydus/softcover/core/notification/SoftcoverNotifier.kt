package nl.rhaydus.softcover.core.notification

interface SoftcoverNotifier {
    /** True when the runtime POST_NOTIFICATIONS permission is granted (always true on API < 33). */
    fun hasPostPermission(): Boolean

    fun notify(
        id: Int,
        content: SoftcoverNotificationContent,
    )

    fun cancel(id: Int)
}
