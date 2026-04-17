package nl.rhaydus.softcover.feature.books.domain.repository

import java.io.File
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

interface BooksRepository {
    val books: Flow<List<Book>>
    val allUserLists: Flow<List<BookList>>

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun initializeBooks(userId: Int)

    suspend fun refreshUserBooks(userId: Int)

    suspend fun cacheBook(book: Book)

    suspend fun removeBook(book: Book)

    suspend fun removeAllBooks()

    suspend fun fetchBookById(id: Int): Book

    suspend fun fetchBooksByIds(ids: List<Int>): List<Book>

    suspend fun getEditionsByBookId(bookId: Int): List<BookEdition>

    suspend fun fetchEditionsByIds(ids: List<Int>): List<BookEdition>

    suspend fun markBookAsWantToRead(bookId: Int): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book

    suspend fun markBookAsRead(book: Book): Book

    suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book

    suspend fun markEditionAsOwned(edition: BookEdition): ListBook

    suspend fun getListBookByEditionId(editionId: Int): ListBook

    suspend fun removeListBook(book: ListBook)

    suspend fun cacheListBook(book: ListBook)

    suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    )
}