package nl.rhaydus.softcover.core.component.badge

/**
 * Every surface [CoverOverlay] pins a badge onto, one entry per corner/pad pair
 * (`component-contract.md` § 7.2 R2) — see [CoverOverlayDimensions.forVariant].
 */
enum class CoverOverlayVariant {
    /** A reading deadline's status badge, pinned top-end — today's behaviour. */
    ReadingStatus,

    /** Library's grid-cover deadline countdown, pinned top-start. */
    LibraryCountdown,
}
