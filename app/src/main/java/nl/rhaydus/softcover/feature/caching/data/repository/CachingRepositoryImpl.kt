package nl.rhaydus.softcover.feature.caching.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.caching.data.datasource.CachingLocalDataSource
import nl.rhaydus.softcover.feature.caching.data.datasource.CachingRemoteDataSource
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository

class CachingRepositoryImpl(
    private val cachingLocalDataSource: CachingLocalDataSource,
    private val cachingRemoteDataSource: CachingRemoteDataSource,
) : CachingRepository {
    override val books: Flow<List<Book>> = cachingLocalDataSource.allUserBooks

    private var initializedBooksThisSession: Boolean = false

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return cachingLocalDataSource.getBooksFlowByStatus(status = status)
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
        val fetchedBooks: List<Book> = cachingRemoteDataSource.initializeBooks(userId = userId)

        cachingLocalDataSource.cacheBooks(books = fetchedBooks)

        val fetchedBookUserBookIds = fetchedBooks.mapNotNull { it.userBook?.id }

        val locallyStoredUserBookIds = books
            .firstOrNull()
            ?.mapNotNull { it.userBook?.id } ?: emptyList()

        val userBookIdsToRemove: List<Int> = locallyStoredUserBookIds
            .filterNot { it in fetchedBookUserBookIds }

        cachingLocalDataSource.removeUserBooksById(ids = userBookIdsToRemove)
    }

    override suspend fun cacheBook(book: Book) {
        cachingLocalDataSource.cacheBook(book = book)
    }

    override suspend fun removeBook(book: Book) {
        val userId: Int = book.userBook?.id ?: throw Exception("Book has no user book id")

        cachingLocalDataSource.removeUserBooksById(ids = listOf(userId))
    }

    override suspend fun removeAllBooks() {
        cachingLocalDataSource.removeAllBooks()

        initializedBooksThisSession = false
    }
}