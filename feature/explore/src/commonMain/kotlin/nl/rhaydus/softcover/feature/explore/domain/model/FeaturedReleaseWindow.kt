package nl.rhaydus.softcover.feature.explore.domain.model

/**
 * How far ahead the Explore hero looks when picking a release to feature: the most-shelved book with
 * a release date inside this window.
 *
 * A domain value rather than a data-layer detail, because two layers state it — the query that
 * bounds the fetch, and the card's own eyebrow, which names the window to the reader. Changing the
 * window here changes both, so the copy can never drift from the rule it describes.
 */
internal const val FEATURED_RELEASE_WINDOW_DAYS = 30
