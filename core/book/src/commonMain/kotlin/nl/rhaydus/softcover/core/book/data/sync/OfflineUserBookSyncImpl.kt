package nl.rhaydus.softcover.core.book.data.sync

import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.domain.sync.OfflineUserBookSync
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead

internal class OfflineUserBookSyncImpl(
    private val userBookWriteQueue: UserBookWriteQueue,
    private val userBookWriteDrainer: UserBookWriteDrainer,
    private val booksLocalDataSource: BooksLocalDataSource,
) : OfflineUserBookSync {
    override suspend fun enqueueProgressUpdate(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
    ) {
        val userBook = book.userBook ?: return
        val userBookRead = book.userBookRead ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = newPage,
                progressSeconds = newSeconds,
                startedAt = userBookRead.startedAt,
                finishedAt = userBookRead.finishedAt,
                enqueuedAt = Clock.System.now().toString(),
            ),
        )
    }

    override suspend fun enqueueMarkAsRead(book: Book) {
        val userBook = book.userBook ?: return
        val userBookRead = book.userBookRead ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = userBookRead.currentPage,
                progressSeconds = userBookRead.currentSeconds,
                startedAt = userBookRead.startedAt,
                finishedAt = userBookRead.finishedAt,
                enqueuedAt = Clock.System.now().toString(),
            ),
        )
    }

    override suspend fun enqueueRatingUpdate(
        book: Book,
        rating: Double,
    ) {
        val userBook = book.userBook ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                userBookId = userBook.id,
                // Unused for UPDATE_RATING replay (which keys off userBookId); a Read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.userBookRead?.id ?: 0,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                rating = rating,
                enqueuedAt = Clock.System.now().toString(),
            ),
        )
    }

    override suspend fun enqueueReviewUpdate(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ) {
        val userBook = book.userBook ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = userBook.id,
                // Unused for UPDATE_REVIEW replay (which keys off userBookId); a Read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.userBookRead?.id ?: 0,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                reviewSlateJson = review.toJson(),
                reviewHasSpoilers = hasSpoilers,
                enqueuedAt = Clock.System.now().toString(),
            ),
        )
    }

    override suspend fun drainAndReconcile(fetchFromServer: suspend () -> List<Book>): List<Book> {
        val syncedWrites: Map<Int, Set<PendingUserBookWriteKind>> = userBookWriteDrainer.drainPendingUpdates()

        val fetchedBooks: List<Book> = fetchFromServer()

        return preserveSyncedWrites(
            fetchedBooks = fetchedBooks,
            syncedWrites = syncedWrites,
        )
    }

    /**
     * Reconciles a fresh server fetch with writes that were just replayed from the offline queue.
     * A replayed write reached the server but the bulk fetch can lag behind it (read-after-write),
     * so for each affected book we re-apply the local optimistic value — but only for the fields
     * the replayed write actually owns. Every other field (e.g. a deadline another client moved)
     * flows through from the server, so the local copy can no longer clobber server-of-record state.
     * Owned fields self-heal on the next refresh once the queue is empty and nothing is preserved.
     */
    private suspend fun preserveSyncedWrites(
        fetchedBooks: List<Book>,
        syncedWrites: Map<Int, Set<PendingUserBookWriteKind>>,
    ): List<Book> {
        if (syncedWrites.isEmpty()) return fetchedBooks

        // A Room DAO flow always emits (at least an empty list), so `firstOrNull()` only removes the
        // empty-flow throw; an upstream failure still propagates, and is caught by the `runCatchingLogged`
        // wrapping the calling use case. An absent snapshot means "nothing to reconcile".
        val snapshots: Map<Int, Book> = booksLocalDataSource.allUserBooks
            .firstOrNull()
            .orEmpty()
            .mapNotNull { book ->
                val userBookId: Int = book.userBook?.id ?: return@mapNotNull null

                if (userBookId in syncedWrites) userBookId to book else null
            }
            .toMap()

        if (snapshots.isEmpty()) return fetchedBooks

        return fetchedBooks.map { fetched ->
            val userBookId: Int = fetched.userBook?.id ?: return@map fetched
            val kinds: Set<PendingUserBookWriteKind> = syncedWrites[userBookId] ?: return@map fetched
            val snapshot: Book = snapshots[userBookId] ?: return@map fetched

            kinds.fold(fetched) { book, kind ->
                book.preserveOwnedFields(
                    kind = kind,
                    snapshot = snapshot,
                )
            }
        }
    }

    private fun Book.preserveOwnedFields(
        kind: PendingUserBookWriteKind,
        snapshot: Book,
    ): Book = when (kind) {
        PendingUserBookWriteKind.UPDATE_PROGRESS -> preserveProgress(snapshot = snapshot)

        PendingUserBookWriteKind.MARK_AS_READ -> preserveReadStatus(snapshot = snapshot)

        PendingUserBookWriteKind.UPDATE_RATING -> preserveRating(snapshot = snapshot)

        PendingUserBookWriteKind.UPDATE_REVIEW -> preserveReview(snapshot = snapshot)
    }

    private fun Book.preserveProgress(snapshot: Book): Book {
        val snapshotRead: UserBookRead = snapshot.userBookRead ?: return this
        // The bulk fetch hasn't surfaced the read row yet (lag) but the local snapshot carries it with
        // its genuine prior-fetch id, so reinstate it; the next refresh takes the server copy wholesale.
        val fetchedRead: UserBookRead = userBookRead ?: return copy(userBookRead = snapshotRead)

        return copy(
            userBookRead = fetchedRead.copy(
                currentPage = snapshotRead.currentPage,
                currentSeconds = snapshotRead.currentSeconds,
                progress = snapshotRead.progress,
                startedAt = snapshotRead.startedAt,
                finishedAt = snapshotRead.finishedAt,
            ),
        )
    }

    private fun Book.preserveReadStatus(snapshot: Book): Book {
        val snapshotUserBook: UserBook = snapshot.userBook ?: return this
        val fetchedUserBook: UserBook = userBook ?: return this

        val snapshotRead: UserBookRead? = snapshot.userBookRead
        val fetchedRead: UserBookRead? = userBookRead

        val updatedRead: UserBookRead? = if (snapshotRead != null && fetchedRead != null) {
            fetchedRead.copy(
                progress = snapshotRead.progress,
                finishedAt = snapshotRead.finishedAt,
            )
        } else {
            fetchedRead
        }

        return copy(
            userBook = fetchedUserBook.copy(status = snapshotUserBook.status),
            userBookRead = updatedRead,
        )
    }

    private fun Book.preserveRating(snapshot: Book): Book {
        val snapshotUserBook: UserBook = snapshot.userBook ?: return this
        val fetchedUserBook: UserBook = userBook ?: return this

        return copy(userBook = fetchedUserBook.copy(rating = snapshotUserBook.rating))
    }

    private fun Book.preserveReview(snapshot: Book): Book {
        val snapshotUserBook: UserBook = snapshot.userBook ?: return this
        val fetchedUserBook: UserBook = userBook ?: return this

        return copy(
            userBook = fetchedUserBook.copy(
                reviewDocument = snapshotUserBook.reviewDocument,
                reviewHasSpoilers = snapshotUserBook.reviewHasSpoilers,
            ),
        )
    }
}
