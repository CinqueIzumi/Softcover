package nl.rhaydus.softcover.core.notification

/**
 * iOS handle for the status-bar notification icon. iOS always uses the app icon for notifications,
 * so there is no per-notification icon to carry — the token exists only to satisfy the common
 * [NotificationAppearance] contract.
 */
actual class NotificationIcon
