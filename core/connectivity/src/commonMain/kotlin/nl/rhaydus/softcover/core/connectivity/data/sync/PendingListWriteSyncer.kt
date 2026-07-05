package nl.rhaydus.softcover.core.connectivity.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingListWrite
import nl.rhaydus.softcover.core.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.database.model.PendingListWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException

private const val IN_DRAIN_RETRIES: Int = 3
private const val INITIAL_BACKOFF_MS: Long = 250L
private const val BACKOFF_MULTIPLIER: Long = 2L

internal class PendingListWriteSyncer(
    private val networkAvailability: NetworkAvailabilityProvider,
    private val dao: PendingListWriteDao,
    private val listsRemoteDataSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsLocalDataSource,
    private val appDispatchers: AppDispatchers,
) : ListWriteDrainer {
    private var job: Job? = null
    private val drainMutex: Mutex = Mutex()

    override fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return

        job = scope.launch(appDispatchers.io) {
            networkAvailability.isOnline
                .onStart { if (networkAvailability.isOnline.value) drainMutex.withLock { drain() } }
                .onEach { online -> if (online) drainMutex.withLock { drain() } }
                .collect()
        }
    }

    override suspend fun drainPendingWrites() {
        drainMutex.withLock { drain() }
    }

    private suspend fun drain() {
        val pending: List<PendingListWriteEntity> = runCatching { dao.getPending() }
            .getOrElse { emptyList() }

        for (entity in pending) {
            val write: PendingListWrite? = entity.toPendingListWrite()

            if (write == null) {
                AppLog.w("Unknown pending list write kind: ${entity.kind}")

                dao.incrementAttempts(entity.localId)

                continue
            }

            val replayed: Result<Unit> = replayWithBackoff(write = write)

            replayed
                .onSuccess { dao.delete(entity.localId) }
                .onFailure { error ->
                    AppLog.w(
                        error,
                        "Pending list write ${entity.localId} failed; halting drain",
                    )

                    dao.incrementAttempts(entity.localId)

                    return
                }
        }
    }

    private suspend fun replayWithBackoff(write: PendingListWrite): Result<Unit> {
        var backoff: Long = INITIAL_BACKOFF_MS
        var lastError: Throwable? = null

        repeat(IN_DRAIN_RETRIES) { attempt ->
            val outcome: Result<Unit> = runCatching { replay(write = write) }

            if (outcome.isSuccess) return outcome

            val error: Throwable = outcome.exceptionOrNull() ?: return outcome

            if (error is CancellationException) throw error

            lastError = error

            if (attempt < IN_DRAIN_RETRIES - 1) {
                delay(backoff)

                backoff *= BACKOFF_MULTIPLIER
            }
        }

        return Result.failure(lastError ?: Exception("Unknown replay failure"))
    }

    private suspend fun replay(write: PendingListWrite) {
        when (write.kind) {
            PendingListWriteKind.CREATE_LIST -> replayCreateList(write = write)
            PendingListWriteKind.ADD_LIST_BOOK -> replayAddListBook(write = write)
            PendingListWriteKind.REMOVE_LIST_BOOK -> replayRemoveListBook(write = write)
            PendingListWriteKind.REORDER_LIST_BOOKS -> replayReorderListBooks(write = write)
        }
    }

    private suspend fun replayCreateList(write: PendingListWrite) {
        val name: String = write.listName ?: error("CREATE_LIST replay missing listName")

        val created: BookList = listsRemoteDataSource.createList(name = name)

        listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
    }

    private suspend fun replayAddListBook(write: PendingListWrite) {
        val listId: Int = write.listId ?: error("ADD_LIST_BOOK replay missing listId")
        val bookId: Int = write.bookId ?: error("ADD_LIST_BOOK replay missing bookId")
        val editionId: Int = write.editionId ?: error("ADD_LIST_BOOK replay missing editionId")

        val real: ListBook = listsRemoteDataSource.addBookToList(
            listId = listId,
            bookId = bookId,
            editionId = editionId,
        )

        listsLocalDataSource.removeOptimisticListBook(
            listId = listId,
            bookId = bookId,
        )
        listsLocalDataSource.cacheListBook(book = real)
    }

    private suspend fun replayRemoveListBook(write: PendingListWrite) {
        val listId: Int = write.listId ?: error("REMOVE_LIST_BOOK replay missing listId")
        val listBookId: Int =
            write.listBookId ?: error("REMOVE_LIST_BOOK replay missing listBookId")
        val bookId: Int = write.bookId ?: error("REMOVE_LIST_BOOK replay missing bookId")
        val editionId: Int = write.editionId ?: error("REMOVE_LIST_BOOK replay missing editionId")

        val snapshot = ListBook(
            listBookId = listBookId,
            listId = listId,
            bookId = bookId,
            editionId = editionId,
        )

        val updatedList: BookList = listsRemoteDataSource.removeListBook(book = snapshot)

        listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    private suspend fun replayReorderListBooks(write: PendingListWrite) {
        val listId: Int = write.listId ?: error("REORDER_LIST_BOOKS replay missing listId")
        val startPosition: Int =
            write.startPosition ?: error("REORDER_LIST_BOOKS replay missing startPosition")
        val ordered: List<Int> = write.orderedListBookIds
            ?.takeIf { it.isNotEmpty() }
            ?: error("REORDER_LIST_BOOKS replay missing orderedListBookIds")

        listsRemoteDataSource.updateListBookPositions(
            listId = listId,
            startPosition = startPosition,
            orderedListBookIds = ordered,
        )
    }
}
