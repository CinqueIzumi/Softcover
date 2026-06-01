package nl.rhaydus.softcover.feature.books.domain.model

/**
 * The Hardcover book and edition a scanned/typed ISBN resolves to. The edition is carried alongside
 * the book so the detail screen can preview the *exact* scanned edition rather than the book's
 * default edition (which is all the book id alone would yield for an off-shelf book).
 */
data class IsbnEditionMatch(
    val bookId: Int,
    val editionId: Int,
)
