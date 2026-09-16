package nl.rhaydus.softcover.core.component.badge

/**
 * The colour register a [Badge] speaks in. A tone is a *meaning*, not a palette entry — the
 * component resolves each one onto the theme's container roles, so a caller cannot hand a badge a
 * colour that does not belong to the design system.
 *
 * [Badge] resolves each entry onto exactly today's colour pairs: [OnTrack] to
 * `primaryContainer`/`onPrimaryContainer`, [Behind] to `errorContainer`/`onErrorContainer`,
 * [Expired] to `surfaceVariant`/`onSurfaceVariant`, and [Release] to `primary`/`onPrimary`.
 */
enum class BadgeTone {
    /** On pace to make the deadline. */
    OnTrack,

    /** Behind pace — the reader needs to pick up speed to still make the deadline. */
    Behind,

    /** The deadline has passed. */
    Expired,

    /** An edition's release date, past or upcoming. */
    Release,
}
