package nl.rhaydus.softcover.core.book.data.sync

import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.domain.sync.OfflineUserBookSync
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import kotlin.time.Clock

/**
 * Gaps between retry drains after an enqueue, about five minutes in total. The first waits out a typical
 * rate-limit window; the later ones back off so a persistently failing write stops costing requests, and
 * the poison cap ends its retries by then anyway.
 */
private val RETRY_DELAYS_MS: List<Long> = listOf(
    30_000L,
    90_000L,
    180_000L,
)

internal class OfflineUserBookSyncImpl(
    private val userBookWriteQueue: UserBookWriteQueue,
    private val userBookWriteDrainer: UserBookWriteDrainer,
    private val booksLocalDataSource: BooksLocalDataSource,
    private val userBookCacheGate: UserBookCacheGate,
    private val networkAvailability: NetworkAvailabilityProvider,
    private val applicationScope: ApplicationScope,
) : OfflineUserBookSync {
    private val retryMutex = Mutex()

    private var retryJob: Job? = null

    private val retainedHintsMutex = Mutex()

    private val retainedHints: MutableMap<Int, Set<PendingUserBookWriteKind>> = mutableMapOf()

    override suspend fun enqueueProgressUpdate(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
        actionAt: String?,
        progressHistoryId: Long?,
    ) {
        val userBook = book.userBook ?: return
        val userBookRead = book.currentRead ?: return

        // A placeholder's dates are local guesses; replay takes the server read's own instead.
        val startedAt = userBookRead.startedAt.takeUnless { userBookRead.isPlaceholder }
        val finishedAt = userBookRead.finishedAt.takeUnless { userBookRead.isPlaceholder }

        enqueueAndScheduleRetry(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = newPage,
                progressSeconds = newSeconds,
                startedAt = startedAt,
                finishedAt = finishedAt,
                enqueuedAt = Clock.System.now().toString(),
                actionAt = actionAt,
                progressHistoryId = progressHistoryId,
            ),
        )
    }

    override suspend fun enqueueMarkAsRead(
        book: Book,
        actionAt: String?,
    ) {
        val userBook = book.userBook ?: return
        val userBookRead = book.currentRead ?: return

        enqueueAndScheduleRetry(
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
                actionAt = actionAt,
            ),
        )
    }

    override suspend fun enqueueStatusUpdate(
        book: Book,
        status: UserBookStatus,
    ) {
        val userBook = book.userBook ?: return

        enqueueAndScheduleRetry(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                userBookId = userBook.id,
                // Unused for SET_STATUS replay (which keys off userBookId); a Want to read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.currentRead?.id ?: 0,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                enqueuedAt = Clock.System.now().toString(),
                statusCode = status.code,
            ),
        )
    }

    override suspend fun enqueueRatingUpdate(
        book: Book,
        rating: Double,
    ) {
        val userBook = book.userBook ?: return

        enqueueAndScheduleRetry(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                userBookId = userBook.id,
                // Unused for UPDATE_RATING replay (which keys off userBookId); a Read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.currentRead?.id ?: 0,
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

        enqueueAndScheduleRetry(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = userBook.id,
                // Unused for UPDATE_REVIEW replay (which keys off userBookId); a Read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.currentRead?.id ?: 0,
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

    /**
     * The foundation drainer only replays on a connectivity return or an on-demand [drain], so a write
     * queued by a transient failure while online (a 429, say) would otherwise wait for the next refresh.
     * Every enqueue restarts one spaced retry schedule, so each write gets the full spacing and the
     * retries never add request fan-out against the rate limit.
     */
    private suspend fun enqueueAndScheduleRetry(write: PendingUserBookWrite) {
        userBookWriteQueue.enqueue(write)

        if (networkAvailability.isOnline.value.not()) return

        retryMutex.withLock {
            retryJob?.cancel()

            retryJob = applicationScope.scope.launch { runRetrySchedule() }
        }
    }

    private suspend fun runRetrySchedule() {
        for (delayMs in RETRY_DELAYS_MS) {
            delay(delayMs)

            // Offline, the drainer's own connectivity trigger replays the queue on reconnect.
            if (networkAvailability.isOnline.value.not()) return

            // A restart cancels the schedule, never a replay already on the wire: cancelling mid-request
            // could leave a write the server applied still queued.
            withContext(NonCancellable) {
                runCatchingLogged(context = "queued user book write retry") {
                    retainHints(synced = userBookWriteDrainer.drain())
                }
            }

            if (userBookWriteQueue.hasPending().not()) return
        }
    }

    /**
     * A drain clears the drainer's reconcile hints, so the retry keeps what it synced for the next
     * [drainReconcileAndCache] instead of letting a later fetch overwrite the optimistic value. Hints are
     * taken under the mutex only after a drain returns, so a retry that races a refresh leaves its hint
     * for the following refresh: applied one refresh late, never lost or applied twice. They live in
     * memory only and are consumed by the next refresh, so they need no expiry.
     */
    private suspend fun retainHints(synced: Map<Int, Set<PendingUserBookWriteKind>>) {
        retainedHintsMutex.withLock {
            synced.forEach { (userBookId, kinds) ->
                retainedHints[userBookId] = retainedHints[userBookId].orEmpty() + kinds
            }
        }
    }

    private suspend fun takeRetainedHints(
        drained: Map<Int, Set<PendingUserBookWriteKind>>,
    ): Map<Int, Set<PendingUserBookWriteKind>> = retainedHintsMutex.withLock {
        val merged: MutableMap<Int, Set<PendingUserBookWriteKind>> = retainedHints.toMutableMap()

        drained.forEach { (userBookId, kinds) ->
            merged[userBookId] = merged[userBookId].orEmpty() + kinds
        }

        retainedHints.clear()

        merged
    }

    override suspend fun drainReconcileAndCache(
        fetchFromServer: suspend () -> List<Book>,
        selectBooksToCache: suspend (List<Book>) -> List<Book>,
    ): List<Book> {
        val drained: Map<Int, Set<PendingUserBookWriteKind>> = userBookWriteDrainer.drain()
        val syncedWrites: Map<Int, Set<PendingUserBookWriteKind>> = takeRetainedHints(drained = drained)

        return userBookCacheGate.refresh(
            fetch = fetchFromServer,
            reconcile = { fetchedBooks, window ->
                preserveSyncedWrites(
                    fetchedBooks = fetchedBooks,
                    syncedWrites = syncedWrites.mergedWith(window.writes),
                ).filterNot { it.userBook?.id in window.removedUserBookIds }
            },
            selectToCache = selectBooksToCache,
        )
    }

    private fun Map<Int, Set<PendingUserBookWriteKind>>.mergedWith(
        other: Map<Int, Set<PendingUserBookWriteKind>>,
    ): Map<Int, Set<PendingUserBookWriteKind>> {
        val merged: MutableMap<Int, Set<PendingUserBookWriteKind>> = toMutableMap()

        other.forEach { (userBookId, kinds) ->
            merged[userBookId] = merged[userBookId].orEmpty() + kinds
        }

        return merged
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

            kinds
                .fold(fetched) { book, kind ->
                    book.preserveOwnedFields(
                        kind = kind,
                        snapshot = snapshot,
                    )
                }
                .preserveUnfinishedRead(snapshot = snapshot)
        }
    }

    /**
     * A status change into Reading can outrun the bulk fetch, which then lacks the new unfinished read:
     * the local placeholder after an offline write, the server's own read after an online one. Keeping
     * the snapshot's read stops `currentRead` falling back to an older finished read.
     */
    private fun Book.preserveUnfinishedRead(snapshot: Book): Book {
        val snapshotRead: UserBookRead = snapshot.currentRead ?: return this

        if (snapshotRead.isFinished) return this

        val reconciledUserBook: UserBook = userBook ?: return this

        val isReadingLike: Boolean = reconciledUserBook.status == BookStatus.Reading ||
            reconciledUserBook.status == BookStatus.Paused

        if (isReadingLike.not() || reconciledUserBook.reads.any { it.isFinished.not() }) return this

        if (reconciledUserBook.reads.any { it.id == snapshotRead.id }) return this

        return copy(userBook = reconciledUserBook.copy(reads = reconciledUserBook.reads + snapshotRead))
    }

    private fun Book.preserveOwnedFields(
        kind: PendingUserBookWriteKind,
        snapshot: Book,
    ): Book = when (kind) {
        PendingUserBookWriteKind.UPDATE_PROGRESS -> preserveProgress(snapshot = snapshot)

        PendingUserBookWriteKind.MARK_AS_READ -> preserveReadStatus(snapshot = snapshot)

        PendingUserBookWriteKind.SET_STATUS -> preserveStatus(snapshot = snapshot)

        PendingUserBookWriteKind.UPDATE_RATING -> preserveRating(snapshot = snapshot)

        PendingUserBookWriteKind.UPDATE_REVIEW -> preserveReview(snapshot = snapshot)
    }

    private fun Book.preserveProgress(snapshot: Book): Book {
        val snapshotRead: UserBookRead = snapshot.currentRead ?: return this

        if (snapshotRead.isFinished) return this

        val fetchedUserBook: UserBook = userBook ?: return this

        val matchingRead: UserBookRead? = fetchedUserBook.reads.firstOrNull { it.id == snapshotRead.id }

        val updatedReads: List<UserBookRead> = when {
            matchingRead != null -> fetchedUserBook.reads.map { read ->
                if (read.id == snapshotRead.id) {
                    read.copy(
                        currentPage = snapshotRead.currentPage,
                        currentSeconds = snapshotRead.currentSeconds,
                        progress = snapshotRead.progress,
                        startedAt = snapshotRead.startedAt,
                        finishedAt = snapshotRead.finishedAt,
                    )
                } else {
                    read
                }
            }

            // The bulk fetch hasn't surfaced the read row yet (lag) but the local snapshot carries it with
            // its genuine prior-fetch id, so reinstate it; the next refresh takes the server copy wholesale.
            snapshotRead.isPlaceholder.not() -> fetchedUserBook.reads + snapshotRead

            else -> fetchedUserBook.reads
        }

        return copy(userBook = fetchedUserBook.copy(reads = updatedReads))
    }

    private fun Book.preserveReadStatus(snapshot: Book): Book {
        val snapshotUserBook: UserBook = snapshot.userBook ?: return this
        val fetchedUserBook: UserBook = userBook ?: return this

        val snapshotRead: UserBookRead? = snapshot.currentRead

        val updatedReads: List<UserBookRead> = fetchedUserBook.reads.map { read ->
            if (snapshotRead != null && read.id == snapshotRead.id) {
                read.copy(
                    progress = snapshotRead.progress,
                    finishedAt = snapshotRead.finishedAt,
                )
            } else {
                read
            }
        }

        // Same lag as in preserveProgress: the fetch may not carry the read this write finished yet.
        val isSnapshotReadMissing: Boolean = snapshotRead != null &&
            snapshotRead.isPlaceholder.not() &&
            fetchedUserBook.reads.none { it.id == snapshotRead.id }

        val missingSnapshotRead: List<UserBookRead> = if (isSnapshotReadMissing) {
            listOfNotNull(snapshotRead)
        } else {
            emptyList()
        }

        return copy(
            userBook = fetchedUserBook.copy(
                status = snapshotUserBook.status,
                reads = updatedReads + missingSnapshotRead,
            ),
        )
    }

    private fun Book.preserveStatus(snapshot: Book): Book {
        val snapshotUserBook: UserBook = snapshot.userBook ?: return this
        val fetchedUserBook: UserBook = userBook ?: return this

        return copy(userBook = fetchedUserBook.copy(status = snapshotUserBook.status))
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
