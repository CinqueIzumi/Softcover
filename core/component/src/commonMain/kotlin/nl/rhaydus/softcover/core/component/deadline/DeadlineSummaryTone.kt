package nl.rhaydus.softcover.core.component.deadline

/**
 * The ink [DeadlineSummaryLine] paints in, named for the surface the line sits on.
 */
enum class DeadlineSummaryTone {
    /** An ordinary surface: `onSurfaceVariant`. */
    OnSurface,

    /** The Reading featured-hero card's blurred-cover backdrop: the fixed `ReadingHeroBackdropForeground`. */
    OnHeroBackdrop,
}
