package nl.rhaydus.softcover.core.component.badge

/**
 * Every surface that renders a [Badge], one entry per surface (`component-contract.md` § 7.2 R2) —
 * not a boolean flag, so a third size cannot arrive as a second nullable beside a first. Tuning one
 * surface's pad, weight or icon through [BadgeDimensions] must never move another's.
 */
enum class BadgeVariant {
    /** Every badge except the ones below — deadline pace badges, and every non-hero release badge. */
    Standard,

    /** Explore's feed-opening "Arriving …" hero badge. Its own, larger pad. */
    FeaturedRelease,

    /** Library's grid-cover deadline countdown, pinned over the book's cover. */
    GridCountdown,

    /** Book detail's "Owned" mark, laid directly over an edition's cover. */
    OwnedCover,

    /** The edition selector's "TRACKING NOW" tag. */
    TrackingTag,
}
