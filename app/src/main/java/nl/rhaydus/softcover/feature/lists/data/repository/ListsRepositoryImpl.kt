package nl.rhaydus.softcover.feature.lists.data.repository

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository

class ListsRepositoryImpl(
    private val listsRemoteDataSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsLocalDataSource,
    private val applicationScope: ApplicationScope,
) : ListsRepository {
    override val allUserLists: Flow<List<BookList>> = listsLocalDataSource.allUserLists

    private val inflightMutex = Mutex()
    private val inflightFetches = mutableMapOf<Set<Int>?, Deferred<List<BookList>>>()

    override suspend fun createList(name: String): BookList {
        val created: BookList = listsRemoteDataSource.createList(name = name)

        listsLocalDataSource.cacheUserBookLists(lists = listOf(created))

        return created
    }

    override suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>?,
    ): List<BookList> {
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
        val snapshot: ListBook? = listsLocalDataSource.findListBookByListAndBook(
            listId = listId,
            bookId = bookId,
        )

        listsLocalDataSource.cacheListBook(
            book = ListBook(
                listBookId = OPTIMISTIC_LIST_BOOK_ID,
                listId = listId,
                bookId = bookId,
                editionId = edition.id,
            ),
        )

        val real: ListBook = runCatching {
            listsRemoteDataSource.addBookToList(
                listId = listId,
                bookId = bookId,
                editionId = edition.id,
            )
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            restoreOptimisticListBook(
                listId = listId,
                bookId = bookId,
                snapshot = snapshot,
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

            listsLocalDataSource.cacheListBook(book = snapshot)
            throw error
        }

        listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    private suspend fun restoreOptimisticListBook(
        listId: Int,
        bookId: Int,
        snapshot: ListBook?,
    ) {
        listsLocalDataSource.removeOptimisticListBook(
            listId = listId,
            bookId = bookId,
        )

        if (snapshot != null && snapshot.listBookId != OPTIMISTIC_LIST_BOOK_ID) {
            listsLocalDataSource.cacheListBook(book = snapshot)
        }
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

            listsLocalDataSource.cacheListBook(book = snapshot)
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

    private companion object {
        const val OPTIMISTIC_LIST_BOOK_ID = 0
    }
}
