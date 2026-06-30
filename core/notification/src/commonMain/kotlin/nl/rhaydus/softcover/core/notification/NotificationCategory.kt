package nl.rhaydus.softcover.core.notification

/**
 * Platform-neutral grouping of notifications by intent. The common contract speaks in logical
 * categories so callers never touch a platform delivery channel: Android maps each to a
 * [SoftcoverNotificationChannel], iOS to a `UNNotificationCategory`.
 */
enum class NotificationCategory {
    /** Deadlines, daily nudges, and release-day alerts for books you're following. */
    Reading,

    /** Celebrations when you finish a series, hit a streak, or complete a yearly goal. */
    Milestones,

    /** The live timer and controls for your current reading session. */
    Session,
}
