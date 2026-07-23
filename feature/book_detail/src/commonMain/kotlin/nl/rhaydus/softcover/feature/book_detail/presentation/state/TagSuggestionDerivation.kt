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
 *
 * The result is **not** truncated: it carries every candidate in the category, ranked, because the
 * editor's suggestion cloud lets the user reveal the whole set. How much of it is on screen at rest
 * is a fold the layout owns (`ExpandableFlowRow`'s collapsed lines), not a cap the derivation
 * applies — a limit here would put a ceiling on the reveal that no affordance could lift.
 */
internal fun computeTagSuggestions(
    vocabulary: List<UserTag>,
    input: String,
    category: TagCategory,
    appliedTags: List<UserTag>,
): List<UserTag> {
    val appliedNames = appliedTags
        .filter { it.category == category }
        .mapTo(mutableSetOf()) { it.name.lowercase() }

    val candidates = vocabulary.filter { candidate ->
        candidate.category == category && candidate.name.lowercase() !in appliedNames
    }

    val query = input.trim()

    if (query.isEmpty()) {
        return candidates.sortedWith(compareByDescending<UserTag> { it.count }.thenBy { it.name })
    }

    return candidates
        .filter {
            it.name.contains(
                query,
                ignoreCase = true,
            )
        }
        .sortedWith(
            compareByDescending<UserTag> {
                it.name.startsWith(
                    query,
                    ignoreCase = true,
                )
            }
                .thenByDescending { it.count }
                .thenBy { it.name },
        )
}
