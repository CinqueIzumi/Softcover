package nl.rhaydus.softcover.core.book.domain.model

/**
 * The outcome of one `upsert_book` attempt for an ISBN that wasn't catalogued yet.
 */
sealed interface BookImport {
    /**
     * Hardcover has the book. [editionId] is nullable because a just-created book may not have a
     * previewable edition, unlike [IsbnEditionMatch].
     */
    data class Created(
        val bookId: Int,
        val editionId: Int?,
    ) : BookImport

    /** Hardcover queued an asynchronous import; repeating the same call returns the book once it lands. */
    data object Fetching : BookImport
}
