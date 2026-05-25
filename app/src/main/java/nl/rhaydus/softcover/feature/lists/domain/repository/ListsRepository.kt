package nl.rhaydus.softcover.feature.lists.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList

interface ListsRepository {
    val allUserLists: Flow<List<BookList>>

    suspend fun createList(name: String): BookList

    /**
     * Fetches user lists from the remote without writing to the local cache. Callers (typically
     * the refresh use case) are responsible for hydrating referenced books and then calling
     * [cacheUserBookLists] in the right order so list-book inserts resolve against the books table.
     */
    suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>? = null,
    ): List<BookList>

    suspend fun cacheUserBookLists(lists: List<BookList>)

    /**
     * Drops locally-cached lists whose ids are no longer present on the server.
     */
    suspend fun syncBookListMetadata(serverListIds: Set<Int>)

    suspend fun markEditionAsOwned(edition: BookEdition)

    suspend fun removeOwnedEdition(editionId: Int)

    suspend fun addBookToList(
        listId: Int,
        bookId: Int,
        edition: BookEdition,
    )

    suspend fun removeBookFromList(
        listId: Int,
        bookId: Int,
    )
}
