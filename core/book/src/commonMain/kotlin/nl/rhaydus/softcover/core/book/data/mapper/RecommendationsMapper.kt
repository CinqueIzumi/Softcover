package nl.rhaydus.softcover.core.book.data.mapper

import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.model.SystemVibe

/**
 * Hardcover's `vibe_type` and `slug` for a [SystemVibe]. The schema has no lookup type, so these are
 * observed rather than confirmed: 0 is user-made, 1 Recommendations, 3 Top Picks, 4 book club.
 */
internal fun SystemVibe.toVibeType(): Int = when (this) {
    SystemVibe.Recommendations -> 1
    SystemVibe.TopPicks -> 3
}

internal fun SystemVibe.toVibeSlug(): String = when (this) {
    SystemVibe.Recommendations -> "recommendations"
    SystemVibe.TopPicks -> "top-picks"
}

/** Shares the trending tables without colliding with a trending, upcoming or recent key. */
internal const val RECOMMENDATIONS_STORAGE_KEY = "recommendations"

/**
 * Throws [UnexpectedApiException] when [cachedBookIds] is not a list; elements that are not ids are skipped.
 */
internal fun parseCachedBookIds(
    cachedBookIds: Any?,
    limit: Int,
): List<Int> {
    if (cachedBookIds !is List<*>) {
        throw UnexpectedApiException(message = "cached_book_ids is not a list")
    }

    return cachedBookIds
        .mapNotNull { element ->
            when (element) {
                is Number -> element.toInt()
                is String -> element.toIntOrNull()
                else -> element.toString().toIntOrNull()
            }
        }
        .distinct()
        .take(limit)
}
