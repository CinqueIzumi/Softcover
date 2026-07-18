package nl.rhaydus.softcover.feature.explore.domain.model

/** The two Typesense sort orderings proven to work against the live `search` index. */
enum class ExploreSortMode {
    RELEVANCE,
    POPULARITY,
}
