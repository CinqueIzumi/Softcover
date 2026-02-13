package nl.rhaydus.softcover.feature.caching.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.caching.data.database.BookDao
import nl.rhaydus.softcover.feature.caching.data.mapper.toModel

interface CachingLocalDataSource {
    val allUserBooks: Flow<List<Book>>

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)

    suspend fun removeUserBooksById(ids: List<Int>)

    suspend fun removeAllBooks()
}

class CachingLocalDataSourceImpl(
    private val dao: BookDao,
) : CachingLocalDataSource {
    override val allUserBooks: Flow<List<Book>>
        get() = dao.observeBooks().map { list -> list.map { it.toModel() } }

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return dao.getBooksByStatus(statusCode = status.code).map { list -> list.map { it.toModel() } }
    }

    override suspend fun cacheBook(book: Book) {
        dao.cacheBook(book = book)
    }

    override suspend fun cacheBooks(books: List<Book>) {
        books.forEach { dao.cacheBook(book = it) }
    }

    override suspend fun removeUserBooksById(ids: List<Int>) {
        ids.forEach { userBookId ->
            dao.deleteAllForUserBookId(userBookId = userBookId)
        }
    }

    override suspend fun removeAllBooks() = dao.deleteAllUserBooksAndData()
}