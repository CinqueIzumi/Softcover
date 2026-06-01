package nl.rhaydus.softcover.core.lists.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.data.database.dao.BookDao
import nl.rhaydus.softcover.core.data.database.mapper.toModel
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook

interface ListsLocalDataSource {
    val allUserLists: Flow<List<BookList>>

    suspend fun cacheUserBookLists(lists: List<BookList>)

    suspend fun cacheListBook(book: ListBook)

    suspend fun findOwnedListBookByEditionId(editionId: Int): ListBook?

    suspend fun findListBookByListAndBook(
        listId: Int,
        bookId: Int,
    ): ListBook?

    suspend fun getOwnedListId(): Int?

    suspend fun removeOwnedListBookByEditionId(editionId: Int)

    suspend fun removeListBookById(listBookId: Int)

    suspend fun removeOptimisticListBook(
        listId: Int,
        bookId: Int,
    )

    suspend fun syncBookListMetadata(serverListIds: Set<Int>)

    /**
     * Rewrites a contiguous slice of [listId]'s local `list_books.position` column to match
     * [orderedListBookIds] starting at [startPosition]. Mirrors the server mutation so the local
     * cache reflects the reorder before the next list refresh round-trips.
     */
    suspend fun applyListBookPositions(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    )

    suspend fun setListRanked(
        listId: Int,
        ranked: Boolean,
    )
}

private const val OPTIMISTIC_LIST_BOOK_ID: Int = 0

class ListsLocalDataSourceImpl(
    private val dao: BookDao,
) : ListsLocalDataSource {
    override val allUserLists: Flow<List<BookList>>
        get() = dao
            .observeBookLists()
            .distinctUntilChanged()
            .map { lists -> lists.map { it.toModel() } }

    override suspend fun cacheUserBookLists(lists: List<BookList>) {
        dao.cacheBookLists(lists = lists)
    }

    override suspend fun cacheListBook(book: ListBook) {
        dao.cacheListBook(listBook = book)
    }

    override suspend fun findOwnedListBookByEditionId(editionId: Int): ListBook? {
        return dao.getOwnedListBookByEditionId(editionId = editionId)?.toModel(isOwnedList = true)
    }

    override suspend fun findListBookByListAndBook(
        listId: Int,
        bookId: Int,
    ): ListBook? {
        return dao
            .getListBookByListAndBook(listId = listId, bookId = bookId)
            ?.toModel(isOwnedList = false)
    }

    override suspend fun getOwnedListId(): Int? {
        return dao.getOwnedListId()
    }

    override suspend fun removeOwnedListBookByEditionId(editionId: Int) {
        dao.deleteOwnedListBookByEditionId(editionId = editionId)
    }

    override suspend fun removeListBookById(listBookId: Int) {
        dao.deleteListBookById(listBookId = listBookId)
    }

    override suspend fun removeOptimisticListBook(
        listId: Int,
        bookId: Int,
    ) {
        dao.deleteListBookByComposite(
            listId = listId,
            bookId = bookId,
            listBookId = OPTIMISTIC_LIST_BOOK_ID,
        )
    }

    override suspend fun syncBookListMetadata(serverListIds: Set<Int>) {
        dao.syncBookListMetadata(serverListIds = serverListIds)
    }

    override suspend fun applyListBookPositions(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    ) {
        dao.applyListBookPositionRange(
            listId = listId,
            startPosition = startPosition,
            listBookIds = orderedListBookIds,
        )
    }

    override suspend fun setListRanked(
        listId: Int,
        ranked: Boolean,
    ) {
        dao.setBookListRanked(listId = listId, ranked = ranked)
    }
}
