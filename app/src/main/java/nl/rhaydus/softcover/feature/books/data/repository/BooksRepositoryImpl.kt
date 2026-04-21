package nl.rhaydus.softcover.feature.books.data.repository

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

private const val OWNED_LIST_SLUG: String = "owned"

class BooksRepositoryImpl(
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val booksLocalDataSource: BooksLocalDataSource,
    private val settingsRepository: SettingsRepository,
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
        fetchAndCacheBooks(userId = userId)
    }

    private suspend fun fetchAndCacheBooks(userId: Int) = withContext(Dispatchers.IO) {
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

        booksLocalDataSource.cacheBooks(books = fetchedBooks)

        val fetchedBookUserBookIds = fetchedBooks.mapNotNull { it.userBook?.id }.toSet()

        val locallyStoredUserBookIds = booksLocalDataSource.getAllUserBookIds()

        val userBookIdsToRemove: List<Int> = locallyStoredUserBookIds
            .filterNot { it in fetchedBookUserBookIds }

        booksLocalDataSource.removeUserBooksById(ids = userBookIdsToRemove)

        booksLocalDataSource.syncBookListMetadata(serverListIds = fetchedLists.map { it.id }.toSet())

        hydrateOrphanOwnedBooks(lists = listsToCache.filter { it.id in listIdsToHydrate })

        booksLocalDataSource.cacheUserBookLists(lists = listsToCache)

        booksLocalDataSource.deleteOrphanBooks()
    }

    private suspend fun hydrateOrphanOwnedBooks(lists: List<BookList>) {
        val referenced = lists.flatMap { list -> list.books.map { it.bookId to it.editionId } }

        if (referenced.isEmpty()) return

        val referencedBookIds = referenced.map { it.first }.distinct()
        val cachedBookIds = booksLocalDataSource.getExistingBookIds(ids = referencedBookIds).toSet()
        val missingBookIds = referencedBookIds.filterNot { it in cachedBookIds }

        if (missingBookIds.isNotEmpty()) {
            val orphanBooks = booksRemoteDataSource.fetchBooksByIds(ids = missingBookIds)

            if (orphanBooks.isNotEmpty()) {
                booksLocalDataSource.cacheBooks(books = orphanBooks)
            }
        }

        val referencedEditionIds = referenced.map { it.second }.distinct()
        val cachedEditionIds = booksLocalDataSource.getExistingEditionIds(ids = referencedEditionIds).toSet()
        val missingEditionIds = referencedEditionIds.filterNot { it in cachedEditionIds }

        if (missingEditionIds.isNotEmpty()) {
            val orphanEditions = booksRemoteDataSource.fetchEditionsByIds(ids = missingEditionIds)

            if (orphanEditions.isNotEmpty()) {
                booksLocalDataSource.cacheEditions(editions = orphanEditions)
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
        return booksRemoteDataSource.fetchBookById(id = id)
    }

    override suspend fun fetchBooksByIds(ids: List<Int>): List<Book> {
        return booksRemoteDataSource.fetchBooksByIds(ids = ids)
    }

    override suspend fun getEditionsByBookId(bookId: Int): List<BookEdition> {
        return booksRemoteDataSource.getEditionsByBookId(bookId = bookId)
    }

    override suspend fun fetchEditionsByIds(ids: List<Int>): List<BookEdition> {
        return booksRemoteDataSource.fetchEditionsByIds(ids = ids)
    }

    override suspend fun markBookAsWantToRead(bookId: Int): Book {
        return booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
    }

    override suspend fun markBookAsReading(book: Book): Book {
        return booksRemoteDataSource.markBookAsReading(book)
    }

    override suspend fun removeBookFromLibrary(book: Book) {
        return booksRemoteDataSource.removeBookFromLibrary(book = book)
    }

    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
    ): Book {
        return booksRemoteDataSource.updateBookProgress(
            book = book,
            newPage = newPage,
            newSeconds = newSeconds,
        )
    }

    override suspend fun markBookAsRead(book: Book): Book {
        return booksRemoteDataSource.markBookAsRead(book = book)
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

    override suspend fun markEditionAsOwned(edition: BookEdition): ListBook {
        return booksRemoteDataSource.markEditionAsOwned(edition = edition)
    }

    override suspend fun getListBookByEditionId(editionId: Int): ListBook {
        return booksLocalDataSource.getOwnedListBookByEditionId(editionId = editionId)
    }

    override suspend fun removeListBook(book: ListBook) {
        val updatedList = booksRemoteDataSource.removeListBook(book = book)

        booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    override suspend fun cacheListBook(book: ListBook) {
        booksLocalDataSource.cacheListBook(book = book)
    }

    override suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    ) {
        booksLocalDataSource.persistEditionImage(editionId = editionId, source = source)
    }
}
