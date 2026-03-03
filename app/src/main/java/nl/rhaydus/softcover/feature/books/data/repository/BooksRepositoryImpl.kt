package nl.rhaydus.softcover.feature.books.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import timber.log.Timber

class BooksRepositoryImpl(
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val booksLocalDataSource: BooksLocalDataSource,
) : BooksRepository {
    override val books: Flow<List<Book>> = booksLocalDataSource.allUserBooks

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

    private suspend fun fetchAndCacheBooks(userId: Int) {
        val fetchedBooks: List<Book> = booksRemoteDataSource.initializeBooks(userId = userId)

        booksLocalDataSource.cacheBooks(books = fetchedBooks)

        val fetchedBookUserBookIds = fetchedBooks.mapNotNull { it.userBook?.id }

        val locallyStoredUserBookIds = books
            .firstOrNull()
            ?.mapNotNull { it.userBook?.id } ?: emptyList()

        val userBookIdsToRemove: List<Int> = locallyStoredUserBookIds
            .filterNot { it in fetchedBookUserBookIds }

        booksLocalDataSource.removeUserBooksById(ids = userBookIdsToRemove)

        val lists = booksRemoteDataSource.fetchUserLists(userId = userId)

        booksLocalDataSource.cacheUserBookLists(lists = lists)
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
        newPage: Int,
    ): Book {
        return booksRemoteDataSource.updateBookProgress(
            book = book,
            newPage = newPage,
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
}