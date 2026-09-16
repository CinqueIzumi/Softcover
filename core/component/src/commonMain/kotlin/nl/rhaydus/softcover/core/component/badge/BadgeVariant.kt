package nl.rhaydus.softcover.core.component.badge

/**
 * Every surface that renders a [Badge], one entry per surface (`component-contract.md` § 7.2 R2) —
 * not a boolean flag, so a third size cannot arrive as a second nullable beside a first.
 *
 * The two entries exist because Explore's unreleased featured hero badge (the feed-opening
 * `FeaturedCard`'s inline "Arriving …" chip) has always used a larger pad than every other badge in
 * the app: it built its own `Surface` + `Text` at 8.dp/4.dp rather than calling the shared badge,
 * against every other badge's 6.dp/2.dp, and nobody had ever named the difference. Recording it as a
 * variant — rather than quietly averaging the two, or letting [Badge] take a loose padding parameter
 * — is this migration's audit (§ 5k precedent): the taxonomy the port produces is itself the record
 * of what was drifting, and tuning one surface's pad through [BadgeDimensions] must never move the
 * other's.
 */
enum class BadgeVariant {
    /** Every badge except the one below — deadline pace badges, and every non-hero release badge. */
    Standard,

    /** Explore's feed-opening "Arriving …" hero badge. Its own, larger pad — see the class doc. */
    FeaturedRelease,
}
