package nl.rhaydus.softcover.core.notification

data class SoftcoverNotificationContent(
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val eyebrow: String? = null,
)
