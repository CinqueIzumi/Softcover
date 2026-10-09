package nl.rhaydus.softcover.core.component.badge

/**
 * The colour register a [Badge] speaks in — a pure colour axis, not a status. What a tone *means*
 * (on-track, behind, owned, tracking) is a mapper's decision; [Badge] resolves each entry onto
 * the theme.
 */
enum class BadgeTone {
    Primary,
    PrimaryContainer,
    ErrorContainer,
    TertiaryContainer,
    Neutral,
    Scrim,
}
