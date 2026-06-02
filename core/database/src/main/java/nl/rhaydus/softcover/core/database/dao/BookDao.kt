package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.mapper.toBookAuthorRefs
import nl.rhaydus.softcover.core.database.mapper.toEditionAuthorRefs
import nl.rhaydus.softcover.core.database.mapper.toEntity
import nl.rhaydus.softcover.core.database.model.AuthorEntity
import nl.rhaydus.softcover.core.database.model.BookAuthorCrossRef
import nl.rhaydus.softcover.core.database.model.BookDeadlineEntity
import nl.rhaydus.softcover.core.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.BookFullEntity
import nl.rhaydus.softcover.core.database.model.BookListEntity
import nl.rhaydus.softcover.core.database.model.BookListWithBooks
import nl.rhaydus.softcover.core.database.model.BookSeriesEntity
import nl.rhaydus.softcover.core.database.model.BookTagCrossRef
import nl.rhaydus.softcover.core.database.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.core.database.model.EditionLocalImagePath
import nl.rhaydus.softcover.core.database.model.ListBookEntity
import nl.rhaydus.softcover.core.database.model.ListBookFull
import nl.rhaydus.softcover.core.database.model.ReadingJournalEntity
import nl.rhaydus.softcover.core.database.model.ShelfManualOrderEntity
import nl.rhaydus.softcover.core.database.model.TagEntity
import nl.rhaydus.softcover.core.database.model.UserBookEntity
import nl.rhaydus.softcover.core.database.model.UserBookReadEntity
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import timber.log.Timber

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
                SELECT userBookId, MAX(updatedAt) AS latestActivity
                FROM reading_journals
                GROUP BY userBookId
            ) rj ON ub.id = rj.userBookId
            ORDER BY
                (rj.latestActivity IS NULL) ASC,
                rj.latestActivity DESC,
                (ub.createdAt IS NULL) ASC,
                ub.createdAt DESC,
                ub.id DESC
            """,
    )
    fun observeBooks(): Flow<List<BookFullEntity>>

    /**
     * Dynamic-sort variant used by the library screen. The caller composes a SELECT against
     * `books` (and joins `user_books`) and supplies the ORDER BY fragment — see
     * `nl.rhaydus.softcover.core.book.data.sort.toOrderByFragment`. The observedEntities
     * list covers every table the supported ORDER BY fragments reach into via subquery so
     * Room re-emits whenever any of them changes.
     */
    @Transaction
    @RawQuery(
        observedEntities = [
            BookEntity::class,
            UserBookEntity::class,
            UserBookReadEntity::class,
            ReadingJournalEntity::class,
            BookEditionEntity::class,
            AuthorEntity::class,
            BookAuthorCrossRef::class,
            EditionAuthorCrossRef::class,
            BookSeriesEntity::class,
            BookDeadlineEntity::class,
            TagEntity::class,
            BookTagCrossRef::class,
            ShelfManualOrderEntity::class,
        ],
    )
    fun observeBooksRaw(query: SupportSQLiteQuery): Flow<List<BookFullEntity>>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Int): BookFullEntity?

    @Transaction
    @Query("SELECT * FROM book_lists")
    fun observeBookLists(): Flow<List<BookListWithBooks>>

    /**
     * Sorts user_books matching [statusCode] by the most recent
     * reading_journals.updatedAt whose event is in [events], descending.
     *
     * Books without any matching journal event fall to the bottom, ordered by
     * ub.id DESC as a stable tiebreaker. This can happen when the server
     * never emitted the expected status event for a record.
     */
    @Transaction
    @Query(
        """
                SELECT b.*
                FROM books b

                INNER JOIN user_books ub
                    ON ub.bookId = b.id

                LEFT JOIN (
                    SELECT userBookId, MAX(updatedAt) AS latestEvent
                    FROM reading_journals
                    WHERE event IN (:events)
                    GROUP BY userBookId
                ) rj
                    ON ub.id = rj.userBookId

                WHERE ub.statusCode = :statusCode

                ORDER BY
                    (rj.latestEvent IS NULL) ASC,
                    rj.latestEvent DESC,
                    ub.id DESC
            """,
    )
    fun getBooksByStatusAndEvents(
        statusCode: Int,
        events: List<String>,
    ): Flow<List<BookFullEntity>>

    @Transaction
    @Query(
        """
                SELECT b.*
                FROM books b

                INNER JOIN user_books ub
                    ON ub.bookId = b.id

                WHERE ub.statusCode = :statusCode

                ORDER BY
                    (ub.createdAt IS NULL) ASC,
                    ub.createdAt DESC,
                    ub.id DESC
            """,
    )
    fun getBooksByStatusSortedByCreatedAt(statusCode: Int): Flow<List<BookFullEntity>>

    @Transaction
    @Query(
        """
                SELECT b.*
                FROM books b

                INNER JOIN user_books ub
                    ON ub.bookId = b.id

                LEFT JOIN (
                    SELECT userBookId, MAX(finishedAt) AS latestFinishedAt
                    FROM user_book_reads
                    WHERE finishedAt IS NOT NULL
                    GROUP BY userBookId
                ) ubr
                    ON ub.id = ubr.userBookId

                LEFT JOIN (
                    SELECT userBookId, MAX(updatedAt) AS latestReadFinished
                    FROM reading_journals
                    WHERE event = 'user_book_read_finished'
                    GROUP BY userBookId
                ) rj
                    ON ub.id = rj.userBookId

                WHERE ub.statusCode = :statusCode

                ORDER BY
                    (ubr.latestFinishedAt IS NULL) ASC,
                    ubr.latestFinishedAt DESC,
                    (rj.latestReadFinished IS NULL) ASC,
                    rj.latestReadFinished DESC,
                    ub.id DESC
            """,
    )
    fun getReadBooks(statusCode: Int): Flow<List<BookFullEntity>>

    @Query("SELECT id FROM user_books")
    suspend fun getAllUserBookIds(): List<Int>

    @Query("SELECT id FROM user_books WHERE statusCode = :statusCode")
    suspend fun getUserBookIdsByStatus(statusCode: Int): List<Int>

    @Query("SELECT id FROM books WHERE id IN (:bookIds)")
    suspend fun getExistingBookIds(bookIds: List<Int>): List<Int>

    @Query("SELECT id FROM book_editions WHERE id IN (:editionIds)")
    suspend fun getExistingEditionIds(editionIds: List<Int>): List<Int>

    @Query("SELECT * FROM authors WHERE name IN (:names)")
    suspend fun getAuthorsByName(names: List<String>): List<AuthorEntity>

    @Query("SELECT id, localImagePath FROM book_editions WHERE id IN (:editionIds)")
    suspend fun getLocalImagePathsByEditionIds(editionIds: List<Int>): List<EditionLocalImagePath>

    @Query("SELECT id, localImagePath FROM book_editions WHERE bookId = :bookId")
    suspend fun getLocalImagePathsByBookId(bookId: Int): List<EditionLocalImagePath>

    @Query("SELECT id, localImagePath FROM book_editions")
    suspend fun getAllLocalImagePaths(): List<EditionLocalImagePath>

    @Query("UPDATE book_editions SET localImagePath = :path WHERE id = :editionId")
    suspend fun updateEditionLocalImagePath(
        editionId: Int,
        path: String?,
    )

    @Query("SELECT bookId FROM user_books WHERE id = :userBookId")
    suspend fun getBookIdByUserBookId(userBookId: Int): Int?

    @Query(
        """
            SELECT a.* 
            FROM authors a
            INNER JOIN edition_author_cross_ref ea 
            ON a.id = ea.authorId
            WHERE ea.editionId = :editionId
        """,
    )
    suspend fun getAuthorsForEdition(editionId: Int): List<AuthorEntity>

    @Transaction
    @Query(
        """
        SELECT lb.* 
        FROM list_books lb
        INNER JOIN book_lists bl ON lb.listId = bl.id
        WHERE lb.editionId = :editionId AND bl.slug = 'owned'
    """,
    )
    suspend fun getOwnedListBookByEditionId(editionId: Int): ListBookFull?

    @Query("SELECT id FROM book_lists WHERE slug = 'owned' LIMIT 1")
    suspend fun getOwnedListId(): Int?

    @Transaction
    @Query(
        """
        SELECT lb.*
        FROM list_books lb
        WHERE lb.listId = :listId AND lb.bookId = :bookId
        LIMIT 1
    """,
    )
    suspend fun getListBookByListAndBook(
        listId: Int,
        bookId: Int,
    ): ListBookFull?
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

        val userBook = book.userBook

        userBook?.id?.let { userBookId ->
            insertUserBook(userBook = userBook.toEntity(bookId = book.id))

            // Manually update all journals
            deleteJournals(userBookId = userBookId)

            insertJournals(journals = userBook.journals.map { it.toEntity(userBookId = userBookId) })

            book.userBookRead?.let { userBookRead ->
                insertUserBookRead(userBookRead.toEntity(userBookId = userBookId))
            }
        }

        // Insert editions
        insertEditions(toEntitiesPreservingLocalImagePath(editions = book.editions))

        // Insert authors (deduplicated)
        val allAuthors = (book.authors + book.editions.flatMap { it.authors })
            .distinctBy { it.name }
        insertAuthors(allAuthors.map { it.toEntity() })

        // Resolve IDs
        val authorEntities = getAuthorsByName(allAuthors.map { it.name })
        val authorIdsByName = authorEntities.associateBy(
            { it.name },
            { it.id },
        )

        // Cross references
        clearBookAuthors(book.id)
        insertBookAuthors(book.toBookAuthorRefs(authorIdsByName))

        clearEditionAuthorsByEditionIds(editionIds = book.editions.map { it.id })
        insertEditionAuthors(book.toEditionAuthorRefs(authorIdsByName))

        if (book.tags.isNotEmpty()) {
            insertTags(
                book.tags.map {
                    TagEntity(
                        id = it.id,
                        name = it.name,
                        category = it.category.name,
                    )
                },
            )
        }

        clearBookTags(bookId = book.id)

        if (book.tags.isNotEmpty()) {
            insertBookTags(
                book.tags.map {
                    BookTagCrossRef(
                        bookId = book.id,
                        tagId = it.id,
                        count = it.count,
                    )
                },
            )
        }
    }

    @Transaction
    suspend fun cacheBookLists(lists: List<BookList>) {
        lists.forEach { cacheBookList(it) }
    }

    @Transaction
    suspend fun cacheBookList(bookList: BookList) {
        clearBookList(bookListId = bookList.id)

        insertBookList(bookList.toEntity())

        val safeBooks = filterCacheableListBooks(books = bookList.books)

        val skipped = bookList.books.size - safeBooks.size

        if (skipped > 0) {
            Timber.w(
                "cacheBookList: skipped $skipped of ${bookList.books.size} list_books " +
                    "for listId=${bookList.id} due to missing book/edition rows",
            )
        }

        safeBooks.forEach { listBook ->
            insertListBook(listBook.toEntity())
        }
    }

    @Transaction
    suspend fun cacheListBook(listBook: ListBook) {
        if (canCacheListBook(listBook = listBook).not()) {
            Timber.w(
                "cacheListBook: skipped insert for listBookId=${listBook.listBookId} " +
                    "(bookId=${listBook.bookId}, editionId=${listBook.editionId}) — missing book/edition rows",
            )

            return
        }

        insertListBook(listBook.toEntity())
    }

    private suspend fun canCacheListBook(listBook: ListBook): Boolean {
        val hasBook = getExistingBookIds(bookIds = listOf(listBook.bookId)).isNotEmpty()
        val hasEdition = getExistingEditionIds(editionIds = listOf(listBook.editionId)).isNotEmpty()

        return hasBook && hasEdition
    }

    private suspend fun filterCacheableListBooks(books: List<ListBook>): List<ListBook> {
        if (books.isEmpty()) return books

        val cachedBookIds = getExistingBookIds(bookIds = books.map { it.bookId }.distinct()).toSet()
        val cachedEditionIds = getExistingEditionIds(editionIds = books.map { it.editionId }.distinct()).toSet()

        return books.filter { it.bookId in cachedBookIds && it.editionId in cachedEditionIds }
    }

    suspend fun toEntitiesPreservingLocalImagePath(
        editions: List<BookEdition>,
    ): List<BookEditionEntity> {
        if (editions.isEmpty()) return emptyList()

        val existing = getLocalImagePathsByEditionIds(editionIds = editions.map { it.id })
            .associateBy { it.id }

        return editions.map { edition ->
            val preserved = edition.localImagePath ?: existing[edition.id]?.localImagePath
            edition.toEntity().copy(localImagePath = preserved)
        }
    }

    @Transaction
    suspend fun cacheEditions(editions: List<BookEdition>) {
        if (editions.isEmpty()) return

        insertEditions(toEntitiesPreservingLocalImagePath(editions = editions))

        val allAuthors = editions.flatMap { it.authors }.distinctBy { it.name }

        if (allAuthors.isEmpty()) return

        insertAuthors(allAuthors.map { it.toEntity() })
        val authorEntities = getAuthorsByName(allAuthors.map { it.name })
        val authorIdsByName = authorEntities.associateBy(
            { it.name },
            { it.id },
        )

        val crossRefs = editions.flatMap { edition -> edition.toEditionAuthorRefs(authorIdsByName) }
        insertEditionAuthors(crossRefs)
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

    @Upsert
    suspend fun insertTags(tags: List<TagEntity>)

    @Upsert
    suspend fun insertBookTags(refs: List<BookTagCrossRef>)
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

    @Query("DELETE FROM user_books WHERE id = :userBookId")
    suspend fun deleteUserBookByUserBookId(userBookId: Int)

    @Query("SELECT COUNT(*) FROM list_books WHERE bookId = :bookId")
    suspend fun countListBooksForBook(bookId: Int): Int

    @Query("SELECT id FROM book_lists")
    suspend fun getAllBookListIds(): List<Int>

    @Query("DELETE FROM list_books WHERE listId IN (:listIds)")
    suspend fun deleteListBooksByListIds(listIds: List<Int>)

    @Query(
        """
        DELETE FROM list_books
        WHERE editionId = :editionId
          AND listId IN (SELECT id FROM book_lists WHERE slug = 'owned')
        """,
    )
    suspend fun deleteOwnedListBookByEditionId(editionId: Int)

    @Query("DELETE FROM list_books WHERE listBookId = :listBookId")
    suspend fun deleteListBookById(listBookId: Int)

    @Query(
        """
        DELETE FROM list_books
        WHERE listId = :listId AND bookId = :bookId AND listBookId = :listBookId
        """,
    )
    suspend fun deleteListBookByComposite(
        listId: Int,
        bookId: Int,
        listBookId: Int,
    )

    @Query("DELETE FROM book_lists WHERE id IN (:ids)")
    suspend fun deleteBookListsByIds(ids: List<Int>)

    @Query(
        """
        SELECT b.id FROM books b
        WHERE b.id NOT IN (SELECT bookId FROM user_books)
          AND b.id NOT IN (SELECT bookId FROM list_books)
        """,
    )
    suspend fun getOrphanBookIds(): List<Int>

    @Transaction
    suspend fun syncBookListMetadata(serverListIds: Set<Int>) {
        val allIds = getAllBookListIds()
        val toRemove = allIds.filterNot { it in serverListIds }

        if (toRemove.isEmpty()) return

        deleteListBooksByListIds(listIds = toRemove)
        deleteBookListsByIds(ids = toRemove)
    }

    @Query("UPDATE user_books SET bookId = :newId WHERE bookId = :oldId")
    suspend fun redirectUserBooksBookId(
        oldId: Int,
        newId: Int,
    )

    @Query("UPDATE OR REPLACE list_books SET bookId = :newId WHERE bookId = :oldId")
    suspend fun redirectListBooksBookId(
        oldId: Int,
        newId: Int,
    )

    @Transaction
    suspend fun redirectBookId(
        oldId: Int,
        newId: Int,
    ) {
        if (oldId == newId) return

        redirectUserBooksBookId(
            oldId = oldId,
            newId = newId,
        )

        redirectListBooksBookId(
            oldId = oldId,
            newId = newId,
        )
    }

    @Transaction
    suspend fun deleteOrphanBooks() {
        val orphanIds = getOrphanBookIds()

        orphanIds.forEach { bookId ->
            clearBookAuthors(bookId)
            clearEditionAuthors(bookId)
            clearBookTags(bookId = bookId)
            deleteEditions(bookId)
            deleteBook(bookId)
        }
    }

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

        deleteUserBookRead(userBookId)
        deleteJournals(userBookId = userBookId)
        deleteUserBookByUserBookId(userBookId)

        if (countListBooksForBook(bookId = bookId) > 0) return

        clearBookAuthors(bookId)
        clearEditionAuthors(bookId)
        clearBookTags(bookId = bookId)
        deleteEditions(bookId)
        deleteBook(bookId)
    }

    @Transaction
    suspend fun deleteAllUserBooksAndData() {
        deleteAllListBooks()
        deleteAllUserBookReads()
        deleteAllReadingJournals()
        deleteAllBookAuthorCrossRefs()
        deleteAllEditionAuthorCrossRefs()
        deleteAllBookTagCrossRefs()
        deleteAllShelfManualOrder()

        deleteAllUserBooks()
        deleteAllBookEditions()

        deleteAllBooks()
        deleteAllBookLists()
        deleteAllAuthors()
        deleteAllTags()
    }

    @Query("DELETE FROM book_author_cross_ref WHERE bookId = :bookId")
    suspend fun clearBookAuthors(bookId: Int)

    @Query(
        """
        DELETE FROM edition_author_cross_ref
        WHERE editionId IN (
            SELECT id FROM book_editions WHERE bookId = :bookId
        )
    """,
    )
    suspend fun clearEditionAuthors(bookId: Int)

    @Query("DELETE FROM edition_author_cross_ref WHERE editionId IN (:editionIds)")
    suspend fun clearEditionAuthorsByEditionIds(editionIds: List<Int>)

    @Query("DELETE FROM book_tag_cross_ref WHERE bookId = :bookId")
    suspend fun clearBookTags(bookId: Int)

    @Query("DELETE FROM book_tag_cross_ref")
    suspend fun deleteAllBookTagCrossRefs()

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()
    // endregion
    @Query("UPDATE book_lists SET ranked = :ranked WHERE id = :listId")
    suspend fun setBookListRanked(
        listId: Int,
        ranked: Boolean,
    )

    // region List book positions
    @Query(
        """
        UPDATE list_books
        SET position = NULL
        WHERE listId = :listId AND position IN (:positions)
        """,
    )
    suspend fun clearListBookPositions(
        listId: Int,
        positions: List<Int>,
    )

    @Query(
        """
        UPDATE list_books
        SET position = :position
        WHERE listBookId = :listBookId
        """,
    )
    suspend fun setListBookPosition(
        listBookId: Int,
        position: Int,
    )

    /**
     * Atomically rewrite a contiguous slice of [listId]'s positions: clears any rows currently
     * occupying [startPosition..startPosition + listBookIds.size - 1] and assigns the new
     * positions to [listBookIds] in order. Rows outside the slice are untouched — matching the
     * Hardcover web client's reorder semantics where only the dragged range is rewritten.
     */
    @Transaction
    suspend fun applyListBookPositionRange(
        listId: Int,
        startPosition: Int,
        listBookIds: List<Int>,
    ) {
        if (listBookIds.isEmpty()) return

        val positions = (startPosition until startPosition + listBookIds.size).toList()

        clearListBookPositions(
            listId = listId,
            positions = positions,
        )

        listBookIds.forEachIndexed { index, listBookId ->
            setListBookPosition(
                listBookId = listBookId,
                position = startPosition + index,
            )
        }
    }
    // endregion
    // region Shelf manual order
    @Query("SELECT * FROM shelf_manual_order WHERE statusCode = :statusCode ORDER BY position ASC")
    fun observeShelfManualOrder(statusCode: Int): Flow<List<ShelfManualOrderEntity>>

    @Upsert
    suspend fun upsertShelfManualOrder(rows: List<ShelfManualOrderEntity>)

    @Query("DELETE FROM shelf_manual_order WHERE statusCode = :statusCode AND position < :positionUpperBound")
    suspend fun deleteShelfManualOrderPrefix(
        statusCode: Int,
        positionUpperBound: Int,
    )

    @Query("DELETE FROM shelf_manual_order WHERE bookId IN (:bookIds)")
    suspend fun deleteShelfManualOrderForBookIds(bookIds: List<Int>)

    @Query("DELETE FROM shelf_manual_order")
    suspend fun deleteAllShelfManualOrder()

    /**
     * Atomically rewrite the **prefix** of the shelf's manual ordering with [prefixBookIds] —
     * positions `0..prefixBookIds.size - 1`. Positions beyond that range are left untouched, so a
     * shallow drag at the top of the shelf doesn't pin books the user never touched.
     *
     * The prefix is delete-then-upsert: any book that previously occupied a position in the
     * prefix range and is no longer present drops out (its position row is gone); any book in
     * [prefixBookIds] that previously had a position elsewhere is moved into the prefix range by
     * the upsert (PK is `statusCode, bookId`, so the old row is overwritten).
     */
    @Transaction
    suspend fun applyShelfManualOrderPrefix(
        statusCode: Int,
        prefixBookIds: List<Int>,
    ) {
        deleteShelfManualOrderPrefix(
            statusCode = statusCode,
            positionUpperBound = prefixBookIds.size,
        )

        if (prefixBookIds.isEmpty()) return

        upsertShelfManualOrder(
            rows = prefixBookIds.mapIndexed { index, bookId ->
                ShelfManualOrderEntity(
                    statusCode = statusCode,
                    bookId = bookId,
                    position = index,
                )
            },
        )
    }
    // endregion
}
