package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.library.presentation.util.availableFinishedYears

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
internal fun buildBookFilterOptions(
    books: List<Book>,
    userTagsByBookId: Map<Int, List<UserTag>> = emptyMap(),
    isReadTab: Boolean = false,
): LibraryFilterOptions {
    if (books.isEmpty()) return LibraryFilterOptions()

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

    val readYears = if (isReadTab) books.availableFinishedYears() else emptyList()

    val supportsOwned = books.any { book -> book.editions.any { it.owned } }

    val maxRating = books.maxOfOrNull { it.rating } ?: 0.0
    val ratingBuckets = RATING_BUCKETS.filter { it <= maxRating }

    return LibraryFilterOptions(
        hardcoverTags = buildHardcoverTagOptions(books = books),
        yourTags = buildYourTagOptions(userTagsPerBook = books.map { userTagsByBookId[it.id].orEmpty() }),
        formats = formats.toImmutableList(),
        releaseYears = years.toImmutableList(),
        readYears = readYears.toImmutableList(),
        supportsOwnedFilter = supportsOwned,
        ratingBuckets = ratingBuckets.toImmutableList(),
    )
}

internal fun buildEditionFilterOptions(
    editions: List<BookEdition>,
    bookByBookId: Map<Int, Book>,
    userTagsByBookId: Map<Int, List<UserTag>> = emptyMap(),
): LibraryFilterOptions {
    if (editions.isEmpty()) return LibraryFilterOptions()

    val parentBooks = editions.mapNotNull { bookByBookId[it.bookId] }
    val uniqueParentBooks = parentBooks.distinctBy { it.id }

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
        hardcoverTags = buildHardcoverTagOptions(books = uniqueParentBooks),
        yourTags = buildYourTagOptions(
            userTagsPerBook = uniqueParentBooks.map { userTagsByBookId[it.id].orEmpty() },
        ),
        formats = formats.toImmutableList(),
        releaseYears = years.toImmutableList(),
        supportsOwnedFilter = supportsOwned,
        ratingBuckets = ratingBuckets.toImmutableList(),
    )
}
