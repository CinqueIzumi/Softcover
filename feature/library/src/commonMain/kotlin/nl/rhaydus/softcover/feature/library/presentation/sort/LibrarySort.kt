package nl.rhaydus.softcover.feature.library.presentation.sort

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection

/**
 * Decorate-sort-undecorate: extract the comparable key once per item (O(n)), then sort by the
 * cached keys. Reserved for in-memory edition lists (custom-list tabs); book sorting happens at
 * the DAO via SQL `ORDER BY`.
 */
private inline fun <T, K : Comparable<K>> List<T>.sortedByCachedKey(
    selector: (T) -> K,
): List<T> {
    if (size <= 1) return this

    // `selector` returns a non-null K, so the key cache needs no nullable slots — which is what let the
    // previous array-of-nulls version reach for `!!` and two unchecked casts to undo its own erasure.
    val keys = ArrayList<K>(size)
    for (item in this) {
        keys += selector(item)
    }

    val indices = IntArray(size) { it }.toTypedArray()
    indices.sortWith(Comparator { a, b -> keys[a].compareTo(keys[b]) })

    val result = ArrayList<T>(size)
    for (index in indices) {
        result.add(this[index])
    }

    return result
}

private fun BookEdition.firstAuthor(): String =
    authors.firstOrNull()?.name?.lowercase().orEmpty()

// Lexically larger than any ISO-8601 timestamp Hardcover emits, so editions without a stored
// `list_books.created_at` fall to the bottom of the ASCENDING sort (and to the top of DESCENDING
// after the asReversed() pass).
private const val MISSING_ADDED_AT_SENTINEL: String = "￿"

internal fun List<BookEdition>.applyEditionSort(
    mode: LibrarySortMode,
    direction: SortDirection = mode.defaultDirection,
    addedAtByEditionId: Map<Int, String?> = emptyMap(),
): List<BookEdition> {
    if (size <= 1) return this

    val ascending = when (mode) {
        LibrarySortMode.TITLE -> sortedByCachedKey { it.title.orEmpty().lowercase() }
        LibrarySortMode.AUTHOR -> sortedByCachedKey { it.firstAuthor() }
        LibrarySortMode.PAGE_COUNT -> sortedByCachedKey { it.pages ?: 0 }
        LibrarySortMode.RELEASE_DATE -> sortedByCachedKey { it.releaseDate ?: LocalDate(
            9999,
            12,
            31,
        ) }
        LibrarySortMode.DATE_ADDED -> sortedByCachedKey {
            addedAtByEditionId[it.id] ?: MISSING_ADDED_AT_SENTINEL
        }
        LibrarySortMode.DATE_FINISHED,
        LibrarySortMode.RATING,
        LibrarySortMode.PROGRESS,
        LibrarySortMode.DEADLINE_URGENCY,
        LibrarySortMode.MANUAL,
        LibrarySortMode.ORDER,
            -> return this
    }

    return if (direction == SortDirection.ASCENDING) ascending else ascending.asReversed()
}
