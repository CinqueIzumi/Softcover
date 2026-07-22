package nl.rhaydus.softcover.core.book.domain.sync

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

/**
 * Owns the offline-first user-book write path: queuing optimistic mutations that could not reach the
 * server, and replaying them against a fresh server fetch. Extracted from `BooksRepositoryImpl` so the
 * repository reads as "fetch + merge" and the sync machinery is independently testable.
 */
interface OfflineUserBookSync {
    suspend fun enqueueProgressUpdate(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
        actionAt: String? = null,
    )

    suspend fun enqueueMarkAsRead(
        book: Book,
        actionAt: String? = null,
    )

    suspend fun enqueueRatingUpdate(
        book: Book,
        rating: Double,
    )

    suspend fun enqueueReviewUpdate(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    )

    /**
     * Drains the offline write queue, then fetches fresh server data via [fetchFromServer], then
     * reconciles the two. The drain runs **before** the fetch so the fetch has the best chance of
     * already reflecting the just-replayed writes; the reconcile then re-applies, for each synced
     * write, only the fields that write owns — covering read-after-write lag without clobbering any
     * field the server changed underneath. Returns the books to cache.
     */
    suspend fun drainAndReconcile(fetchFromServer: suspend () -> List<Book>): List<Book>
}
