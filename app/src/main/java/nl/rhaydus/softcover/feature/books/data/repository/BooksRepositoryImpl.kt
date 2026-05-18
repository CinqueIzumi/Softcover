package nl.rhaydus.softcover.feature.books.data.repository

import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.OfflineProgressQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressDrainer
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressUpdate
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressUpdateKind
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.domain.model.enum.JournalEventType
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import timber.log.Timber

private const val OWNED_LIST_SLUG: String = "owned"

class BooksRepositoryImpl(
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val booksLocalDataSource: BooksLocalDataSource,
    private val settingsRepository: SettingsRepository,
    private val networkAvailability: NetworkAvailabilityProvider,
    private val offlineProgressQueue: OfflineProgressQueue,
    private val pendingProgressDrainer: PendingProgressDrainer,
) : BooksRepository {
    override val books: Flow<List<Book>> = booksLocalDataSource.allUserBooks
    override val allUserLists: Flow<List<BookList>> = booksLocalDataSource.allUserLists

    private var initializedBooksThisSession: Boolean = false

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return booksLocalDataSource.getBooksFlowByStatus(status = status)
    }

    override suspend fun initializeBooks(userId: Int) {
        if (initializedBooksThisSession) {
            throw Exception("User already initialized books this session")
        }

        fetchAndCacheBooks(userId = userId)

        initializedBooksThisSession = true
    }

    override suspend fun refreshUserBooks(userId: Int) {
        fetchAndCacheBooks(userId = userId, forceRefreshReferences = true)
    }

    private suspend fun fetchAndCacheBooks(
        userId: Int,
        forceRefreshReferences: Boolean = false,
    ) = withContext(Dispatchers.IO) {
        val syncedUserBookIds: Set<Int> = pendingProgressDrainer.drainPendingUpdates()

        val fetchStatusCodes = UserBookStatus.activeLibraryCodes(
            enabledCodes = settingsRepository.enabledStatusCodes.first(),
        ) + UserBookStatus.alwaysCachedCodes
        val seeded = settingsRepository.listDefaultsSeeded.first()

        val booksDeferred = async {
            booksRemoteDataSource.initializeBooks(userId = userId, statusIds = fetchStatusCodes)
        }
        val listsDeferred = async {
            booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
        }

        val fetchedBooks = booksDeferred.await()
        val fetchedLists = listsDeferred.await()

        if (seeded.not()) {
            val ownedListId = fetchedLists.firstOrNull { it.slug == OWNED_LIST_SLUG }?.id
            settingsRepository.seedEnabledListIds(ids = setOfNotNull(ownedListId))
        }

        val enabledListIds = settingsRepository.enabledListIds.first()
        val ownedListId = fetchedLists.firstOrNull { it.slug == OWNED_LIST_SLUG }?.id
        val alwaysCachedListIds = setOfNotNull(ownedListId)
        val listIdsToHydrate = enabledListIds + alwaysCachedListIds

        val listsToCache = fetchedLists.map { list ->
            if (list.id in listIdsToHydrate) list else list.copy(books = emptyList())
        }

        val booksToCache: List<Book> = preserveSyncedProgress(
            fetchedBooks = fetchedBooks,
            syncedUserBookIds = syncedUserBookIds,
        )

        booksLocalDataSource.cacheBooks(books = booksToCache)

        val fetchedBookUserBookIds = fetchedBooks.mapNotNull { it.userBook?.id }.toSet()

        val locallyStoredUserBookIds = booksLocalDataSource.getAllUserBookIds()

        val userBookIdsToRemove: List<Int> = locallyStoredUserBookIds
            .filterNot { it in fetchedBookUserBookIds }

        booksLocalDataSource.removeUserBooksById(ids = userBookIdsToRemove)

        booksLocalDataSource.syncBookListMetadata(serverListIds = fetchedLists.map { it.id }.toSet())

        hydrateOrphanOwnedBooks(
            lists = listsToCache.filter { it.id in listIdsToHydrate },
            forceRefreshAll = forceRefreshReferences,
        )

        booksLocalDataSource.cacheUserBookLists(lists = listsToCache)

        booksLocalDataSource.deleteOrphanBooks()
    }

    private suspend fun hydrateOrphanOwnedBooks(
        lists: List<BookList>,
        forceRefreshAll: Boolean,
    ) {
        val referenced = lists.flatMap { list -> list.books.map { it.bookId to it.editionId } }

        if (referenced.isEmpty()) return

        val referencedBookIds = referenced.map { it.first }.distinct()
        val bookIdsToFetch: List<Int> = if (forceRefreshAll) {
            referencedBookIds
        } else {
            val cachedBookIds = booksLocalDataSource.getExistingBookIds(ids = referencedBookIds).toSet()
            referencedBookIds.filterNot { it in cachedBookIds }
        }

        if (bookIdsToFetch.isNotEmpty()) {
            val fetchedBooks = booksRemoteDataSource.fetchBooksByIds(
                ids = bookIdsToFetch,
                forceNetwork = forceRefreshAll,
            )

            if (fetchedBooks.isNotEmpty()) {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        val referencedEditionIds = referenced.map { it.second }.distinct()
        val editionIdsToFetch: List<Int> = if (forceRefreshAll) {
            referencedEditionIds
        } else {
            val cachedEditionIds = booksLocalDataSource.getExistingEditionIds(ids = referencedEditionIds).toSet()
            referencedEditionIds.filterNot { it in cachedEditionIds }
        }

        if (editionIdsToFetch.isNotEmpty()) {
            val fetchedEditions = booksRemoteDataSource.fetchEditionsByIds(
                ids = editionIdsToFetch,
                forceNetwork = forceRefreshAll,
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

        initializedBooksThisSession = false
    }

    override suspend fun fetchBookById(id: Int): Book {
        if (networkAvailability.isOnline.value.not()) {
            return booksLocalDataSource.getBookById(id = id)
                ?: throw OfflineException()
        }

        return booksRemoteDataSource.fetchBookById(id = id)
    }

    override suspend fun fetchBooksByIds(ids: List<Int>): List<Book> {
        return booksRemoteDataSource.fetchBooksByIds(ids = ids)
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

    override suspend fun markBookAsWantToRead(book: Book): Book {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withMarkedAsWantToRead()

        if (optimistic !== book) {
            booksLocalDataSource.cacheBook(book = optimistic)
        }

        return runCatching {
            booksRemoteDataSource.markBookAsWantToRead(bookId = book.id)
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

    override suspend fun markBookAsRead(book: Book): Book {
        val snapshot: Book? = booksLocalDataSource.getBookById(id = book.id)
        val optimistic = book.withMarkedAsRead()
        booksLocalDataSource.cacheBook(book = optimistic)

        if (networkAvailability.isOnline.value) {
            return runCatching {
                booksRemoteDataSource.markBookAsRead(book = book)
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

        offlineProgressQueue.enqueue(
            PendingProgressUpdate(
                kind = PendingProgressUpdateKind.UPDATE_PROGRESS,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = newPage,
                progressSeconds = newSeconds,
                startedAt = userBookRead.startedAt,
                finishedAt = userBookRead.finishedAt,
                enqueuedAt = Instant.now().toString(),
            )
        )
    }

    private suspend fun enqueueMarkAsRead(book: Book) {
        val userBook = book.userBook ?: return
        val userBookRead = book.userBookRead ?: return

        offlineProgressQueue.enqueue(
            PendingProgressUpdate(
                kind = PendingProgressUpdateKind.MARK_AS_READ,
                userBookId = userBook.id,
                userBookReadId = userBookRead.id,
                bookId = book.id,
                editionId = userBook.editionId,
                progressPages = userBookRead.currentPage,
                progressSeconds = userBookRead.currentSeconds,
                startedAt = userBookRead.startedAt,
                finishedAt = userBookRead.finishedAt,
                enqueuedAt = Instant.now().toString(),
            )
        )
    }

    private suspend fun restoreOptimisticWrite(snapshot: Book?) {
        if (snapshot == null) {
            Timber.w("-=- Optimistic rollback skipped: no prior snapshot in cache")
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
                (newSeconds.toFloat() / totalSeconds.toFloat() * 100f).coerceIn(0f, 100f)

            newPage != null && totalPages != null && totalPages > 0 ->
                (newPage.toFloat() / totalPages.toFloat() * 100f).coerceIn(0f, 100f)

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

    override suspend fun markEditionAsOwned(edition: BookEdition) {
        val snapshot: ListBook? = booksLocalDataSource.findOwnedListBookByEditionId(
            editionId = edition.id,
        )
        val ownedListId: Int? = booksLocalDataSource.getOwnedListId()

        if (ownedListId != null) {
            booksLocalDataSource.cacheListBook(
                book = ListBook(
                    listBookId = OPTIMISTIC_LIST_BOOK_ID,
                    listId = ownedListId,
                    bookId = edition.bookId,
                    editionId = edition.id,
                ),
            )
        }

        val real: ListBook = runCatching {
            booksRemoteDataSource.markEditionAsOwned(edition = edition)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            restoreOwnedListBook(
                editionId = edition.id,
                snapshot = snapshot,
            )
            throw error
        }

        booksLocalDataSource.removeOwnedListBookByEditionId(editionId = edition.id)
        booksLocalDataSource.cacheListBook(book = real)
    }

    override suspend fun removeOwnedEdition(editionId: Int) {
        val snapshot: ListBook = booksLocalDataSource.findOwnedListBookByEditionId(
            editionId = editionId,
        ) ?: return

        booksLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)

        val updatedList: BookList = runCatching {
            booksRemoteDataSource.removeListBook(book = snapshot)
        }.getOrElse { error ->
            if (error is CancellationException) throw error

            booksLocalDataSource.cacheListBook(book = snapshot)
            throw error
        }

        booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    private suspend fun restoreOwnedListBook(
        editionId: Int,
        snapshot: ListBook?,
    ) {
        booksLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)

        if (snapshot != null && snapshot.listBookId != OPTIMISTIC_LIST_BOOK_ID) {
            booksLocalDataSource.cacheListBook(book = snapshot)
        }
    }

    override suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    ) {
        booksLocalDataSource.persistEditionImage(editionId = editionId, source = source)
    }

    private companion object {
        const val OPTIMISTIC_LIST_BOOK_ID = 0
    }
}
