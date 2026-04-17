package nl.rhaydus.softcover.feature.books.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.dao.BookDao
import nl.rhaydus.softcover.feature.books.data.mapper.toModel

interface BooksLocalDataSource {
    val allUserBooks: Flow<List<Book>>
    val allUserLists: Flow<List<BookList>>

    suspend fun getAllUserBookIds(): List<Int>

    suspend fun getExistingBookIds(ids: List<Int>): List<Int>

    suspend fun getExistingEditionIds(ids: List<Int>): List<Int>

    suspend fun cacheEditions(editions: List<BookEdition>)

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)

    suspend fun removeUserBooksById(ids: List<Int>)

    suspend fun cacheUserBookLists(lists: List<BookList>)

    suspend fun cacheListBook(book: ListBook)

    suspend fun removeAllBooks()

    suspend fun getOwnedListBookByEditionId(editionId: Int): ListBook
}

class BooksLocalDataSourceImpl(
    private val dao: BookDao,
) : BooksLocalDataSource {
    override val allUserBooks: Flow<List<Book>>
        get() = dao
            .observeBooks()
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }

    override val allUserLists: Flow<List<BookList>>
        get() = dao
            .observeBookLists()
            .distinctUntilChanged()
            .map { lists -> lists.map { it.toModel() } }

    override suspend fun getAllUserBookIds(): List<Int> {
        return dao.getAllUserBookIds()
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

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return dao
            .getBooksByStatus(statusCode = status.code)
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }
    }

    override suspend fun cacheBook(book: Book) {
        dao.cacheBook(book = book)
    }

    override suspend fun cacheBooks(books: List<Book>) {
        dao.cacheBooks(books = books)
    }

    override suspend fun removeUserBooksById(ids: List<Int>) {
        dao.deleteUserBooksByIds(ids)
    }

    override suspend fun cacheUserBookLists(lists: List<BookList>) {
        dao.cacheBookLists(lists = lists)
    }

    override suspend fun cacheListBook(book: ListBook) {
        dao.cacheListBook(listBook = book)
    }

    override suspend fun removeAllBooks() = dao.deleteAllUserBooksAndData()

    override suspend fun getOwnedListBookByEditionId(editionId: Int): ListBook {
        val book = dao.getOwnedListBookByEditionId(editionId = editionId)
            ?: throw Exception("List book was not found!")

        return book.toModel()
    }
}