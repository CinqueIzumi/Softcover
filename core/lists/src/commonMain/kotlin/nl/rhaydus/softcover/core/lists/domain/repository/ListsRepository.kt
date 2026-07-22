package nl.rhaydus.softcover.core.lists.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.domain.model.ListsRefreshResult

interface ListsRepository {
    val allUserLists: Flow<List<BookList>>

    suspend fun createList(
        name: String,
        privacy: PrivacySetting = PrivacySetting.PUBLIC,
    ): BookList

    /**
     * Gated full refresh of the user's lists. Fetches a cheap per-list freshness signature for every
     * list, then deep-fetches the `list_books` only for lists whose signature advanced (or that are
     * new). The returned [ListsRefreshResult] carries the full server id set (for
     * [syncBookListMetadata]) plus only the changed lists, which the caller must hydrate and then
     * pass to [cacheUserBookLists] in that order so list-book inserts resolve against the books table.
     */
    suspend fun refreshUserLists(userId: Int): ListsRefreshResult

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

    /**
     * Persists a user-defined manual order for a contiguous slice of [listId]. [orderedListBookIds]
     * lists the `list_books` rows the user dragged through, in their new top-down order, starting
     * at visible index [startPosition]. Books outside the slice are left untouched so a shallow
     * drag at the top of the list doesn't reassign positions to books the user never moved.
     *
     * Optimistically applies the new positions to the local cache, then pushes the same slice to
     * Hardcover. On server failure the local change stands until the next list refresh reconciles —
     * matching the spec from Step 2.7 (Hardcover is authoritative, refresh resolves divergence).
     */
    suspend fun reorderListBooks(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    )

    /**
     * Toggles a custom list's `ranked` flag. Optimistically updates the local cache so the UI can
     * surface the ordered-list affordances immediately, then pushes the change to Hardcover. On
     * server failure the local flag is rolled back so the next refresh doesn't see two divergent
     * states racing each other.
     */
    suspend fun setListRanked(
        listId: Int,
        ranked: Boolean,
    )
}
