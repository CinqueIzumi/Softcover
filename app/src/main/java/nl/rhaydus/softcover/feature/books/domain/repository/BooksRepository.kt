package nl.rhaydus.softcover.feature.books.domain.repository

import java.io.File
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection

interface BooksRepository {
    val books: Flow<List<Book>>

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    /** Library-screen path: all user books sorted via SQL `ORDER BY`. */
    fun getSortedAllUserBooks(mode: LibrarySortMode, direction: SortDirection): Flow<List<Book>>

    /** Library-screen path: user books for [status], sorted via SQL `ORDER BY`. */
    fun getSortedBooksByStatus(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>>

    /**
     * Rewrite the **prefix** of the manual ordering for [status] with [prefixBookIds]
     * (position = list index). Positions beyond `prefixBookIds.size` are intentionally left
     * intact so that a shallow drag at the top of the shelf doesn't pin books the user never
     * touched. Local-only — Hardcover does not model server-side per-shelf positions.
     */
    suspend fun applyShelfManualOrderPrefix(
        status: UserBookStatus,
        prefixBookIds: List<Int>,
    )

    /**
     * Re-fetches the user's books from the remote and reconciles them with the local cache.
     * When [statusFilter] is null, refreshes every status; otherwise refreshes only that status's
     * books. This is a books-only operation — list refresh is owned by [nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository].
     */
    suspend fun refreshUserBooks(
        userId: Int,
        statusFilter: UserBookStatus? = null,
    )

    /**
     * Deletes locally-cached books that have no user-book row and no list-book references.
     * Callers should invoke this after lists have been re-cached so list_book references are
     * accounted for when determining orphans.
     */
    suspend fun deleteOrphanBooks()

    suspend fun cacheBook(book: Book)

    suspend fun removeBook(book: Book)

    suspend fun removeAllBooks()

    suspend fun fetchBookById(id: Int): Book

    suspend fun fetchBooksByIds(ids: List<Int>): List<Book>

    suspend fun getEditionsByBookId(bookId: Int): List<BookEdition>

    suspend fun fetchEditionsByIds(ids: List<Int>): List<BookEdition>

    /**
     * Ensures the given books and editions exist in the local cache, fetching any missing ones
     * (or all of them when [forceNetwork] is true) from the remote in batches.
     *
     * This is the cross-feature hydration entry point: other features (e.g. lists) call it to
     * pre-populate the local books cache for the items they reference, so their own cache writes
     * resolve cleanly against the books table.
     */
    suspend fun hydrateReferencedBooks(
        bookIds: List<Int>,
        editionIds: List<Int>,
        forceNetwork: Boolean,
    )

    suspend fun markBookAsWantToRead(book: Book): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Book

    suspend fun markBookAsRead(book: Book): Book

    suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book

    suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    )
}