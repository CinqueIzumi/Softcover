package nl.rhaydus.softcover.core.component.badge

/**
 * The colour register a [Badge] speaks in — a pure colour axis, not a status. What a tone *means*
 * (on-track, behind, owned, tracking) is a mapper's decision; [Badge] only resolves each entry onto
 * the theme's container roles: [Primary] to `primary`/`onPrimary`, [PrimaryContainer] to
 * `primaryContainer`/`onPrimaryContainer`, [ErrorContainer] to `errorContainer`/`onErrorContainer`,
 * [Muted] to `surfaceVariant`/`onSurfaceVariant`, [Neutral] to
 * `surfaceContainerHigh`/`onSurfaceVariant`, and [Scrim] to a translucent black over white.
 */
enum class BadgeTone {
    /** An edition's release date, past or upcoming. */
    Primary,

    /** On pace to make a deadline. */
    PrimaryContainer,

    /** Behind pace, or another state that needs attention. */
    ErrorContainer,

    /** A deadline that has expired, or another inert/dimmed state. */
    Muted,

    /** A quiet, chrome-toned label such as an edition's tracking tag. */
    Neutral,

    /** A badge laid directly over imagery, such as a cover's "Owned" mark. */
    Scrim,
}
