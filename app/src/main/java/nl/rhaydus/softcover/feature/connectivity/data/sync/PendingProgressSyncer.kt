package nl.rhaydus.softcover.feature.connectivity.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressDrainer
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressUpdateKind
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingProgressUpdateDao
import nl.rhaydus.softcover.feature.connectivity.data.model.PendingProgressUpdateEntity
import timber.log.Timber

class PendingProgressSyncer(
    private val networkAvailability: NetworkAvailabilityProvider,
    private val dao: PendingProgressUpdateDao,
    private val booksRemoteDataSource: BooksRemoteDataSource,
) : PendingProgressDrainer {
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
                .onSuccess {
                    dao.delete(entity.localId)
                    recentlySyncedUserBookIds.add(entity.userBookId)
                }
                .onFailure { error ->
                    Timber.w(error, "Pending progress update ${entity.localId} failed; halting drain")
                    dao.incrementAttempts(entity.localId)
                    return
                }
        }
    }

    private suspend fun replay(entity: PendingProgressUpdateEntity) {
        when (entity.kind) {
            PendingProgressUpdateKind.UPDATE_PROGRESS.name -> booksRemoteDataSource.replayUpdateBookProgress(
                userBookReadId = entity.userBookReadId,
                editionId = entity.editionId,
                progressPages = entity.progressPages,
                progressSeconds = entity.progressSeconds,
                startedAt = entity.startedAt,
                finishedAt = entity.finishedAt,
            )

            PendingProgressUpdateKind.MARK_AS_READ.name -> booksRemoteDataSource.replayMarkBookAsRead(
                bookId = entity.bookId,
                userDate = entity.enqueuedAt.substringBefore('T'),
            )

            else -> Timber.w("Unknown pending progress update kind: ${entity.kind}")
        }
    }
}
