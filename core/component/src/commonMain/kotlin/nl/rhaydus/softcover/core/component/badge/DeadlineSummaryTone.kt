package nl.rhaydus.softcover.core.component.badge

/**
 * The ink [DeadlineSummaryLine] paints in — a *meaning* (which surface the line sits on), not a flat
 * `Color`. The original `DeadlineSummaryLine(foreground: Color? = null)` let a caller hand the
 * component any colour; putting a `foreground: Color?` straight on the model would satisfy R2's
 * letter ("no flat nullable beside the model") no better than the parameter did, because the
 * *default* ink — `onSurfaceVariant` — is a theme lookup that only resolves inside composition
 * (`MaterialTheme.colorScheme.onSurfaceVariant`), so it cannot be baked into a `Color` field on a
 * model a collector assembles off the composition (§ 7.2 R10). A tone is the fix on both counts: the
 * component resolves it in composition, and the two things a caller can mean — "the ordinary ink" or
 * "the Reading hero backdrop's fixed ink" — are enumerable rather than an open colour space.
 */
enum class DeadlineSummaryTone {
    /** The line sits on an ordinary surface — `onSurfaceVariant`. */
    OnSurface,

    /**
     * The line sits on the Reading screen's featured-hero card's blurred-cover backdrop, so it needs
     * a colour that reads reliably against an arbitrary cover image regardless of theme — the fixed
     * `ReadingHeroBackdropForeground` token.
     */
    OnHeroBackdrop,
}
