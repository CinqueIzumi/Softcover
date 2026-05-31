package nl.rhaydus.softcover.feature.lists.data.repository

import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import nl.rhaydus.softcover.feature.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository

class ListsRepositoryImpl(
    private val listsRemoteDataSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsLocalDataSource,
    private val booksRepository: BooksRepository,
    private val applicationScope: ApplicationScope,
    private val listWriteQueue: ListWriteQueue,
    private val listWriteDrainer: ListWriteDrainer,
) : ListsRepository {
    override val allUserLists: Flow<List<BookList>> = listsLocalDataSource.allUserLists

    private val inflightMutex = Mutex()
    private val inflightFetches = mutableMapOf<Set<Int>?, Deferred<List<BookList>>>()

    override suspend fun createList(name: String): BookList {
        val created: BookList = runCatching {
            listsRemoteDataSource.createList(name = name)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            if (error is ListNameTakenException) throw error

            enqueueCreateList(name = name)

            throw error
        }

        listsLocalDataSource.cacheUserBookLists(lists = listOf(created))

        return created
    }

    override suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>?,
    ): List<BookList> {
        listWriteDrainer.drainPendingWrites()

        val deferred: Deferred<List<BookList>> = inflightMutex.withLock {
            inflightFetches[listIds]?.let { return@withLock it }

            val started: Deferred<List<BookList>> = applicationScope.scope.async {
                try {
                    listsRemoteDataSource.fetchUserLists(userId = userId, listIds = listIds)
                } finally {
                    inflightMutex.withLock { inflightFetches.remove(listIds) }
                }
            }

            inflightFetches[listIds] = started

            started
        }

        return deferred.await()
    }

    override suspend fun cacheUserBookLists(lists: List<BookList>) {
        listsLocalDataSource.cacheUserBookLists(lists = lists)
    }

    override suspend fun syncBookListMetadata(serverListIds: Set<Int>) {
        listsLocalDataSource.syncBookListMetadata(serverListIds = serverListIds)
    }

    override suspend fun markEditionAsOwned(edition: BookEdition) {
        val snapshot: ListBook? = listsLocalDataSource.findOwnedListBookByEditionId(
            editionId = edition.id,
        )

        val ownedListId: Int? = listsLocalDataSource.getOwnedListId()

        if (ownedListId != null) {
            listsLocalDataSource.cacheListBook(
                book = ListBook(
                    listBookId = OPTIMISTIC_LIST_BOOK_ID,
                    listId = ownedListId,
                    bookId = edition.bookId,
                    editionId = edition.id,
                ),
            )
        }

        hydrateReferencedBook(bookId = edition.bookId, editionId = edition.id)

        val real: ListBook = runCatching {
            listsRemoteDataSource.markEditionAsOwned(edition = edition)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            restoreOwnedListBook(
                editionId = edition.id,
                snapshot = snapshot,
            )
            throw error
        }

        listsLocalDataSource.removeOwnedListBookByEditionId(editionId = edition.id)
        listsLocalDataSource.cacheListBook(book = real)
    }

    override suspend fun addBookToList(
        listId: Int,
        bookId: Int,
        edition: BookEdition,
    ) {
        listsLocalDataSource.cacheListBook(
            book = ListBook(
                listBookId = OPTIMISTIC_LIST_BOOK_ID,
                listId = listId,
                bookId = bookId,
                editionId = edition.id,
            ),
        )

        hydrateReferencedBook(bookId = bookId, editionId = edition.id)

        val real: ListBook = runCatching {
            listsRemoteDataSource.addBookToList(
                listId = listId,
                bookId = bookId,
                editionId = edition.id,
            )
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            enqueueAddListBook(
                listId = listId,
                bookId = bookId,
                editionId = edition.id,
            )

            throw error
        }

        listsLocalDataSource.removeOptimisticListBook(
            listId = listId,
            bookId = bookId,
        )
        listsLocalDataSource.cacheListBook(book = real)
    }

    override suspend fun removeBookFromList(
        listId: Int,
        bookId: Int,
    ) {
        val snapshot: ListBook = listsLocalDataSource.findListBookByListAndBook(
            listId = listId,
            bookId = bookId,
        ) ?: return

        listsLocalDataSource.removeListBookById(listBookId = snapshot.listBookId)

        val updatedList: BookList = runCatching {
            listsRemoteDataSource.removeListBook(book = snapshot)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            enqueueRemoveListBook(snapshot = snapshot)

            throw error
        }

        listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    override suspend fun removeOwnedEdition(editionId: Int) {
        val snapshot: ListBook = listsLocalDataSource.findOwnedListBookByEditionId(
            editionId = editionId,
        ) ?: return

        listsLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)

        val updatedList: BookList = runCatching {
            listsRemoteDataSource.removeListBook(book = snapshot)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            enqueueRemoveListBook(snapshot = snapshot)

            throw error
        }

        listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    private suspend fun restoreOwnedListBook(
        editionId: Int,
        snapshot: ListBook?,
    ) {
        listsLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)

        if (snapshot != null && snapshot.listBookId != OPTIMISTIC_LIST_BOOK_ID) {
            listsLocalDataSource.cacheListBook(book = snapshot)
        }
    }

    /**
     * Ensures the book + edition a freshly-written `list_book` references are cached locally. The
     * `BookListWithBooks` join drops list-books whose book/edition aren't cached, so without this an
     * off-shelf reference (e.g. a scanned edition just marked owned) would only surface after a
     * library refresh re-hydrates it. Best-effort: hydration failure (e.g. offline) is non-fatal —
     * the next refresh fills the gap — so it never blocks the optimistic list write.
     */
    private suspend fun hydrateReferencedBook(
        bookId: Int,
        editionId: Int,
    ) {
        runCatching {
            booksRepository.hydrateReferencedBooks(
                bookIds = listOf(bookId),
                editionIds = listOf(editionId),
                forceNetwork = false,
            )
        }.onFailure { error ->
            if (error is CancellationException) throw error
        }
    }

    override suspend fun reorderListBooks(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    ) {
        if (orderedListBookIds.isEmpty()) return

        listsLocalDataSource.applyListBookPositions(
            listId = listId,
            startPosition = startPosition,
            orderedListBookIds = orderedListBookIds,
        )

        runCatching {
            listsRemoteDataSource.updateListBookPositions(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedListBookIds,
            )
        }.onFailure { error ->
            if (error is CancellationException) throw error

            enqueueReorderListBooks(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedListBookIds,
            )

            throw error
        }
    }

    override suspend fun setListRanked(
        listId: Int,
        ranked: Boolean,
    ) {
        listsLocalDataSource.setListRanked(listId = listId, ranked = ranked)

        val refreshed: BookList = runCatching {
            listsRemoteDataSource.setListRanked(listId = listId, ranked = ranked)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            listsLocalDataSource.setListRanked(listId = listId, ranked = ranked.not())

            throw error
        }

        listsLocalDataSource.cacheUserBookLists(lists = listOf(refreshed))
    }

    private suspend fun enqueueCreateList(name: String) {
        runCatching {
            listWriteQueue.enqueue(
                PendingListWrite(
                    kind = PendingListWriteKind.CREATE_LIST,
                    listId = null,
                    listName = name,
                    bookId = null,
                    editionId = null,
                    listBookId = null,
                    startPosition = null,
                    orderedListBookIds = null,
                    enqueuedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private suspend fun enqueueAddListBook(
        listId: Int,
        bookId: Int,
        editionId: Int,
    ) {
        runCatching {
            listWriteQueue.enqueue(
                PendingListWrite(
                    kind = PendingListWriteKind.ADD_LIST_BOOK,
                    listId = listId,
                    listName = null,
                    bookId = bookId,
                    editionId = editionId,
                    listBookId = null,
                    startPosition = null,
                    orderedListBookIds = null,
                    enqueuedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private suspend fun enqueueRemoveListBook(snapshot: ListBook) {
        runCatching {
            listWriteQueue.enqueue(
                PendingListWrite(
                    kind = PendingListWriteKind.REMOVE_LIST_BOOK,
                    listId = snapshot.listId,
                    listName = null,
                    bookId = snapshot.bookId,
                    editionId = snapshot.editionId,
                    listBookId = snapshot.listBookId,
                    startPosition = null,
                    orderedListBookIds = null,
                    enqueuedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private suspend fun enqueueReorderListBooks(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    ) {
        runCatching {
            listWriteQueue.enqueue(
                PendingListWrite(
                    kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                    listId = listId,
                    listName = null,
                    bookId = null,
                    editionId = null,
                    listBookId = null,
                    startPosition = startPosition,
                    orderedListBookIds = orderedListBookIds,
                    enqueuedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private companion object {
        const val OPTIMISTIC_LIST_BOOK_ID = 0
    }
}
