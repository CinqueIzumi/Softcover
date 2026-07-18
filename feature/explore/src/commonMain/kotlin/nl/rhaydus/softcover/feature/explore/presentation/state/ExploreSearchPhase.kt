package nl.rhaydus.softcover.feature.explore.presentation.state

/**
 * The four search-chrome states from the explore-3a spec's `frameHTML()` / `focusHTML()` /
 * `resultsHTML()`. Derived from [ExploreScreenUiState] rather than stored, so it can never drift
 * out of sync with the fields that actually drive it.
 */
internal enum class ExploreSearchPhase {
    /** Unfocused, empty query - the default feed (featured card, rails, mood grid, recents). */
    FEED,

    /** Focused, empty query - recent searches + "try a mood" chips replace the feed. */
    FOCUS,

    /** A text or mood search is in flight. */
    LOADING,

    /** A text or mood search has returned (or failed - see `searchError`). */
    RESULTS,
}
