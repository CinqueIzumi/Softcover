package nl.rhaydus.softcover.feature.books.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.books.data.mapper.toBookAuthorRefs
import nl.rhaydus.softcover.feature.books.data.mapper.toEditionAuthorRefs
import nl.rhaydus.softcover.feature.books.data.mapper.toEntity
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEditionCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListWithEditions
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef
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
    fun observeBookLists(): Flow<List<BookListWithEditions>>

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
    // endregion

    // region Data insertions
    @Transaction
    suspend fun cacheBook(book: Book) {
        // Insert book
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
    suspend fun cacheBookList(bookList: BookList) {
        insertBookList(bookList = bookList.toEntity())

        bookList.editions.forEach { currentEdition ->
            insertEditions(editions = listOf(currentEdition.toEntity()))

            insertAuthors(authors = currentEdition.authors.map { it.toEntity() })

            val editionAuthorCrossRefs = currentEdition.authors.map { author ->
                EditionAuthorCrossRef(
                    editionId = currentEdition.id,
                    authorId = author.id,
                )
            }
            insertEditionAuthors(refs = editionAuthorCrossRefs)

            val bookListEditionRef = BookListEditionCrossRef(
                bookListId = bookList.id,
                editionId = currentEdition.id,
            )
            insertBookListEditions(refs = listOf(bookListEditionRef))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookList(bookList: BookListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBook(userBook: UserBookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBookRead(userBookRead: UserBookReadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditions(editions: List<BookEditionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookListEditions(refs: List<BookListEditionCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthors(authors: List<AuthorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournals(journals: List<ReadingJournalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookAuthors(refs: List<BookAuthorCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditionAuthors(refs: List<EditionAuthorCrossRef>)
    // endregion

    // region Data removers
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
        val bookIds = getAllUserBookIds()

        bookIds.forEach {
            deleteAllForUserBookId(userBookId = it)
        }
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