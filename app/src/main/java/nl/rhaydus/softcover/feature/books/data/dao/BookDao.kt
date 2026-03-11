package nl.rhaydus.softcover.feature.books.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.data.mapper.toBookAuthorRefs
import nl.rhaydus.softcover.feature.books.data.mapper.toEditionAuthorRefs
import nl.rhaydus.softcover.feature.books.data.mapper.toEntity
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListWithBooks
import nl.rhaydus.softcover.feature.books.data.model.BookSeriesEntity
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.ListBookEntity
import nl.rhaydus.softcover.feature.books.data.model.ListBookFull
import nl.rhaydus.softcover.feature.books.data.model.ReadingJournalEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookReadEntity

@Dao
interface BookDao {
    // region Data fetchers
    @Transaction
    @Query(
        """
            SELECT b.*
            FROM books b
            LEFT JOIN user_books ub ON ub.bookId = b.id
            LEFT JOIN (
                SELECT userBookId, MAX(updatedAt) AS latestProgress
                FROM reading_journals
                WHERE event = 'progress_updated'
                GROUP BY userBookId
            ) rj ON ub.id = rj.userBookId
            ORDER BY
                (rj.latestProgress IS NULL) ASC,
                rj.latestProgress DESC,
                ub.updatedAt DESC
            """
    )
    fun observeBooks(): Flow<List<BookFullEntity>>

    @Transaction
    @Query("SELECT * FROM book_lists")
    fun observeBookLists(): Flow<List<BookListWithBooks>>

    @Transaction
    @Query(
        """
                SELECT b.*
                FROM books b

                INNER JOIN user_books ub 
                    ON ub.bookId = b.id

                LEFT JOIN (
                    SELECT userBookId, MAX(updatedAt) AS latestProgress
                    FROM reading_journals
                    WHERE event = 'progress_updated'
                    GROUP BY userBookId
                ) rj 
                    ON ub.id = rj.userBookId

                WHERE ub.statusCode = :statusCode

                ORDER BY
                    (rj.latestProgress IS NULL) ASC,
                    rj.latestProgress DESC,
                    ub.updatedAt DESC
            """
    )
    fun getBooksByStatus(statusCode: Int): Flow<List<BookFullEntity>>

    @Query("SELECT id FROM user_books")
    suspend fun getAllUserBookIds(): List<Int>

    @Query("SELECT * FROM authors WHERE name IN (:names)")
    suspend fun getAuthorsByName(names: List<String>): List<AuthorEntity>

    @Query("SELECT bookId FROM user_books WHERE id = :userBookId")
    suspend fun getBookIdByUserBookId(userBookId: Int): Int?

    @Query(
        """
            SELECT a.* 
            FROM authors a
            INNER JOIN edition_author_cross_ref ea 
            ON a.id = ea.authorId
            WHERE ea.editionId = :editionId
        """
    )
    suspend fun getAuthorsForEdition(editionId: Int): List<AuthorEntity>

    @Transaction
    @Query(
        """
        SELECT lb.* 
        FROM list_books lb
        INNER JOIN book_lists bl ON lb.listId = bl.id
        WHERE lb.editionId = :editionId AND bl.slug = 'owned'
    """
    )
    suspend fun getOwnedListBookByEditionId(editionId: Int): ListBookFull?
    // endregion

    // region Data insertions
    @Transaction
    suspend fun cacheBooks(books: List<Book>) {
        books.forEach { cacheBook(it) }
    }

    @Transaction
    suspend fun cacheBook(book: Book) {
        book.bookSeries?.let { series ->
            insertBookSeries(bookSeries = series.toEntity())
        }

        insertBook(book.toEntity())

        book.userBook?.id?.let { userBookId ->
            insertUserBook(userBook = book.userBook.toEntity(bookId = book.id))

            // Manually update all journals
            deleteJournals(userBookId = userBookId)

            insertJournals(journals = book.userBook.journals.map { it.toEntity(userBookId = userBookId) })

            book.userBookRead?.let { userBookRead ->
                insertUserBookRead(userBookRead.toEntity(userBookId = userBookId))
            }
        }

        // Insert editions
        val editionEntities = book.editions.map { it.toEntity() }
        insertEditions(editionEntities)

        // Insert authors (deduplicated)
        val allAuthors = (book.authors + book.editions.flatMap { it.authors })
            .distinctBy { it.name }
        insertAuthors(allAuthors.map { it.toEntity() })

        // Resolve IDs
        val authorEntities = getAuthorsByName(allAuthors.map { it.name })
        val authorIdsByName = authorEntities.associateBy({ it.name }, { it.id })

        // Cross references
        clearBookAuthors(book.id)
        insertBookAuthors(book.toBookAuthorRefs(authorIdsByName))

        clearEditionAuthors(book.id)
        insertEditionAuthors(book.toEditionAuthorRefs(authorIdsByName))
    }

    @Transaction
    suspend fun cacheBookLists(lists: List<BookList>) {
        lists.forEach { cacheBookList(it) }
    }

    @Transaction
    suspend fun cacheBookList(bookList: BookList) {
        clearBookList(bookListId = bookList.id)

        insertBookList(bookList.toEntity())

        bookList.books.forEach { listBook ->
            cacheListBook(listBook)
        }
    }

    @Transaction
    suspend fun cacheListBook(listBook: ListBook) {
        val book = listBook.book
        val edition = listBook.edition

        insertBook(book.toEntity())
        insertEditions(listOf(edition.toEntity()))
        insertListBook(listBook.toEntity())

        val allAuthors = (book.authors + edition.authors).distinctBy { it.name }
        insertAuthors(allAuthors.map { it.toEntity() })

        val authorEntities = getAuthorsByName(allAuthors.map { it.name })
        val authorIdsByName = authorEntities.associateBy({ it.name }, { it.id })

        insertBookAuthors(book.toBookAuthorRefs(authorIdsByName))
        insertEditionAuthors(edition.toEditionAuthorRefs(authorIdsByName))
    }

    @Upsert
    suspend fun insertBook(book: BookEntity)

    @Upsert
    suspend fun insertBookList(bookList: BookListEntity)

    @Upsert
    suspend fun insertBookSeries(bookSeries: BookSeriesEntity)

    @Upsert
    suspend fun insertUserBook(userBook: UserBookEntity)

    @Upsert
    suspend fun insertUserBookRead(userBookRead: UserBookReadEntity)

    @Upsert
    suspend fun insertEditions(editions: List<BookEditionEntity>)

    @Upsert
    suspend fun insertListBook(listBook: ListBookEntity)

    @Upsert
    suspend fun insertAuthors(authors: List<AuthorEntity>)

    @Upsert
    suspend fun insertJournals(journals: List<ReadingJournalEntity>)

    @Upsert
    suspend fun insertBookAuthors(refs: List<BookAuthorCrossRef>)

    @Upsert
    suspend fun insertEditionAuthors(refs: List<EditionAuthorCrossRef>)
    // endregion

    // region Data removers
    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("DELETE FROM book_lists")
    suspend fun deleteAllBookLists()

    @Query("DELETE FROM user_books")
    suspend fun deleteAllUserBooks()

    @Query("DELETE FROM user_book_reads")
    suspend fun deleteAllUserBookReads()

    @Query("DELETE FROM book_editions")
    suspend fun deleteAllBookEditions()

    @Query("DELETE FROM list_books")
    suspend fun deleteAllListBooks()

    @Query("DELETE FROM authors")
    suspend fun deleteAllAuthors()

    @Query("DELETE FROM reading_journals")
    suspend fun deleteAllReadingJournals()

    @Query("DELETE FROM book_author_cross_ref")
    suspend fun deleteAllBookAuthorCrossRefs()

    @Query("DELETE FROM edition_author_cross_ref")
    suspend fun deleteAllEditionAuthorCrossRefs()

    @Query("DELETE FROM book_editions WHERE bookId = :bookId")
    suspend fun deleteEditions(bookId: Int)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: Int)

    @Query("DELETE FROM reading_journals WHERE userBookId = :userBookId")
    suspend fun deleteJournals(userBookId: Int)

    @Query("DELETE FROM user_books WHERE bookId = :bookId")
    suspend fun deleteUserBook(bookId: Int)

    @Query("DELETE FROM user_book_reads WHERE userBookId = :userBookId")
    suspend fun deleteUserBookRead(userBookId: Int)

    @Query("DELETE FROM list_books WHERE listId = :bookListId")
    suspend fun clearBookList(bookListId: Int)

    @Transaction
    suspend fun deleteUserBooksByIds(ids: List<Int>) {
        ids.forEach { id ->
            deleteAllForUserBookId(id)
        }
    }

    @Transaction
    suspend fun deleteAllForUserBookId(userBookId: Int) {
        val bookId = getBookIdByUserBookId(userBookId) ?: return

        clearBookAuthors(bookId)
        clearEditionAuthors(bookId)
        deleteEditions(bookId)
        deleteBook(bookId)
        deleteUserBook(bookId)
        deleteUserBookRead(userBookId)
        deleteJournals(userBookId = userBookId)
    }

    @Transaction
    suspend fun deleteAllUserBooksAndData() {
        deleteAllListBooks()
        deleteAllUserBookReads()
        deleteAllReadingJournals()
        deleteAllBookAuthorCrossRefs()
        deleteAllEditionAuthorCrossRefs()

        deleteAllUserBooks()
        deleteAllBookEditions()

        deleteAllBooks()
        deleteAllBookLists()
        deleteAllAuthors()
    }

    @Query("DELETE FROM book_author_cross_ref WHERE bookId = :bookId")
    suspend fun clearBookAuthors(bookId: Int)

    @Query(
        """
        DELETE FROM edition_author_cross_ref
        WHERE editionId IN (
            SELECT id FROM book_editions WHERE bookId = :bookId
        )
    """
    )
    suspend fun clearEditionAuthors(bookId: Int)
    // endregion
}