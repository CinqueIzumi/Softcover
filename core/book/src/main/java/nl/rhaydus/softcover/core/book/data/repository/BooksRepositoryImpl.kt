package nl.rhaydus.softcover.core.book.data.repository

import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.book.data.datasource.BookNotFoundException
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.domain.model.CreatedBook
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.JournalEventType
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.isBlank
import timber.log.Timber

private const val TRENDING_LIMIT = 10
private const val TRENDING_OFFSET = 0
private const val TRENDING_WINDOW_DAYS = 7L

class BooksRepositoryImpl(
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val booksLocalDataSource: BooksLocalDataSource,
    private val networkAvailability: NetworkAvailabilityProvider,
    private val userBookWriteQueue: UserBookWriteQueue,
    private val userBookWriteDrainer: UserBookWriteDrainer,
    private val applicationScope: ApplicationScope,
) : BooksRepository {
    override val books: Flow<List<Book>> = booksLocalDataSource.allUserBooks

    private val inflightMutex = Mutex()
    private val inflightRefreshes = mutableMapOf<UserBookStatus?, Deferred<Unit>>()

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return booksLocalDataSource.getBooksFlowByStatus(status = status)
    }

    override fun getSortedAllUserBooks(
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> = booksLocalDataSource.getSortedAllUserBooks(
        mode = mode,
        direction = direction,
    )

    override fun getSortedBooksByStatus(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> = booksLocalDataSource.getSortedBooksByStatus(
        status = status,
        mode = mode,
        direction = direction,
    )

    override suspend fun applyShelfManualOrderPrefix(
        status: UserBookStatus,
        prefixBookIds: List<Int>,
    ) {
        booksLocalDataSource.applyShelfManualOrderPrefix(
            status = status,
            prefixBookIds = prefixBookIds,
        )
    }

    override suspend fun refreshUserBooks(
        userId: Int,
        statusFilter: UserBookStatus?,
    ) {
        runOrJoin(statusFilter = statusFilter) {
            withContext(Dispatchers.IO) {
                refreshUserBooksInternal(
                    userId = userId,
                    statusFilter = statusFilter,
                )
            }
        }
    }

    private suspend fun runOrJoin(
        statusFilter: UserBookStatus?,
        work: suspend () -> Unit,
    ) {
        val deferred: Deferred<Unit> = inflightMutex.withLock {
            inflightRefreshes[statusFilter]?.let { return@withLock it }

            val started: Deferred<Unit> = applicationScope.scope.async {
                try {
                    work()
                } finally {
                    inflightMutex.withLock { inflightRefreshes.remove(statusFilter) }
                }
            }

            inflightRefreshes[statusFilter] = started

            started
        }

        deferred.await()
    }

    private suspend fun refreshUserBooksInternal(
        userId: Int,
        statusFilter: UserBookStatus?,
    ) {
        val syncedUserBookIds: Set<Int> = userBookWriteDrainer.drainPendingUpdates()

        val statusIds: Set<Int> = statusFilter
            ?.let { setOf(it.code) }
            ?: UserBookStatus.entries.map { it.code }.toSet()

        val fetchedBooks = booksRemoteDataSource.initializeBooks(
            userId = userId,
            statusIds = statusIds,
        )

        val booksToCache: List<Book> = preserveSyncedProgress(
            fetchedBooks = fetchedBooks,
            syncedUserBookIds = syncedUserBookIds,
        )

        booksLocalDataSource.cacheBooks(books = booksToCache)

        val fetchedBookUserBookIds: Set<Int> = fetchedBooks.mapNotNull { it.userBook?.id }.toSet()
        val locallyStoredUserBookIds: List<Int> = if (statusFilter == null) {
            booksLocalDataSource.getAllUserBookIds()
        } else {
            booksLocalDataSource.getUserBookIdsByStatus(status = statusFilter)
        }

        val userBookIdsToRemove: List<Int> = locallyStoredUserBookIds
            .filterNot { it in fetchedBookUserBookIds }

        booksLocalDataSource.removeUserBooksById(ids = userBookIdsToRemove)
    }

    override suspend fun deleteOrphanBooks() {
        booksLocalDataSource.deleteOrphanBooks()
    }

    override suspend fun hydrateReferencedBooks(
        bookIds: List<Int>,
        editionIds: List<Int>,
        forceNetwork: Boolean,
    ) {
        if (bookIds.isEmpty() && editionIds.isEmpty()) return

        val distinctBookIds = bookIds.distinct()
        val bookIdsToFetch: List<Int> = if (forceNetwork) {
            distinctBookIds
        } else {
            val cachedBookIds = booksLocalDataSource.getExistingBookIds(ids = distinctBookIds).toSet()
            distinctBookIds.filterNot { it in cachedBookIds }
        }

        if (bookIdsToFetch.isNotEmpty()) {
            val fetchedBooks = booksRemoteDataSource.fetchBooksByIds(
                ids = bookIdsToFetch,
                forceNetwork = forceNetwork,
            )

            if (fetchedBooks.isNotEmpty()) {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        val distinctEditionIds = editionIds.distinct()
        val editionIdsToFetch: List<Int> = if (forceNetwork) {
            distinctEditionIds
        } else {
            val cachedEditionIds = booksLocalDataSource.getExistingEditionIds(ids = distinctEditionIds).toSet()
            distinctEditionIds.filterNot { it in cachedEditionIds }
        }

        if (editionIdsToFetch.isNotEmpty()) {
            val fetchedEditions = booksRemoteDataSource.fetchEditionsByIds(
                ids = editionIdsToFetch,
                forceNetwork = forceNetwork,
            )

            if (fetchedEditions.isNotEmpty()) {
                booksLocalDataSource.cacheEditions(editions = fetchedEditions)
            }
        }
    }

    override suspend fun cacheBook(book: Book) {
        booksLocalDataSource.cacheBook(book = book)
    }

    override suspend fun removeBook(book: Book) {
        val userId: Int = book.userBook?.id ?: throw Exception("Book has no user book id")

        booksLocalDataSource.removeUserBooksById(ids = listOf(userId))
    }

    override suspend fun removeAllBooks() {
        booksLocalDataSource.removeAllBooks()
    }

    override suspend fun fetchBookById(id: Int): Book {
        if (networkAvailability.isOnline.value.not()) {
            return booksLocalDataSource.getBookById(id = id)
                ?: throw OfflineException()
        }

        return try {
            booksRemoteDataSource.fetchBookById(id = id)
        } catch (notFound: BookNotFoundException) {
            recoverBookByEdition(missingBookId = id) ?: throw notFound
        }
    }

    private suspend fun recoverBookByEdition(missingBookId: Int): Book? {
        val localBook: Book = booksLocalDataSource.getBookById(id = missingBookId) ?: return null

        val editionId: Int = localBook.userBook?.editionId
            ?: localBook.currentEdition?.id
            ?: localBook.editions.firstOrNull()?.id
            ?: return null

        val canonicalBookId: Int = booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            ?: return null

        if (canonicalBookId == missingBookId) return null

        Timber.w("Book $missingBookId missing remotely; recovered canonical $canonicalBookId via edition $editionId")

        val canonical: Book = try {
            booksRemoteDataSource.fetchBookById(id = canonicalBookId)
        } catch (canonicalMissing: BookNotFoundException) {
            Timber.w("Canonical $canonicalBookId also missing while recovering $missingBookId")

            throw BookNotFoundException(bookId = missingBookId)
        }

        persistCanonicalRedirect(
            oldId = missingBookId,
            canonical = canonical,
        )

        return canonical
    }

    private suspend fun persistCanonicalRedirect(
        oldId: Int,
        canonical: Book,
    ) {
        booksLocalDataSource.cacheBook(book = canonical)

        booksLocalDataSource.redirectBookId(
            oldId = oldId,
            newId = canonical.id,
        )

        booksLocalDataSource.deleteOrphanBooks()
    }

    override suspend fun fetchEditionMatchForIsbn(isbn: String): IsbnEditionMatch? {
        if (networkAvailability.isOnline.value.not()) throw OfflineException()

        return booksRemoteDataSource.fetchEditionMatchForIsbn(isbn = isbn)
    }

    override suspend fun addBookByIsbn(isbn: String): CreatedBook {
        if (networkAvailability.isOnline.value.not()) throw OfflineException()

        return booksRemoteDataSource.addBookByIsbn(isbn = isbn)
    }

    override suspend fun fetchBooksByIds(ids: List<Int>): List<Book> {
        return booksRemoteDataSource.fetchBooksByIds(ids = ids)
    }

    override suspend fun fetchTrendingBooks(): List<Book> {
        val today = LocalDate.now()

        return booksRemoteDataSource.fetchTrendingBooks(
            from = today.minusDays(TRENDING_WINDOW_DAYS).toString(),
            to = today.toString(),
            limit = TRENDING_LIMIT,
            offset = TRENDING_OFFSET,
        )
    }

    override suspend fun getEditionsByBookId(bookId: Int): List<BookEdition> {
        if (networkAvailability.isOnline.value.not()) {
            return booksLocalDataSource.getBookById(id = bookId)?.editions.orEmpty()
        }

        return booksRemoteDataSource.getEditionsByBookId(bookId = bookId)
    }

    override suspend fun fetchEditionsByIds(ids: List<Int>): List<BookEdition> {
        return booksRemoteDataSource.fetchEditionsByIds(ids = ids)
    }

    override suspend fun markBookAsWantToRead(
        book: Book,
        editionId: Int?,
    ): Book {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withMarkedAsWantToRead()

        if (optimistic !== book) {
            booksLocalDataSource.cacheBook(book = optimistic)
        }

        return runCatching {
            booksRemoteDataSource.markBookAsWantToRead(
                bookId = book.id,
                editionId = editionId,
            )
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            restoreOptimisticWrite(snapshot = snapshot)
            throw error
        }.also { updated ->
            booksLocalDataSource.cacheBook(book = updated)
        }
    }

    override suspend fun markBookAsReading(book: Book): Book {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withMarkedAsReading()
        booksLocalDataSource.cacheBook(book = optimistic)

        return runCatching {
            booksRemoteDataSource.markBookAsReading(book)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            restoreOptimisticWrite(snapshot = snapshot)
            throw error
        }
    }

    override suspend fun updateBookRating(
        book: Book,
        rating: Double,
    ): Book {
        val userBook = book.userBook ?: throw Exception("User did not have a user book")

        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withRating(rating = rating)
        booksLocalDataSource.cacheBook(book = optimistic)

        if (networkAvailability.isOnline.value) {
            return runCatching {
                booksRemoteDataSource.updateBookRating(
                    userBook = userBook,
                    rating = rating,
                )
            }.getOrElse { error ->
                when (error) {
                    is CancellationException -> throw error

                    is OfflineException -> {
                        enqueueRatingUpdate(
                            book = optimistic,
                            rating = rating,
                        )
                        optimistic
                    }

                    else -> {
                        restoreOptimisticWrite(snapshot = snapshot)
                        throw error
                    }
                }
            }
        }

        enqueueRatingUpdate(
            book = optimistic,
            rating = rating,
        )

        return optimistic
    }

    override suspend fun updateBookReview(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ): Book {
        val userBook = book.userBook ?: throw Exception("User did not have a user book")

        val reviewedAt: String = LocalDate.now().toString()

        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withReview(
            review = review,
            hasSpoilers = hasSpoilers,
        )
        booksLocalDataSource.cacheBook(book = optimistic)

        if (networkAvailability.isOnline.value) {
            return runCatching {
                booksRemoteDataSource.updateBookReview(
                    userBook = userBook,
                    review = review,
                    hasSpoilers = hasSpoilers,
                    reviewedAt = reviewedAt,
                )
            }.getOrElse { error ->
                when (error) {
                    is CancellationException -> throw error

                    is OfflineException -> {
                        enqueueReviewUpdate(
                            book = optimistic,
                            review = review,
                            hasSpoilers = hasSpoilers,
                        )
                        optimistic
                    }

                    else -> {
                        restoreOptimisticWrite(snapshot = snapshot)
                        throw error
                    }
                }
            }
        }

        enqueueReviewUpdate(
            book = optimistic,
            review = review,
            hasSpoilers = hasSpoilers,
        )

        return optimistic
    }

    override suspend fun removeBookFromLibrary(book: Book) {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val userBookId: Int? = book.userBook?.id

        if (userBookId != null) {
            booksLocalDataSource.removeUserBooksById(ids = listOf(userBookId))
        }

        runCatching {
            booksRemoteDataSource.removeBookFromLibrary(book = book)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            restoreOptimisticWrite(snapshot = snapshot)
            throw error
        }
    }

    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
    ): Book {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withProgress(
            newPage = newPage,
            newSeconds = newSeconds,
        )
        booksLocalDataSource.cacheBook(book = optimistic)

        if (networkAvailability.isOnline.value) {
            return runCatching {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = newPage,
                    newSeconds = newSeconds,
                )
            }.getOrElse { error ->
                when (error) {
                    is CancellationException -> throw error

                    is OfflineException -> {
                        enqueueProgressUpdate(
                            book = optimistic,
                            newPage = newPage,
                            newSeconds = newSeconds,
                        )
                        optimistic
                    }

                    else -> {
                        restoreOptimisticWrite(snapshot = snapshot)
                        throw error
                    }
                }
            }
        }

        enqueueProgressUpdate(
            book = optimistic,
            newPage = newPage,
            newSeconds = newSeconds,
        )
        return optimistic
    }

    override suspend fun markBookAsRead(
        book: Book,
        editionId: Int?,
    ): Book {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withMarkedAsRead()
        booksLocalDataSource.cacheBook(book = optimistic)

        if (networkAvailability.isOnline.value) {
            return runCatching {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = editionId,
                )
            }.getOrElse { error ->
                when (error) {
                    is CancellationException -> throw error

                    is OfflineException -> {
                        enqueueMarkAsRead(book = optimistic)
                        optimistic
                    }

                    else -> {
                        restoreOptimisticWrite(snapshot = snapshot)
                        throw error
                    }
                }
            }
        }

        enqueueMarkAsRead(book = optimistic)
        return optimistic
    }

    private suspend fun enqueueProgressUpdate(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
    ) {
        val userBook = book.userBook ?: return
        val userBookRead = book.userBookRead ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = newPage,
                progressSeconds = newSeconds,
                startedAt = userBookRead.startedAt,
                finishedAt = userBookRead.finishedAt,
                enqueuedAt = Instant.now().toString(),
            ),
        )
    }

    private suspend fun enqueueMarkAsRead(book: Book) {
        val userBook = book.userBook ?: return
        val userBookRead = book.userBookRead ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = userBookRead.currentPage,
                progressSeconds = userBookRead.currentSeconds,
                startedAt = userBookRead.startedAt,
                finishedAt = userBookRead.finishedAt,
                enqueuedAt = Instant.now().toString(),
            ),
        )
    }

    private suspend fun enqueueRatingUpdate(
        book: Book,
        rating: Double,
    ) {
        val userBook = book.userBook ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                userBookId = userBook.id,
                // Unused for UPDATE_RATING replay (which keys off userBookId); a Read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.userBookRead?.id ?: 0,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                rating = rating,
                enqueuedAt = Instant.now().toString(),
            ),
        )
    }

    private suspend fun enqueueReviewUpdate(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ) {
        val userBook = book.userBook ?: return

        userBookWriteQueue.enqueue(
            PendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = userBook.id,
                // Unused for UPDATE_REVIEW replay (which keys off userBookId); a Read book may have
                // no userBookRead, so fall back to the schema's non-null placeholder.
                userBookReadId = book.userBookRead?.id ?: 0,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                reviewSlateJson = review.toJson(),
                reviewHasSpoilers = hasSpoilers,
                enqueuedAt = Instant.now().toString(),
            ),
        )
    }

    private suspend fun restoreOptimisticWrite(snapshot: Book?) {
        if (snapshot == null) {
            Timber.w("Optimistic rollback skipped: no prior snapshot in cache")
            return
        }

        booksLocalDataSource.cacheBook(book = snapshot)
    }

    private suspend fun preserveSyncedProgress(
        fetchedBooks: List<Book>,
        syncedUserBookIds: Set<Int>,
    ): List<Book> {
        if (syncedUserBookIds.isEmpty()) return fetchedBooks

        val snapshots: Map<Int, Book> = booksLocalDataSource.allUserBooks
            .first()
            .mapNotNull { book ->
                val userBookId: Int = book.userBook?.id ?: return@mapNotNull null

                if (userBookId in syncedUserBookIds) userBookId to book else null
            }
            .toMap()

        if (snapshots.isEmpty()) return fetchedBooks

        return fetchedBooks.map { fetched ->
            val userBookId: Int? = fetched.userBook?.id
            val snapshot: Book? = userBookId?.let { snapshots[it] }

            if (snapshot != null) {
                fetched.copy(
                    userBook = snapshot.userBook,
                    userBookRead = snapshot.userBookRead,
                )
            } else {
                fetched
            }
        }
    }

    private fun Book.withProgress(
        newPage: Int?,
        newSeconds: Int?,
    ): Book {
        val existingRead = userBookRead ?: return this
        val edition = currentEdition

        val totalPages = edition?.pages
        val totalSeconds = edition?.audioSeconds

        val progress: Float = when {
            newSeconds != null && totalSeconds != null && totalSeconds > 0 ->
                (newSeconds.toFloat() / totalSeconds.toFloat() * 100f).coerceIn(
                    0f,
                    100f,
                )

            newPage != null && totalPages != null && totalPages > 0 ->
                (newPage.toFloat() / totalPages.toFloat() * 100f).coerceIn(
                    0f,
                    100f,
                )

            else -> existingRead.progress
        }

        val updatedUserBook: UserBook? = userBook?.withAppendedJournal(
            event = JournalEventType.ProgressUpdated,
        )

        return copy(
            userBook = updatedUserBook,
            userBookRead = existingRead.copy(
                currentPage = newPage ?: existingRead.currentPage,
                currentSeconds = newSeconds ?: existingRead.currentSeconds,
                progress = progress,
            ),
        )
    }

    private fun Book.withMarkedAsWantToRead(): Book {
        val existingUserBook = userBook ?: return this

        if (existingUserBook.status == BookStatus.WantToRead) return this

        val updatedUserBook: UserBook = existingUserBook.copy(status = BookStatus.WantToRead)

        return copy(userBook = updatedUserBook)
    }

    private fun Book.withMarkedAsReading(): Book {
        val existingUserBook = userBook ?: return this

        val updatedUserBook: UserBook = existingUserBook
            .copy(status = BookStatus.Reading)
            .withAppendedJournal(event = JournalEventType.UserBookReadStarted)

        return copy(userBook = updatedUserBook)
    }

    private fun Book.withRating(rating: Double): Book {
        val existingUserBook = userBook ?: return this

        if (existingUserBook.rating == rating) return this

        val updatedUserBook: UserBook = existingUserBook.copy(rating = rating)

        return copy(userBook = updatedUserBook)
    }

    private fun Book.withReview(
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ): Book {
        val existingUserBook = userBook ?: return this

        // A blank document clears the review; the optimistic copy stores what the user wrote, which the
        // next server refresh overwrites with Hardcover's canonical `review_slate`.
        val normalizedReview: ReviewDocument? = review.takeUnless { it.isBlank() }

        val updatedUserBook: UserBook = existingUserBook.copy(
            reviewDocument = normalizedReview,
            reviewHasSpoilers = hasSpoilers,
        )

        return copy(userBook = updatedUserBook)
    }

    private fun Book.withMarkedAsRead(): Book {
        val existingUserBook = userBook ?: return this
        val existingRead = userBookRead

        val today = LocalDate.now().toString()

        val updatedUserBook: UserBook = existingUserBook
            .copy(status = BookStatus.Read)
            .withAppendedJournal(event = JournalEventType.StatusFinished)

        return copy(
            userBook = updatedUserBook,
            userBookRead = existingRead?.copy(
                finishedAt = today,
                progress = 100f,
            ) ?: existingRead,
        )
    }

    private fun UserBook.withAppendedJournal(event: JournalEventType): UserBook {
        val entry = ReadingJournal(
            updatedAt = LocalDateTime.now().toString(),
            event = event.eventName,
        )

        return copy(journals = journals + entry)
    }

    override suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book {
        return booksRemoteDataSource.updateBookEdition(
            userBook = userBook,
            newEditionId = newEditionId,
        )
    }

    override suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    ) {
        booksLocalDataSource.persistEditionImage(
            editionId = editionId,
            source = source,
        )
    }
}
