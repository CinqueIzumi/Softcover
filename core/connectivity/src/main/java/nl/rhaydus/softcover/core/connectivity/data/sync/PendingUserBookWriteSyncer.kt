package nl.rhaydus.softcover.core.connectivity.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromJson
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import timber.log.Timber

class PendingUserBookWriteSyncer(
    private val networkAvailability: NetworkAvailabilityProvider,
    private val dao: PendingUserBookWriteDao,
    private val booksRemoteDataSource: BooksRemoteDataSource,
) : UserBookWriteDrainer {
    private var job: Job? = null
    private val drainMutex: Mutex = Mutex()
    private val recentlySyncedUserBookIds: MutableSet<Int> = mutableSetOf()

    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return

        job = scope.launch(Dispatchers.IO) {
            networkAvailability.isOnline
                .onStart { if (networkAvailability.isOnline.value) drainMutex.withLock { drain() } }
                .onEach { online -> if (online) drainMutex.withLock { drain() } }
                .collect()
        }
    }

    override suspend fun drainPendingUpdates(): Set<Int> = drainMutex.withLock {
        drain()

        val snapshot: Set<Int> = recentlySyncedUserBookIds.toSet()
        recentlySyncedUserBookIds.clear()

        snapshot
    }

    private suspend fun drain() {
        val pending = runCatching { dao.getPending() }.getOrElse { emptyList() }

        for (entity in pending) {
            val replayed = runCatching { replay(entity) }

            replayed
                .onSuccess { outcome ->
                    dao.delete(entity.localId)

                    if (outcome == ReplayOutcome.SYNCED) {
                        recentlySyncedUserBookIds.add(entity.userBookId)
                    }
                }
                .onFailure { error ->
                    Timber.w(
                        error,
                        "Pending user-book write ${entity.localId} failed; halting drain",
                    )
                    dao.incrementAttempts(entity.localId)
                    return
                }
        }
    }

    /**
     * Replays a single queued mutation. A [ReplayOutcome.SYNCED] result means the mutation reached
     * the server and the affected book was genuinely updated; a [ReplayOutcome.DISCARDED] result
     * means the row was unprocessable (unknown kind, missing payload) and should be dropped without
     * being counted as a server-side sync. A thrown exception (network/server failure) is handled by
     * [drain] — the row is kept and retried later.
     */
    private suspend fun replay(entity: PendingUserBookWriteEntity): ReplayOutcome {
        return when (entity.kind) {
            PendingUserBookWriteKind.UPDATE_PROGRESS.name -> {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = entity.userBookReadId,
                    editionId = entity.editionId,
                    progressPages = entity.progressPages,
                    progressSeconds = entity.progressSeconds,
                    startedAt = entity.startedAt,
                    finishedAt = entity.finishedAt,
                )

                ReplayOutcome.SYNCED
            }

            PendingUserBookWriteKind.MARK_AS_READ.name -> {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = entity.bookId,
                    userDate = entity.enqueuedAt.substringBefore('T'),
                )

                ReplayOutcome.SYNCED
            }

            PendingUserBookWriteKind.UPDATE_RATING.name -> {
                val rating = entity.rating

                if (rating == null) {
                    Timber.w("Pending rating update ${entity.localId} has no rating; discarding")

                    ReplayOutcome.DISCARDED
                } else {
                    booksRemoteDataSource.replayUpdateBookRating(
                        userBookId = entity.userBookId,
                        rating = rating,
                    )

                    ReplayOutcome.SYNCED
                }
            }

            PendingUserBookWriteKind.UPDATE_REVIEW.name -> {
                val document = reviewDocumentFromJson(json = entity.reviewSlateJson)

                if (document == null) {
                    Timber.w("Pending review update ${entity.localId} has no body; discarding")

                    ReplayOutcome.DISCARDED
                } else {
                    booksRemoteDataSource.replayUpdateBookReview(
                        userBookId = entity.userBookId,
                        review = document,
                        hasSpoilers = entity.reviewHasSpoilers ?: false,
                        reviewedAt = entity.enqueuedAt.substringBefore('T'),
                    )

                    ReplayOutcome.SYNCED
                }
            }

            else -> {
                Timber.w("Unknown pending user-book write kind: ${entity.kind}; discarding")

                ReplayOutcome.DISCARDED
            }
        }
    }

    private enum class ReplayOutcome {
        SYNCED,
        DISCARDED,
    }
}
