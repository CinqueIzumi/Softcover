package nl.rhaydus.softcover.core.designsystem.presentation.layout

/**
 * The coarse width buckets the whole app adapts to. Screens branch on this rather than on raw
 * dp so the breakpoints live in exactly one place ([WindowSizeClass]).
 *
 * - [COMPACT] — phones in portrait and narrow windows (`< 600dp`). The canonical bottom-bar shell.
 * - [MEDIUM] — large phones in landscape, small tablets, medium desktop windows (`600–840dp`).
 *   The navigation rail replaces the bottom bar.
 * - [EXPANDED] — tablets, foldables unfolded, and desktop windows (`>= 840dp`). A permanent
 *   editorial sidebar replaces the rail and list surfaces gain a two-pane detail.
 */
enum class WindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}
