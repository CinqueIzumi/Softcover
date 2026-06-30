package nl.rhaydus.softcover.core.notification

interface SoftcoverNotifier {
    /** True when the runtime permission to post notifications is granted (always true on Android < 33). */
    fun hasPostPermission(): Boolean

    fun notify(
        id: Int,
        content: SoftcoverNotificationContent,
    )

    fun cancel(id: Int)
}
