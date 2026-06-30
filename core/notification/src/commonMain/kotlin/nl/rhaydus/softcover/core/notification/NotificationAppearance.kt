package nl.rhaydus.softcover.core.notification

/**
 * App-supplied branding for system notifications: the status-bar [smallIcon] silhouette and the
 * [accentColor] used to tint the notification. Expressed as platform tokens so this module stays
 * free of any design-system dependency — each platform resolves the token to its own resource
 * handle (an Android `@DrawableRes`/`@ColorRes` id, the iOS app icon / accent).
 */
data class NotificationAppearance(
    val smallIcon: NotificationIcon,
    val accentColor: NotificationAccentColor,
)
