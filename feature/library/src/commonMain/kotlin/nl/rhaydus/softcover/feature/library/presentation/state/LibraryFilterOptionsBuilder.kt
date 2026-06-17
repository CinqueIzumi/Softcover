package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * Rating thresholds offered as filter chips. Each is included only if the underlying tab has at
 * least one book whose rating meets it — see [buildBookFilterOptions] / [buildEditionFilterOptions].
 */
private val RATING_BUCKETS = listOf(4.5, 4.0, 3.5, 3.0)

/**
 * Pure, dispatcher-friendly helpers that derive a [LibraryFilterOptions] for a tab. Kept top-level
 * (rather than as members of [LibraryUiState]) so [FilterOptionsCollector] can call them on
 * `Dispatchers.Default` without keeping a reference to the whole UI state on the worker thread.
 */
internal fun buildBookFilterOptions(books: List<Book>): LibraryFilterOptions {
    if (books.isEmpty()) return LibraryFilterOptions()

    val tags = books
        .flatMap { it.tags }
        .filter { it.category == TagCategory.GENRE }
        .distinctBy { it.id }
        .sortedBy { it.name.lowercase() }

    val formats = books
        .flatMap { book -> book.editions.map { it.format } }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val years = books
        .map { it.releaseYear }
        .filter { it > 0 }
        .distinct()
        .sortedDescending()

    val supportsOwned = books.any { book -> book.editions.any { it.owned } }

    val maxRating = books.maxOfOrNull { it.rating } ?: 0.0
    val ratingBuckets = RATING_BUCKETS.filter { it <= maxRating }

    return LibraryFilterOptions(
        tags = tags,
        formats = formats,
        releaseYears = years,
        supportsOwnedFilter = supportsOwned,
        ratingBuckets = ratingBuckets,
    )
}

internal fun buildEditionFilterOptions(
    editions: List<BookEdition>,
    bookByBookId: Map<Int, Book>,
): LibraryFilterOptions {
    if (editions.isEmpty()) return LibraryFilterOptions()

    val parentBooks = editions.mapNotNull { bookByBookId[it.bookId] }

    val tags: List<Tag> = parentBooks
        .flatMap { it.tags }
        .filter { it.category == TagCategory.GENRE }
        .distinctBy { it.id }
        .sortedBy { it.name.lowercase() }

    val formats = editions
        .map { it.format }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val years = parentBooks
        .map { it.releaseYear }
        .filter { it > 0 }
        .distinct()
        .sortedDescending()

    val supportsOwned = editions.any { it.owned }

    val maxRating = parentBooks.maxOfOrNull { it.rating } ?: 0.0
    val ratingBuckets = RATING_BUCKETS.filter { it <= maxRating }

    return LibraryFilterOptions(
        tags = tags,
        formats = formats,
        releaseYears = years,
        supportsOwnedFilter = supportsOwned,
        ratingBuckets = ratingBuckets,
    )
}
