package nl.rhaydus.softcover.core.component.callout

/**
 * The colour register a [Banner] speaks in. A tone is a *meaning*, not a palette entry — the
 * component resolves each one onto the theme's container roles, so a caller cannot hand a banner a
 * colour that does not belong to the design system.
 */
enum class BannerTone {
    /** Something the reader should know about but need not act on — the offline notice. */
    WARNING,
}
