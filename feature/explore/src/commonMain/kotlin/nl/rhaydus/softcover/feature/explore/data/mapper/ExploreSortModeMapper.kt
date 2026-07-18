package nl.rhaydus.softcover.feature.explore.data.mapper

import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode

/**
 * Maps to the two Typesense sort orderings proven to work against the live `search` index
 * (see explore-3a brief, deviation 4). No other ordering should ever be threaded through here.
 */
internal fun ExploreSortMode.toTypesenseSort(): String = when (this) {
    ExploreSortMode.RELEVANCE -> "_text_match:desc,users_count:desc"
    ExploreSortMode.POPULARITY -> "users_count:desc"
}
