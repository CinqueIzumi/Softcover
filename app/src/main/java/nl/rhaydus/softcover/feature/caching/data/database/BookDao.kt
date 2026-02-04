package nl.rhaydus.softcover.feature.caching.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.caching.data.mapper.toBookAuthorRefs
import nl.rhaydus.softcover.feature.caching.data.mapper.toEditionAuthorRefs
import nl.rhaydus.softcover.feature.caching.data.mapper.toEntity
import nl.rhaydus.softcover.feature.caching.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.caching.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.caching.data.model.EditionAuthorCrossRef

@Dao
interface BookDao {
    // region Data fetchers
    @Transaction
    @Query("SELECT * FROM books ORDER BY userUpdatedAt DESC")
    fun observeBooks(): Flow<List<BookFullEntity>>

    @Transaction
    @Query("SELECT * from books WHERE statusCode = :statusCode ORDER BY userUpdatedAt DESC")
    fun getBooksByStatus(statusCode: Int): Flow<List<BookFullEntity>>

    @Query("SELECT userBookId FROM books")
    suspend fun getAllUserBookIds(): List<Int>

    @Query("SELECT * FROM authors WHERE name IN (:names)")
    suspend fun getAuthorsByName(names: List<String>): List<AuthorEntity>

    @Query("SELECT id FROM books WHERE userBookId = :userBookId")
    suspend fun getBookIdByUserBookId(userBookId: Int): Int?
    // endregion

    // region Data insertions
    @Transaction
    suspend fun cacheBook(book: Book) {
        // Insert book
        insertBook(book.toEntity())

        // Insert editions
        val editionEntities = book.editions.map { it.toEntity(book.id) }
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditions(editions: List<BookEditionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthors(authors: List<AuthorEntity>)

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

    @Transaction
    suspend fun deleteAllForUserBookId(userBookId: Int) {
        val bookId = getBookIdByUserBookId(userBookId) ?: return

        clearBookAuthors(bookId)
        clearEditionAuthors(bookId)
        deleteEditions(bookId)
        deleteBook(bookId)
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