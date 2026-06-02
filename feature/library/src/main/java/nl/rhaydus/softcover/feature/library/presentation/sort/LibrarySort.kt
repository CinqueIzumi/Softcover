package nl.rhaydus.softcover.feature.library.presentation.sort

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import java.time.LocalDate

/**
 * Decorate-sort-undecorate: extract the comparable key once per item (O(n)), then sort by the
 * cached keys. Reserved for in-memory edition lists (custom-list tabs); book sorting happens at
 * the DAO via SQL `ORDER BY`.
 */
private inline fun <T, K : Comparable<K>> List<T>.sortedByCachedKey(
    selector: (T) -> K,
): List<T> {
    if (size <= 1) return this

    val n = size
    val items = arrayOfNulls<Any?>(n)
    @Suppress("UNCHECKED_CAST")
    val keys = arrayOfNulls<Comparable<Any>>(n) as Array<K?>

    for (i in 0 until n) {
        val item = this[i]
        items[i] = item
        keys[i] = selector(item)
    }

    val indices = IntArray(n) { it }.toTypedArray()
    indices.sortWith(Comparator { a, b -> keys[a]!!.compareTo(keys[b]!!) })

    val result = ArrayList<T>(n)
    for (index in indices) {
        @Suppress("UNCHECKED_CAST")
        result.add(items[index] as T)
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
        LibrarySortMode.RELEASE_DATE -> sortedByCachedKey { it.releaseDate ?: LocalDate.MAX }
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
