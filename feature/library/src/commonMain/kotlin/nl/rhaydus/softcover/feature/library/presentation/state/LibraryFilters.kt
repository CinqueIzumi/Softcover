package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Tag

internal data class LibraryFilters(
    val tags: Set<Tag> = emptySet(),
    val formats: Set<String> = emptySet(),
    val releaseYears: Set<Int> = emptySet(),
    val owned: Boolean? = null,
    val ratingMin: Double? = null,
) {
    val isEmpty: Boolean
        get() = tags.isEmpty() &&
            formats.isEmpty() &&
            releaseYears.isEmpty() &&
            owned == null &&
            ratingMin == null

    private val activeTagIds: Set<Int>
        get() = tags.mapTo(mutableSetOf()) { it.id }

    fun matchesBook(book: Book): Boolean {
        if (tags.isNotEmpty()) {
            val ids = activeTagIds
            if (book.tags.none { it.id in ids }) return false
        }

        if (formats.isNotEmpty() && book.editions.none { it.format in formats }) return false

        if (releaseYears.isNotEmpty() && book.releaseYear !in releaseYears) return false

        when (owned) {
            true -> if (book.editions.none { it.owned }) return false
            false -> if (book.editions.any { it.owned }) return false
            null -> Unit
        }

        if (ratingMin != null && book.rating < ratingMin) return false

        return true
    }

    /**
     * Edition-side filter for custom-list tabs. Book-level facets (tags, releaseYear, rating)
     * are resolved against the parent [book] when supplied; edition-level facets (format, owned)
     * are matched against the specific [edition].
     */
    fun matchesEdition(
        edition: BookEdition,
        book: Book?,
    ): Boolean {
        if (tags.isNotEmpty()) {
            val ids = activeTagIds
            val bookTags = book?.tags.orEmpty()
            if (bookTags.none { it.id in ids }) return false
        }

        if (formats.isNotEmpty() && edition.format !in formats) return false

        if (releaseYears.isNotEmpty()) {
            val year = book?.releaseYear ?: edition.releaseYear
            if (year !in releaseYears) return false
        }

        when (owned) {
            true -> if (edition.owned.not()) return false
            false -> if (edition.owned) return false
            null -> Unit
        }

        if (ratingMin != null) {
            val rating = book?.rating ?: 0.0
            if (rating < ratingMin) return false
        }

        return true
    }
}
