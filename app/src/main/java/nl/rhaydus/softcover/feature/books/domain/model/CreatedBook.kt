package nl.rhaydus.softcover.feature.books.domain.model

/**
 * The Hardcover book (and, when Hardcover returns one, edition) freshly created via `upsert_book`
 * for an ISBN that wasn't catalogued yet. Distinct from [IsbnEditionMatch] because a just-created
 * book may not have a previewable edition id, so [editionId] is nullable here.
 */
data class CreatedBook(
    val bookId: Int,
    val editionId: Int?,
)
