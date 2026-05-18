package nl.rhaydus.softcover.feature.books.data.datasource

import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.data.storage.EditionImageStorage
import nl.rhaydus.softcover.feature.books.data.dao.BookDao
import nl.rhaydus.softcover.feature.books.data.mapper.toModel

interface BooksLocalDataSource {
    val allUserBooks: Flow<List<Book>>
    val allUserLists: Flow<List<BookList>>

    suspend fun getAllUserBookIds(): List<Int>

    suspend fun getExistingBookIds(ids: List<Int>): List<Int>

    suspend fun getExistingEditionIds(ids: List<Int>): List<Int>

    suspend fun cacheEditions(editions: List<BookEdition>)

    suspend fun updateEditionLocalImagePath(
        editionId: Int,
        path: String?,
    )

    suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    )

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)

    suspend fun removeUserBooksById(ids: List<Int>)

    suspend fun cacheUserBookLists(lists: List<BookList>)

    suspend fun cacheListBook(book: ListBook)

    suspend fun removeAllBooks()

    suspend fun findOwnedListBookByEditionId(editionId: Int): ListBook?

    suspend fun getOwnedListId(): Int?

    suspend fun removeOwnedListBookByEditionId(editionId: Int)

    suspend fun syncBookListMetadata(serverListIds: Set<Int>)

    suspend fun deleteOrphanBooks()

    suspend fun getBookById(id: Int): Book?

    suspend fun redirectBookId(oldId: Int, newId: Int)
}

class BooksLocalDataSourceImpl(
    private val dao: BookDao,
    private val editionImageStorage: EditionImageStorage,
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

    override suspend fun updateEditionLocalImagePath(
        editionId: Int,
        path: String?,
    ) {
        dao.updateEditionLocalImagePath(editionId = editionId, path = path)
    }

    override suspend fun persistEditionImage(
        editionId: Int,
        source: File,
    ) {
        if (editionImageStorage.exists(editionId = editionId)) return

        val storedPath = editionImageStorage.copyFrom(editionId = editionId, source = source)

        dao.updateEditionLocalImagePath(editionId = editionId, path = storedPath)
    }

    override fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>> {
        return when (status) {
            UserBookStatus.CURRENTLY_READING -> dao.getBooksByStatusAndEvents(
                statusCode = status.code,
                events = listOf("progress_updated", "user_book_read_started"),
            )
            UserBookStatus.READ -> dao.getReadBooks(statusCode = status.code)
            UserBookStatus.WANT_TO_READ -> dao.getBooksByStatusSortedByCreatedAt(
                statusCode = status.code,
            )
            UserBookStatus.DID_NOT_FINISH -> dao.getBooksByStatusAndEvents(
                statusCode = status.code,
                events = listOf("status_stopped"),
            )
        }
            .distinctUntilChanged()
            .map { list -> list.map { it.toModel() } }
    }

    override suspend fun cacheBook(book: Book) {
        dao.cacheBook(book = book)
    }

    override suspend fun getBookById(id: Int): Book? {
        return dao.getBookById(id = id)?.toModel()
    }

    override suspend fun cacheBooks(books: List<Book>) {
        dao.cacheBooks(books = books)
    }

    override suspend fun removeUserBooksById(ids: List<Int>) {
        val bookIds = ids.mapNotNull { dao.getBookIdByUserBookId(userBookId = it) }
        val pathsToDelete = bookIds.flatMap { bookId ->
            dao.getLocalImagePathsByBookId(bookId = bookId).mapNotNull { it.localImagePath }
        }

        dao.deleteUserBooksByIds(ids)

        pathsToDelete.forEach { editionImageStorage.delete(path = it) }
    }

    override suspend fun cacheUserBookLists(lists: List<BookList>) {
        dao.cacheBookLists(lists = lists)
    }

    override suspend fun cacheListBook(book: ListBook) {
        dao.cacheListBook(listBook = book)
    }

    override suspend fun removeAllBooks() {
        val pathsToDelete = dao.getAllLocalImagePaths().mapNotNull { it.localImagePath }

        dao.deleteAllUserBooksAndData()

        pathsToDelete.forEach { editionImageStorage.delete(path = it) }
    }

    override suspend fun findOwnedListBookByEditionId(editionId: Int): ListBook? {
        return dao.getOwnedListBookByEditionId(editionId = editionId)?.toModel(isOwnedList = true)
    }

    override suspend fun getOwnedListId(): Int? {
        return dao.getOwnedListId()
    }

    override suspend fun removeOwnedListBookByEditionId(editionId: Int) {
        dao.deleteOwnedListBookByEditionId(editionId = editionId)
    }

    override suspend fun syncBookListMetadata(serverListIds: Set<Int>) {
        dao.syncBookListMetadata(serverListIds = serverListIds)
    }

    override suspend fun deleteOrphanBooks() {
        dao.deleteOrphanBooks()
    }

    override suspend fun redirectBookId(oldId: Int, newId: Int) {
        dao.redirectBookId(oldId = oldId, newId = newId)
    }
}