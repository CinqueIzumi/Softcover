package nl.rhaydus.softcover.feature.lists.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.data.database.dao.BookDao
import nl.rhaydus.softcover.feature.lists.data.mapper.toModel

interface ListsLocalDataSource {
    val allUserLists: Flow<List<BookList>>

    suspend fun cacheUserBookLists(lists: List<BookList>)

    suspend fun cacheListBook(book: ListBook)

    suspend fun findOwnedListBookByEditionId(editionId: Int): ListBook?

    suspend fun getOwnedListId(): Int?

    suspend fun removeOwnedListBookByEditionId(editionId: Int)

    suspend fun syncBookListMetadata(serverListIds: Set<Int>)
}

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

    override suspend fun getOwnedListId(): Int? {
        return dao.getOwnedListId()
    }

    override suspend fun removeOwnedListBookByEditionId(editionId: Int) {
        dao.deleteOwnedListBookByEditionId(editionId = editionId)
    }

    override suspend fun syncBookListMetadata(serverListIds: Set<Int>) {
        dao.syncBookListMetadata(serverListIds = serverListIds)
    }
}
