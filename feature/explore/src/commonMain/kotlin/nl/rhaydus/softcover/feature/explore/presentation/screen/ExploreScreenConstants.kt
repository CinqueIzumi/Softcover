package nl.rhaydus.softcover.feature.explore.presentation.screen

internal const val TRENDING_SKELETON_COUNT = 4
internal const val CONTINUE_SERIES_SKELETON_COUNT = 4
internal const val BECAUSE_YOU_READ_SKELETON_COUNT = 4

// How many items from the end of the search-results list/grid trigger the next page (explore-3a
// feedback item 7) — shared between the mobile LazyColumn and the desktop LazyVerticalGrid so the
// two platforms feel the same distance from the edge before they fetch.
internal const val SEARCH_RESULTS_LOAD_MORE_THRESHOLD = 4

// Distinct shared-element surfaces so the same book showing up in more than one rail
// registers a unique key in the SharedTransitionScope.
internal const val SURFACE_TRENDING = "explore-trending"
internal const val SURFACE_UP_NEXT = "explore-up-next"
internal const val SURFACE_BECAUSE_YOU_READ = "explore-because-you-read"
internal const val SURFACE_FEATURED = "explore-featured"
