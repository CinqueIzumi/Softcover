package nl.rhaydus.softcover.feature.books.domain.repository

import java.io.File
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection

interface BooksRepository {
    val books: Flow<List<Book>>
    val allUserLists: Flow<List<BookList>>

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    /** Library-screen path: all user books sorted via SQL `ORDER BY`. */
    fun getSortedAllUserBooks(mode: LibrarySortMode, direction: SortDirection): Flow<List<Book>>

    /** Library-screen path: user books for [status], sorted via SQL `ORDER BY`. */
    fun getSortedBooksByStatus(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>>

    suspend fun refreshUserBooks(
        userId: Int,
        scope: RefreshScope = RefreshScope.All,
    )

    suspend fun cacheBook(book: Book)

    suspend fun removeBook(book: Book)

    suspend fun removeAllBooks()

    suspend fun fetchBookById(id: Int): Book

    suspend fun fetchBooksByIds(ids: List<Int>): List<Book>

    suspend fun getEditionsByBookId(bookId: Int): List<BookEdition>

    suspend fun fetchEditionsByIds(ids: List<Int>): List<BookEdition>

    suspend fun markBookAsWantToRead(book: Book): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Book

    suspend fun markBookAsRead(book: Book): Book

    suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book

    suspend fun markEditionAsOwned(edition: BookEdition)

    suspend fun removeOwnedEdition(editionId: Int)

    suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    )
}