package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * Derives the tag-suggestion list for the editor: entries from the cached [vocabulary] restricted to
 * [category], minus any tag already in [appliedTags] for that category (case-insensitive name match,
 * mirroring the dedupe in [nl.rhaydus.softcover.feature.book_detail.presentation.action.OnAddUserTagAction]).
 * With a blank [input] the list ranks by the user's personal usage frequency ([UserTag.count] desc,
 * then name asc). Once the user types, candidates are filtered to substring matches, with
 * prefix matches ranked ahead of mid-string ones, each tier ordered by frequency then name.
 */
internal fun computeTagSuggestions(
    vocabulary: List<UserTag>,
    input: String,
    category: TagCategory,
    appliedTags: List<UserTag>,
    limit: Int = 8,
): List<UserTag> {
    val appliedNames = appliedTags
        .filter { it.category == category }
        .mapTo(mutableSetOf()) { it.name.lowercase() }

    val candidates = vocabulary.filter { candidate ->
        candidate.category == category && candidate.name.lowercase() !in appliedNames
    }

    val query = input.trim()

    if (query.isEmpty()) {
        return candidates
            .sortedWith(compareByDescending<UserTag> { it.count }.thenBy { it.name })
            .take(limit)
    }

    return candidates
        .filter { it.name.contains(
            query,
            ignoreCase = true,
        ) }
        .sortedWith(
            compareByDescending<UserTag> { it.name.startsWith(
                query,
                ignoreCase = true,
            ) }
                .thenByDescending { it.count }
                .thenBy { it.name },
        )
        .take(limit)
}
