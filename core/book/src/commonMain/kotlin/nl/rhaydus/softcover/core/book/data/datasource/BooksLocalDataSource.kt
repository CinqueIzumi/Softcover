package nl.rhaydus.softcover.core.book.data.datasource

import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.data.mapper.toStorageKey
import nl.rhaydus.softcover.core.book.data.sort.toOrderByFragment
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.database.dao.BookDao
import nl.rhaydus.softcover.core.database.mapper.toModel
import nl.rhaydus.softcover.core.database.model.BookFullEntity
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import kotlin.time.Instant

interface BooksLocalDataSource {
    val allUserBooks: Flow<List<Book>>

    fun observeUserBook(id: Int): Flow<Book?>

    fun observeTrendingBooks(period: TrendingPeriod): Flow<List<Book>>

    fun observeTrendingFetchedAt(period: TrendingPeriod): Flow<Instant?>

    suspend fun getTrendingFetchedAt(period: TrendingPeriod): Instant?

    fun observeUpcomingBooks(window: UpcomingWindow): Flow<List<Book>>

    suspend fun getUpcomingBooks(window: UpcomingWindow): List<Book>

    fun observeUpcomingFetchedAt(window: UpcomingWindow): Flow<Instant?>

    suspend fun getUpcomingFetchedAt(window: UpcomingWindow): Instant?

    suspend fun replaceUpcomingBooks(
        window: UpcomingWindow,
        requestedIds: List<Int>,
        books: List<Book>,
        fetchedAt: Instant,
    )

    suspend fun getExistingUserBookBookIds(ids: List<Int>): List<Int>

    suspend fun replaceTrendingBooks(
        period: TrendingPeriod,
        requestedIds: List<Int>,
        books: List<Book>,
        fetchedAt: Instant,
    )

    fun observeBooksByIds(ids: List<Int>): Flow<List<Book>>

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

    suspend fun getUserBookIdsByStatuses(statuses: Set<UserBookStatus>): List<Int>

    suspend fun getExistingBookIds(ids: List<Int>): List<Int>

    suspend fun getExistingEditionIds(ids: List<Int>): List<Int>

    suspend fun cacheEditions(editions: List<BookEdition>)

    suspend fun updateEditionLocalImage(
        editionId: Int,
        path: String?,
        url: String?,
    )

    suspend fun persistEditionImage(
        editionId: Int,
        url: String?,
        bytes: ByteArray,
    )

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)

    suspend fun removeUserBooksById(ids: List<Int>)

    suspend fun removeAllBooks()

    suspend fun deleteOrphanBooks()

    suspend fun getBookById(id: Int): Book?

    suspend fun redirectBookId(
        oldId: Int,
        newId: Int,
    )
}

internal class BooksLocalDataSourceImpl(
    private val dao: BookDao,
    private val editionImageStorage: EditionImageStorage,
    private val appDispatchers: AppDispatchers,
) : BooksLocalDataSource {
    override val allUserBooks: Flow<List<Book>>
        get() = dao
            .observeBooks()
            .distinctUntilChanged()
            .toModelsReusingUnchanged()

    override fun observeTrendingBooks(period: TrendingPeriod): Flow<List<Book>> = dao
        .observeTrendingBooks(period = period.toStorageKey())
        .distinctUntilChanged()
        .toModelsReusingUnchanged()

    override fun observeTrendingFetchedAt(period: TrendingPeriod): Flow<Instant?> = dao
        .observeTrendingFetchedAt(period = period.toStorageKey())
        .map { millis -> millis?.let(Instant::fromEpochMilliseconds) }
        .distinctUntilChanged()

    override suspend fun getTrendingFetchedAt(period: TrendingPeriod): Instant? = dao
        .getTrendingFetchedAt(period = period.toStorageKey())
        ?.let(Instant::fromEpochMilliseconds)

    override fun observeUpcomingBooks(window: UpcomingWindow): Flow<List<Book>> = dao
        .observeTrendingBooks(period = window.toStorageKey())
        .distinctUntilChanged()
        .toModelsReusingUnchanged()

    override suspend fun getUpcomingBooks(window: UpcomingWindow): List<Book> = dao
        .getTrendingBooks(period = window.toStorageKey())
        .map { entity -> entity.toModel() }

    override fun observeUpcomingFetchedAt(window: UpcomingWindow): Flow<Instant?> = dao
        .observeTrendingFetchedAt(period = window.toStorageKey())
        .map { millis -> millis?.let(Instant::fromEpochMilliseconds) }
        .distinctUntilChanged()

    override suspend fun getUpcomingFetchedAt(window: UpcomingWindow): Instant? = dao
        .getTrendingFetchedAt(period = window.toStorageKey())
        ?.let(Instant::fromEpochMilliseconds)

    override suspend fun replaceUpcomingBooks(
        window: UpcomingWindow,
        requestedIds: List<Int>,
        books: List<Book>,
        fetchedAt: Instant,
    ) {
        deleteStaleCovers(
            dao.replaceTrendingBooks(
                period = window.toStorageKey(),
                requestedIds = requestedIds,
                books = books,
                fetchedAtEpochMillis = fetchedAt.toEpochMilliseconds(),
            ),
        )
    }

    override suspend fun getExistingUserBookBookIds(ids: List<Int>): List<Int> {
        if (ids.isEmpty()) return emptyList()

        return dao.getBookIdsWithUserBook(bookIds = ids)
    }

    override suspend fun replaceTrendingBooks(
        period: TrendingPeriod,
        requestedIds: List<Int>,
        books: List<Book>,
        fetchedAt: Instant,
    ) {
        deleteStaleCovers(
            dao.replaceTrendingBooks(
                period = period.toStorageKey(),
                requestedIds = requestedIds,
                books = books,
                fetchedAtEpochMillis = fetchedAt.toEpochMilliseconds(),
            ),
        )
    }

    override fun observeUserBook(id: Int): Flow<Book?> = dao
        .observeBookById(id = id)
        .distinctUntilChanged()
        .map { entity -> entity?.toModel() }
        .distinctUntilChanged()
        .flowOn(appDispatchers.default)

    override fun observeBooksByIds(ids: List<Int>): Flow<List<Book>> = dao
        .observeBooksByIds(ids = ids)
        .distinctUntilChanged()
        .toModelsReusingUnchanged()
        .distinctUntilChanged()

    override suspend fun getAllUserBookIds(): List<Int> {
        return dao.getAllUserBookIds()
    }

    override suspend fun getUserBookIdsByStatuses(statuses: Set<UserBookStatus>): List<Int> {
        return dao.getUserBookIdsByStatuses(statusCodes = statuses.map { it.code })
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
        deleteStaleCovers(dao.cacheEditions(editions = editions))
    }

    override suspend fun updateEditionLocalImage(
        editionId: Int,
        path: String?,
        url: String?,
    ) {
        dao.updateEditionLocalImage(
            editionId = editionId,
            path = path,
            url = url,
        )
    }

    override suspend fun persistEditionImage(
        editionId: Int,
        url: String?,
        bytes: ByteArray,
    ) {
        // Always (over)write: the resolver only loads from the URL when no valid local cover exists
        // (a stale file is evicted by the cache transaction first), so reaching here means the file
        // is missing or out of date — and recording [url] is what lets a later URL change be detected.
        val storedPath = editionImageStorage.write(
            editionId = editionId,
            bytes = bytes,
        )

        dao.updateEditionLocalImage(
            editionId = editionId,
            path = storedPath,
            url = url,
        )
    }

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return when (status) {
            UserBookStatus.CURRENTLY_READING,
            UserBookStatus.PAUSED,
            -> dao.getBooksByStatusAndEvents(
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
            .toModelsReusingUnchanged()
    }

    override fun getSortedAllUserBooks(
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> {
        // MANUAL is a per-shelf concept (positions are keyed by statusCode); ORDER is custom-list
        // only (positions live on list_books). Neither has meaning on the All tab — the sort
        // dropdown excludes both — but the fallback protects this path against a stale persisted
        // setting reaching here from a future code change and crashing the collector via
        // toOrderByFragment.
        if (mode == LibrarySortMode.MANUAL || mode == LibrarySortMode.ORDER) {
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

        return dao.observeBooksRaw(query = RoomRawQuery(sql = sql))
            .distinctUntilChanged()
            .toModelsReusingUnchanged()
    }

    override fun getSortedBooksByStatus(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> {
        // ORDER is custom-list-only; it has no meaning on a built-in shelf. The dropdown excludes
        // it, but this fallback protects the path against a stale persisted setting reaching here
        // and crashing the collector via toOrderByFragment.
        if (mode == LibrarySortMode.ORDER) {
            return getSortedBooksByStatus(
                status = status,
                mode = LibrarySortMode.Default,
                direction = LibrarySortMode.Default.defaultDirection,
            )
        }

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

        val bindArgs: List<Int> = if (mode == LibrarySortMode.MANUAL) {
            listOf(status.code, status.code)
        } else {
            listOf(status.code)
        }

        return dao.observeBooksRaw(
            query = RoomRawQuery(sql = sql) { statement ->
                bindArgs.forEachIndexed { index, arg ->
                    statement.bindLong(
                        index + 1,
                        arg.toLong(),
                    )
                }
            },
        )
            .distinctUntilChanged()
            .toModelsReusingUnchanged()
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
        deleteStaleCovers(dao.cacheBook(book = book))
    }

    override suspend fun getBookById(id: Int): Book? {
        return dao.getBookById(id = id)?.toModel()
    }

    override suspend fun cacheBooks(books: List<Book>) {
        deleteStaleCovers(dao.cacheBooks(books = books))
    }

    override suspend fun removeUserBooksById(ids: List<Int>) {
        val bookIds = ids.mapNotNull { dao.getBookIdByUserBookId(userBookId = it) }
        val pathsToDelete = bookIds.flatMap { bookId ->
            dao.getEditionImageRefsByBookId(bookId = bookId).mapNotNull { it.localImagePath }
        }

        dao.deleteUserBooksByIds(ids)

        if (bookIds.isNotEmpty()) {
            dao.deleteShelfManualOrderForBookIds(bookIds = bookIds)
        }

        pathsToDelete.forEach { editionImageStorage.delete(path = it) }
    }

    override suspend fun removeAllBooks() {
        val pathsToDelete = dao.getAllEditionImageRefs().mapNotNull { it.localImagePath }

        dao.deleteAllLocalData()

        pathsToDelete.forEach { editionImageStorage.delete(path = it) }
    }

    private fun deleteStaleCovers(stalePaths: List<String>) {
        stalePaths.forEach { editionImageStorage.delete(path = it) }
    }

    override suspend fun deleteOrphanBooks() {
        dao.deleteOrphanBooks()
    }

    override suspend fun redirectBookId(
        oldId: Int,
        newId: Int,
    ) {
        dao.redirectBookId(
            oldId = oldId,
            newId = newId,
        )
    }

    /**
     * Maps each emission to models, reusing the previous [Book] instance for every row whose
     * entity is unchanged. Downstream `distinctUntilChanged` comparisons then short-circuit on
     * identity instead of walking every field of every book. The previous-emission state is
     * created per collection, so subscribers never share it.
     */
    private fun Flow<List<BookFullEntity>>.toModelsReusingUnchanged(): Flow<List<Book>> {
        val upstream = this

        return flow {
            var previous: Map<Int, Pair<BookFullEntity, Book>> = emptyMap()

            upstream.collect { entities ->
                val next = HashMap<Int, Pair<BookFullEntity, Book>>(entities.size)
                val models = entities.map { entity ->
                    val id = entity.book.id
                    val prior = previous[id]
                    val model = if (prior != null && prior.first == entity) {
                        prior.second
                    } else {
                        entity.toModel()
                    }
                    next[id] = entity to model
                    model
                }

                previous = next
                emit(models)
            }
        }.flowOn(appDispatchers.default)
    }
}
