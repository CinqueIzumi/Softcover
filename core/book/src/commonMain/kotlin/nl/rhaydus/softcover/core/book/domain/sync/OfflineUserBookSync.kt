package nl.rhaydus.softcover.core.book.domain.sync

import nl.rhaydus.softcover.core.book.domain.model.UserBooksSnapshot
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

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
        progressHistoryId: Long? = null,
    ): QueuedWrite

    suspend fun enqueueMarkAsRead(
        book: Book,
        actionAt: String? = null,
    ): QueuedWrite

    suspend fun enqueueStatusUpdate(
        book: Book,
        status: UserBookStatus,
    ): QueuedWrite

    suspend fun enqueueRatingUpdate(
        book: Book,
        rating: Double,
    ): QueuedWrite

    suspend fun enqueueReviewUpdate(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ): QueuedWrite

    /**
     * Runs [block] on the application scope under the per-book write lock and awaits it. Cancelling the
     * caller cancels only the wait, never [block]. [block] must not call `drain()` or the cache gate's
     * `refresh`.
     */
    suspend fun <T> runSerialized(
        bookId: Int,
        block: suspend () -> T,
    ): T

    suspend fun settle(write: QueuedWrite)

    fun retryLater()

    suspend fun hasOlderPending(write: QueuedWrite): Boolean

    /**
     * Drains the offline write queue, fetches fresh server data via [fetchFromServer], reconciles the
     * two and caches what [selectBooksToCache] keeps of the result. The drain runs **before** the fetch so
     * the fetch has the best chance of already reflecting the just-replayed writes. The reconcile then
     * re-applies, for each replayed write and for each local write made while the fetch was in flight,
     * only the fields that write owns, covering read-after-write lag without clobbering any field the
     * server changed underneath; books removed locally during the fetch are dropped. Merge and save run
     * serialised against other local user-book writes, so [selectBooksToCache] must not write user books.
     * Books whose write is still queued after the drain keep their owned fields too. The snapshot's tags
     * are stored for the cached books, except books tagged during the fetch.
     * Returns the reconciled books.
     */
    suspend fun drainReconcileAndCache(
        fetchFromServer: suspend () -> UserBooksSnapshot,
        selectBooksToCache: suspend (List<Book>) -> List<Book>,
    ): List<Book>
}
