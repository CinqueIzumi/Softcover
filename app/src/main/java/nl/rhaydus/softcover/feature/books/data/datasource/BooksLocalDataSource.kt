package nl.rhaydus.softcover.feature.books.data.datasource

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.data.database.dao.BookDao
import nl.rhaydus.softcover.core.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.mapper.toModel
import nl.rhaydus.softcover.feature.books.data.sort.toOrderByFragment
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection
import java.io.File

interface BooksLocalDataSource {
    val allUserBooks: Flow<List<Book>>

    /**
     * Rewrite the **prefix** of the manual ordering for [status] with [prefixBookIds]
     * (position = index). Positions beyond `prefixBookIds.size` are intentionally left intact so
     * that a shallow drag at the top of the shelf doesn't pin books the user never touched. An
     * empty list clears positions `0` (i.e. no-op).
     */
    suspend fun applyShelfManualOrderPrefix(
        status: UserBookStatus,
        prefixBookIds: List<Int>,
    )

    /**
     * Library-screen path: returns ALL user books sorted by [mode] + [direction], performed
     * by the database via `ORDER BY`. The flow re-emits whenever the underlying tables change
     * — and whenever the caller passes new sort params (rebuilt query, fresh subscription).
     */
    fun getSortedAllUserBooks(
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>>

    /**
     * Library-screen path: returns user books in [status] sorted by [mode] + [direction]. See
     * [getSortedAllUserBooks] for semantics.
     */
    fun getSortedBooksByStatus(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>>

    suspend fun getAllUserBookIds(): List<Int>

    suspend fun getUserBookIdsByStatus(status: UserBookStatus): List<Int>

    suspend fun getExistingBookIds(ids: List<Int>): List<Int>

    suspend fun getExistingEditionIds(ids: List<Int>): List<Int>

    suspend fun cacheEditions(editions: List<BookEdition>)

    suspend fun updateEditionLocalImagePath(
        editionId: Int,
        path: String?,
    )

    suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    )

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)

    suspend fun removeUserBooksById(ids: List<Int>)

    suspend fun removeAllBooks()

    suspend fun deleteOrphanBooks()

    suspend fun getBookById(id: Int): Book?

    suspend fun redirectBookId(oldId: Int, newId: Int)
}

class BooksLocalDataSourceImpl(
    private val dao: BookDao,
    private val editionImageStorage: EditionImageStorage,
) : BooksLocalDataSource {
    override val allUserBooks: Flow<List<Book>>
        get() = dao
            .observeBooks()
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }

    override suspend fun getAllUserBookIds(): List<Int> {
        return dao.getAllUserBookIds()
    }

    override suspend fun getUserBookIdsByStatus(status: UserBookStatus): List<Int> {
        return dao.getUserBookIdsByStatus(statusCode = status.code)
    }

    override suspend fun getExistingBookIds(ids: List<Int>): List<Int> {
        if (ids.isEmpty()) return emptyList()

        return dao.getExistingBookIds(bookIds = ids)
    }

    override suspend fun getExistingEditionIds(ids: List<Int>): List<Int> {
        if (ids.isEmpty()) return emptyList()

        return dao.getExistingEditionIds(editionIds = ids)
    }

    override suspend fun cacheEditions(editions: List<BookEdition>) {
        dao.cacheEditions(editions = editions)
    }

    override suspend fun updateEditionLocalImagePath(
        editionId: Int,
        path: String?,
    ) {
        dao.updateEditionLocalImagePath(editionId = editionId, path = path)
    }

    override suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    ) {
        if (editionImageStorage.exists(editionId = editionId)) return

        val storedPath = editionImageStorage.copyFrom(editionId = editionId, source = source)

        dao.updateEditionLocalImagePath(editionId = editionId, path = storedPath)
    }

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return when (status) {
            UserBookStatus.CURRENTLY_READING -> dao.getBooksByStatusAndEvents(
                statusCode = status.code,
                events = listOf("progress_updated", "user_book_read_started"),
            )
            UserBookStatus.READ -> dao.getReadBooks(statusCode = status.code)
            UserBookStatus.WANT_TO_READ -> dao.getBooksByStatusSortedByCreatedAt(
                statusCode = status.code,
            )
            UserBookStatus.DID_NOT_FINISH -> dao.getBooksByStatusAndEvents(
                statusCode = status.code,
                events = listOf("status_stopped"),
            )
        }
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }
    }

    override fun getSortedAllUserBooks(
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> {
        // MANUAL is a per-shelf concept (positions are keyed by statusCode), so it has no
        // meaning on the All tab. The sort dropdown excludes MANUAL on All; this fallback
        // protects the path against a stale persisted setting reaching here from a future code
        // change and crashing the collector via toOrderByFragment.
        if (mode == LibrarySortMode.MANUAL) {
            return getSortedAllUserBooks(
                mode = LibrarySortMode.Default,
                direction = LibrarySortMode.Default.defaultDirection,
            )
        }

        val orderBy = mode.toOrderByFragment(direction = direction)
        val sql = """
            SELECT b.*
            FROM books b
            LEFT JOIN user_books ub ON ub.bookId = b.id
            ORDER BY $orderBy, b.id DESC
        """.trimIndent()

        return dao.observeBooksRaw(query = SimpleSQLiteQuery(sql))
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }
    }

    override fun getSortedBooksByStatus(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> {
        // MANUAL needs a LEFT JOIN against shelf_manual_order keyed by the same statusCode the
        // shelf is filtered on; books without a row sort last (and tie-break on ub.id DESC, so
        // newcomers appear at the bottom in the order they were shelved). Direction is ignored
        // because position itself is the order — flipping would just mirror the shelf.
        val sql = if (mode == LibrarySortMode.MANUAL) {
            """
                SELECT b.*
                FROM books b
                INNER JOIN user_books ub ON ub.bookId = b.id
                LEFT JOIN shelf_manual_order smo
                    ON smo.bookId = b.id AND smo.statusCode = ?
                WHERE ub.statusCode = ?
                ORDER BY
                    (smo.position IS NULL) ASC,
                    smo.position ASC,
                    ub.id DESC
            """.trimIndent()
        } else {
            val orderBy = mode.toOrderByFragment(direction = direction)
            """
                SELECT b.*
                FROM books b
                INNER JOIN user_books ub ON ub.bookId = b.id
                WHERE ub.statusCode = ?
                ORDER BY $orderBy, ub.id DESC
            """.trimIndent()
        }

        val args: Array<Any> = if (mode == LibrarySortMode.MANUAL) {
            arrayOf(status.code, status.code)
        } else {
            arrayOf(status.code)
        }

        return dao.observeBooksRaw(query = SimpleSQLiteQuery(sql, args))
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }
    }

    override suspend fun applyShelfManualOrderPrefix(
        status: UserBookStatus,
        prefixBookIds: List<Int>,
    ) {
        dao.applyShelfManualOrderPrefix(
            statusCode = status.code,
            prefixBookIds = prefixBookIds,
        )
    }

    override suspend fun cacheBook(book: Book) {
        dao.cacheBook(book = book)
    }

    override suspend fun getBookById(id: Int): Book? {
        return dao.getBookById(id = id)?.toModel()
    }

    override suspend fun cacheBooks(books: List<Book>) {
        dao.cacheBooks(books = books)
    }

    override suspend fun removeUserBooksById(ids: List<Int>) {
        val bookIds = ids.mapNotNull { dao.getBookIdByUserBookId(userBookId = it) }
        val pathsToDelete = bookIds.flatMap { bookId ->
            dao.getLocalImagePathsByBookId(bookId = bookId).mapNotNull { it.localImagePath }
        }

        dao.deleteUserBooksByIds(ids)

        if (bookIds.isNotEmpty()) {
            dao.deleteShelfManualOrderForBookIds(bookIds = bookIds)
        }

        pathsToDelete.forEach { editionImageStorage.delete(path = it) }
    }

    override suspend fun removeAllBooks() {
        val pathsToDelete = dao.getAllLocalImagePaths().mapNotNull { it.localImagePath }

        dao.deleteAllUserBooksAndData()

        pathsToDelete.forEach { editionImageStorage.delete(path = it) }
    }

    override suspend fun deleteOrphanBooks() {
        dao.deleteOrphanBooks()
    }

    override suspend fun redirectBookId(oldId: Int, newId: Int) {
        dao.redirectBookId(oldId = oldId, newId = newId)
    }
}