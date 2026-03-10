package nl.rhaydus.softcover.feature.books.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.dao.BookDao
import nl.rhaydus.softcover.feature.books.data.mapper.toModel

interface BooksLocalDataSource {
    val allUserBooks: Flow<List<Book>>
    val allUserLists: Flow<List<BookList>>

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)

    suspend fun removeUserBooksById(ids: List<Int>)

    suspend fun cacheUserBookLists(lists: List<BookList>)

    suspend fun removeAllBooks()
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
            .map { lists ->
                lists.map { list ->
                    val editionsWithAuthors = list.editions.map { editionView ->
                        val edition = editionView.edition

                        val authors = dao.getAuthorsForEdition(editionId = edition.id)

                        edition.toModel(
                            authors = authors,
                            owned = editionView.isOwned
                        )
                    }

                    BookList(
                        id = list.bookList.id,
                        name = list.bookList.name,
                        editions = editionsWithAuthors,
                        slug = list.bookList.slug,
                    )
                }
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
        books.forEach { dao.cacheBook(book = it) }
    }

    override suspend fun removeUserBooksById(ids: List<Int>) {
        ids.forEach { userBookId ->
            dao.deleteAllForUserBookId(userBookId = userBookId)
        }
    }

    override suspend fun cacheUserBookLists(lists: List<BookList>) {
        lists.forEach { dao.cacheBookList(bookList = it) }
    }

    override suspend fun removeAllBooks() = dao.deleteAllUserBooksAndData()
}