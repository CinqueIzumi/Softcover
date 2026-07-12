package nl.rhaydus.softcover.core.database.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.model.AuthorEntity
import nl.rhaydus.softcover.core.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.database.model.BookEditionView
import nl.rhaydus.softcover.core.database.model.BookEditionWithAuthors
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.BookFullEntity
import nl.rhaydus.softcover.core.database.model.BookListEntity
import nl.rhaydus.softcover.core.database.model.BookListWithBooks
import nl.rhaydus.softcover.core.database.model.BookSeriesEntity
import nl.rhaydus.softcover.core.database.model.ListBookEntity
import nl.rhaydus.softcover.core.database.model.ListBookFull
import nl.rhaydus.softcover.core.database.model.ReadingJournalEntity
import nl.rhaydus.softcover.core.database.model.UserBookEntity
import nl.rhaydus.softcover.core.database.model.UserBookReadEntity
import nl.rhaydus.softcover.core.database.model.UserBookWithJournals
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ListBook

class ListEntityMapperTest {
    // region Shared stubs for UI -> Entity section
    private fun stubBookList(
        id: Int = 20,
        name: String = "My List",
        slug: String = "my-list",
        ranked: Boolean = false,
        books: List<ListBook> = emptyList(),
        signature: String? = null,
    ): BookList = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.name
        } returns name
        every {
            this@mockk.slug
        } returns slug
        every {
            this@mockk.ranked
        } returns ranked
        every {
            this@mockk.books
        } returns books
        every {
            this@mockk.signature
        } returns signature
    }

    private fun stubListBook(
        listId: Int = 20,
        listBookId: Int = 99,
        bookId: Int = 1,
        editionId: Int = 10,
        position: Int? = null,
        addedAt: String? = null,
        book: Book? = null,
        edition: BookEdition? = null,
    ): ListBook = mockk {
        every {
            this@mockk.listId
        } returns listId
        every {
            this@mockk.listBookId
        } returns listBookId
        every {
            this@mockk.bookId
        } returns bookId
        every {
            this@mockk.editionId
        } returns editionId
        every {
            this@mockk.position
        } returns position
        every {
            this@mockk.addedAt
        } returns addedAt
        every {
            this@mockk.book
        } returns book
        every {
            this@mockk.edition
        } returns edition
    }
    // endregion
    // region Shared stubs for Entity -> Model section
    private fun stubBookEditionEntity(
        id: Int = 10,
        canonicalId: Int? = null,
        bookId: Int = 1,
        publisher: String? = "Publisher",
        title: String? = "Edition Title",
        url: String? = "https://example.com/cover.jpg",
        localImagePath: String? = null,
        isbn10: String? = "1234567890",
        isbn13: String? = null,
        pages: Int? = 300,
        audioSeconds: Int? = null,
        releaseYear: Int = 2020,
        releaseDate: String? = null,
        format: String = "Paperback",
    ): BookEditionEntity = BookEditionEntity(
        id = id,
        canonicalId = canonicalId,
        bookId = bookId,
        publisher = publisher,
        title = title,
        url = url,
        localImagePath = localImagePath,
        isbn10 = isbn10,
        isbn13 = isbn13,
        pages = pages,
        audioSeconds = audioSeconds,
        releaseYear = releaseYear,
        releaseDate = releaseDate,
        format = format,
        readingFormatId = null,
    )

    private fun stubBookEditionView(
        entity: BookEditionEntity = stubBookEditionEntity(),
        isOwned: Boolean = false,
    ): BookEditionView = BookEditionView(
        edition = entity,
        isOwned = isOwned,
    )

    private fun stubBookEditionWithAuthors(
        editionView: BookEditionView = stubBookEditionView(),
        authors: List<AuthorEntity> = emptyList(),
    ): BookEditionWithAuthors = BookEditionWithAuthors(
        edition = editionView,
        authors = authors,
    )

    private fun stubBookEntity(
        id: Int = 1,
        title: String = "Test Book",
        defaultEditionId: Int? = null,
        rating: Double = 4.2,
        headline: String = "",
        description: String = "A great book.",
        releaseYear: Int = 2019,
        releaseDate: String? = null,
        coverUrl: String = "https://example.com/book.jpg",
        usersCount: Int = 100,
        ratingsCount: Int = 0,
        positionsInSeries: String = "",
        isCompilation: Boolean = false,
        seriesId: Int? = null,
    ): BookEntity = BookEntity(
        id = id,
        title = title,
        defaultEditionId = defaultEditionId,
        rating = rating,
        headline = headline,
        description = description,
        releaseYear = releaseYear,
        releaseDate = releaseDate,
        coverUrl = coverUrl,
        usersCount = usersCount,
        ratingsCount = ratingsCount,
        positionsInSeries = positionsInSeries,
        isCompilation = isCompilation,
        seriesId = seriesId,
    )

    private fun stubUserBookEntity(
        id: Int = 3,
        bookId: Int = 1,
        statusCode: Int = BookStatus.Reading.code,
        dateAdded: String = "2024-01-01",
        createdAt: String? = null,
        privacySettingId: Int = 1,
        reviewHasSpoilers: Boolean = false,
        editionId: Int? = 10,
        lastReadDate: String? = null,
        rating: Double? = null,
        referrerUserId: Int? = null,
        reviewedAt: String? = null,
        updatedAt: String? = null,
    ): UserBookEntity = UserBookEntity(
        id = id,
        bookId = bookId,
        statusCode = statusCode,
        dateAdded = dateAdded,
        createdAt = createdAt,
        privacySettingId = privacySettingId,
        reviewHasSpoilers = reviewHasSpoilers,
        editionId = editionId,
        lastReadDate = lastReadDate,
        rating = rating,
        referrerUserId = referrerUserId,
        reviewedAt = reviewedAt,
        updatedAt = updatedAt,
    )

    private fun stubUserBookWithJournals(
        userBook: UserBookEntity = stubUserBookEntity(),
        journals: List<ReadingJournalEntity> = emptyList(),
        userBookRead: UserBookReadEntity? = null,
    ): UserBookWithJournals = UserBookWithJournals(
        userBook = userBook,
        journals = journals,
        userBookRead = userBookRead,
    )

    private fun stubBookFullEntity(
        book: BookEntity = stubBookEntity(),
        bookAuthors: List<AuthorEntity> = emptyList(),
        series: BookSeriesEntity? = null,
        editions: List<BookEditionWithAuthors> = emptyList(),
        userBookWithJournals: UserBookWithJournals? = null,
    ): BookFullEntity = BookFullEntity(
        book = book,
        bookAuthors = bookAuthors,
        series = series,
        editions = editions,
        userBookWithJournals = userBookWithJournals,
        tags = emptyList(),
    )

    private fun stubListBookEntity(
        listId: Int = 20,
        bookId: Int = 1,
        editionId: Int = 10,
        listBookId: Int = 99,
        position: Int? = null,
        addedAt: String? = null,
    ): ListBookEntity = ListBookEntity(
        listId = listId,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        position = position,
        addedAt = addedAt,
    )

    private fun stubListBookFull(
        listBook: ListBookEntity = stubListBookEntity(),
        book: BookFullEntity? = stubBookFullEntity(),
        edition: BookEditionWithAuthors? = stubBookEditionWithAuthors(),
    ): ListBookFull = ListBookFull(
        listBook = listBook,
        book = book,
        edition = edition,
    )

    private fun stubBookListEntity(
        id: Int = 20,
        name: String = "My List",
        slug: String = "my-list",
        ranked: Boolean = false,
        signature: String? = null,
    ): BookListEntity = BookListEntity(
        id = id,
        name = name,
        slug = slug,
        ranked = ranked,
        signature = signature,
    )

    private fun stubBookListWithBooks(
        bookList: BookListEntity = stubBookListEntity(),
        listBooks: List<ListBookFull> = emptyList(),
    ): BookListWithBooks = BookListWithBooks(
        bookList = bookList,
        listBooks = listBooks,
    )
    // endregion
    // =========================================================
    // UI -> Entity mappers
    // =========================================================

    @Nested
    inner class BookListToEntity {
        @Test
        fun `maps all fields from BookList to BookListEntity`() {
            // ----- Arrange -----
            val bookList = stubBookList(
                id = 20,
                name = "My List",
                slug = "my-list",
            )

            // ----- Act -----
            val result = bookList.toEntity()

            // ----- Assert -----
            result.id shouldBe 20
            result.name shouldBe "My List"
            result.slug shouldBe "my-list"
        }

        @Test
        fun `writes ranked=true to entity`() {
            // ----- Arrange -----
            val bookList = stubBookList(ranked = true)

            // ----- Act -----
            val result = bookList.toEntity()

            // ----- Assert -----
            result.ranked shouldBe true
        }

        @Test
        fun `writes ranked=false to entity`() {
            // ----- Arrange -----
            val bookList = stubBookList(ranked = false)

            // ----- Act -----
            val result = bookList.toEntity()

            // ----- Assert -----
            result.ranked shouldBe false
        }

        @Test
        fun `carries non-null signature through to entity`() {
            // ----- Arrange -----
            val bookList = stubBookList(signature = "abc123")

            // ----- Act -----
            val result = bookList.toEntity()

            // ----- Assert -----
            result.signature shouldBe "abc123"
        }

        @Test
        fun `carries null signature through to entity`() {
            // ----- Arrange -----
            val bookList = stubBookList(signature = null)

            // ----- Act -----
            val result = bookList.toEntity()

            // ----- Assert -----
            result.signature shouldBe null
        }
    }

    @Nested
    inner class ListBookToEntity {
        @Test
        fun `maps listId, bookId, editionId and listBookId directly from ListBook fields`() {
            // ----- Arrange -----
            val listBook = stubListBook(
                listId = 20,
                listBookId = 99,
                bookId = 1,
                editionId = 10,
            )

            // ----- Act -----
            val result = listBook.toEntity()

            // ----- Assert -----
            result.listId shouldBe 20
            result.bookId shouldBe 1
            result.editionId shouldBe 10
            result.listBookId shouldBe 99
        }

        @Test
        fun `propagates addedAt when present`() {
            // ----- Arrange -----
            val listBook = stubListBook(addedAt = "2024-06-01")

            // ----- Act -----
            val result = listBook.toEntity()

            // ----- Assert -----
            result.addedAt shouldBe "2024-06-01"
        }

        @Test
        fun `propagates null addedAt as null`() {
            // ----- Arrange -----
            val listBook = stubListBook(addedAt = null)

            // ----- Act -----
            val result = listBook.toEntity()

            // ----- Assert -----
            result.addedAt shouldBe null
        }

        @Test
        fun `writes position when present`() {
            // ----- Arrange -----
            val listBook = stubListBook(position = 5)

            // ----- Act -----
            val result = listBook.toEntity()

            // ----- Assert -----
            result.position shouldBe 5
        }

        @Test
        fun `writes null position as null`() {
            // ----- Arrange -----
            val listBook = stubListBook(position = null)

            // ----- Act -----
            val result = listBook.toEntity()

            // ----- Assert -----
            result.position shouldBe null
        }
    }

    // =========================================================
    // Entity -> Model mappers
    // =========================================================

    @Nested
    inner class ListBookFullToModel {
        @Test
        fun `maps listId, listBookId, bookId and editionId from ListBookEntity`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(
                listId = 20,
                listBookId = 99,
                bookId = 1,
                editionId = 10,
            )
            val editionEntity = stubBookEditionEntity(
                id = 10,
                bookId = 1,
            )
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = false,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(
                editionView = editionView,
                authors = emptyList(),
            )
            val bookFullEntity = stubBookFullEntity(book = stubBookEntity(id = 1))
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = editionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.listId shouldBe 20
            result.listBookId shouldBe 99
            result.bookId shouldBe 1
            result.editionId shouldBe 10
        }

        @Test
        fun `populates book and edition from Room join data`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = 10,
            )
            val editionEntity = stubBookEditionEntity(
                id = 10,
                bookId = 1,
            )
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = false,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(editionView = editionView)
            val bookFullEntity = stubBookFullEntity(
                book = stubBookEntity(id = 1),
                editions = listOf(editionWithAuthors),
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = editionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.book shouldBe bookFullEntity.toModel()
            result.edition?.id shouldBe 10
        }

        @Test
        fun `maps edition owned flag from BookEditionView isOwned`() {
            // ----- Arrange -----
            val editionEntity = stubBookEditionEntity(id = 10)
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = true,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(editionView = editionView)
            val listBookFull = stubListBookFull(edition = editionWithAuthors)

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.edition?.owned shouldBe true
        }

        @Test
        fun `maps edition owned as false when isOwned is false`() {
            // ----- Arrange -----
            val editionEntity = stubBookEditionEntity(id = 10)
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = false,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(editionView = editionView)
            val listBookFull = stubListBookFull(edition = editionWithAuthors)

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.edition?.owned shouldBe false
        }

        @Test
        fun `copies addedAt from ListBookEntity`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(addedAt = "2024-07-20")
            val listBookFull = stubListBookFull(listBook = listBookEntity)

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.addedAt shouldBe "2024-07-20"
        }

        @Test
        fun `copies null addedAt from ListBookEntity`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(addedAt = null)
            val listBookFull = stubListBookFull(listBook = listBookEntity)

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.addedAt shouldBe null
        }

        @Test
        fun `copies position from ListBookEntity when present`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(position = 7)
            val listBookFull = stubListBookFull(listBook = listBookEntity)

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.position shouldBe 7
        }

        @Test
        fun `copies null position from ListBookEntity`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(position = null)
            val listBookFull = stubListBookFull(listBook = listBookEntity)

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.position shouldBe null
        }

        @Test
        fun `returns null book when book relation is null`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(listBookId = 42)
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = null,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.book shouldBe null
            result.listBookId shouldBe 42
        }

        @Test
        fun `returns null edition when edition relation is null`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(listBookId = 43)
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                edition = null,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.edition shouldBe null
            result.listBookId shouldBe 43
        }

        @Test
        fun `prefers userBook editionId over defaultEditionId when edition exists in book editions`() {
            // ----- Arrange -----
            val userBookEditionId = 55
            val defaultEditionId = 99
            val topLevelEditionId = 10

            val userBookEditionEntity = stubBookEditionEntity(
                id = userBookEditionId,
                bookId = 1,
            )
            val userBookEditionView = stubBookEditionView(
                entity = userBookEditionEntity,
                isOwned = false,
            )
            val userBookEditionWithAuthors = stubBookEditionWithAuthors(editionView = userBookEditionView)

            val topLevelEditionEntity = stubBookEditionEntity(
                id = topLevelEditionId,
                bookId = 1,
            )
            val topLevelEditionView = stubBookEditionView(
                entity = topLevelEditionEntity,
                isOwned = false,
            )
            val topLevelEditionWithAuthors = stubBookEditionWithAuthors(editionView = topLevelEditionView)

            val userBookEntity = stubUserBookEntity(editionId = userBookEditionId)
            val userBookWithJournals = stubUserBookWithJournals(userBook = userBookEntity)

            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = defaultEditionId,
            )
            val bookFullEntity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(userBookEditionWithAuthors),
                userBookWithJournals = userBookWithJournals,
            )

            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = topLevelEditionId,
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = topLevelEditionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.edition?.id shouldBe userBookEditionId
            result.editionId shouldBe userBookEditionId
        }

        @Test
        fun `falls back to defaultEditionId when no userBook editionId but matching edition exists`() {
            // ----- Arrange -----
            val defaultEditionId = 77
            val topLevelEditionId = 10

            val defaultEditionEntity = stubBookEditionEntity(
                id = defaultEditionId,
                bookId = 1,
            )
            val defaultEditionView = stubBookEditionView(
                entity = defaultEditionEntity,
                isOwned = false,
            )
            val defaultEditionWithAuthors = stubBookEditionWithAuthors(editionView = defaultEditionView)

            val topLevelEditionEntity = stubBookEditionEntity(
                id = topLevelEditionId,
                bookId = 1,
            )
            val topLevelEditionView = stubBookEditionView(
                entity = topLevelEditionEntity,
                isOwned = false,
            )
            val topLevelEditionWithAuthors = stubBookEditionWithAuthors(editionView = topLevelEditionView)

            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = defaultEditionId,
            )
            val bookFullEntity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(defaultEditionWithAuthors),
                userBookWithJournals = null,
            )

            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = topLevelEditionId,
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = topLevelEditionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.edition?.id shouldBe defaultEditionId
            result.editionId shouldBe defaultEditionId
        }

        @Test
        fun `falls back to top-level edition when preferred id has no matching entry in book editions`() {
            // ----- Arrange -----
            val topLevelEditionId = 10

            val topLevelEditionEntity = stubBookEditionEntity(
                id = topLevelEditionId,
                bookId = 1,
            )
            val topLevelEditionView = stubBookEditionView(
                entity = topLevelEditionEntity,
                isOwned = false,
            )
            val topLevelEditionWithAuthors = stubBookEditionWithAuthors(editionView = topLevelEditionView)

            // defaultEditionId 99 is not present in the editions list, triggering fallback
            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = 99,
            )
            val bookFullEntity = stubBookFullEntity(
                book = bookEntity,
                editions = emptyList(),
                userBookWithJournals = null,
            )

            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = topLevelEditionId,
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = topLevelEditionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = false)

            // ----- Assert -----
            result.edition?.id shouldBe topLevelEditionId
            result.editionId shouldBe topLevelEditionId
        }

        @Test
        fun `uses list_books edition_id when isOwnedList is true even if userBook has a different edition`() {
            // ----- Arrange -----
            val topLevelEditionId = 10
            val userBookEditionId = 55

            val topLevelEditionEntity = stubBookEditionEntity(
                id = topLevelEditionId,
                bookId = 1,
            )
            val topLevelEditionView = stubBookEditionView(
                entity = topLevelEditionEntity,
                isOwned = false,
            )
            val topLevelEditionWithAuthors = stubBookEditionWithAuthors(editionView = topLevelEditionView)

            val userBookEditionEntity = stubBookEditionEntity(
                id = userBookEditionId,
                bookId = 1,
            )
            val userBookEditionView = stubBookEditionView(
                entity = userBookEditionEntity,
                isOwned = false,
            )
            val userBookEditionWithAuthors = stubBookEditionWithAuthors(editionView = userBookEditionView)

            val userBookEntity = stubUserBookEntity(editionId = userBookEditionId)
            val userBookWithJournals = stubUserBookWithJournals(userBook = userBookEntity)

            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = userBookEditionId,
            )
            val bookFullEntity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(topLevelEditionWithAuthors, userBookEditionWithAuthors),
                userBookWithJournals = userBookWithJournals,
            )

            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = topLevelEditionId,
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = topLevelEditionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = true)

            // ----- Assert -----
            result.edition?.id shouldBe topLevelEditionId
            result.editionId shouldBe topLevelEditionId
        }

        @Test
        fun `uses list_books edition_id when isOwnedList is true even if defaultEditionId differs`() {
            // ----- Arrange -----
            val topLevelEditionId = 10
            val defaultEditionId = 77

            val topLevelEditionEntity = stubBookEditionEntity(
                id = topLevelEditionId,
                bookId = 1,
            )
            val topLevelEditionView = stubBookEditionView(
                entity = topLevelEditionEntity,
                isOwned = false,
            )
            val topLevelEditionWithAuthors = stubBookEditionWithAuthors(editionView = topLevelEditionView)

            val defaultEditionEntity = stubBookEditionEntity(
                id = defaultEditionId,
                bookId = 1,
            )
            val defaultEditionView = stubBookEditionView(
                entity = defaultEditionEntity,
                isOwned = false,
            )
            val defaultEditionWithAuthors = stubBookEditionWithAuthors(editionView = defaultEditionView)

            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = defaultEditionId,
            )
            val bookFullEntity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(topLevelEditionWithAuthors, defaultEditionWithAuthors),
                userBookWithJournals = null,
            )

            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = topLevelEditionId,
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = topLevelEditionWithAuthors,
            )

            // ----- Act -----
            val result = listBookFull.toModel(isOwnedList = true)

            // ----- Assert -----
            result.edition?.id shouldBe topLevelEditionId
            result.editionId shouldBe topLevelEditionId
        }
    }

    @Nested
    inner class BookListWithBooksToModel {
        @Test
        fun `maps id, name and slug from BookListEntity`() {
            // ----- Arrange -----
            val bookListEntity = stubBookListEntity(
                id = 20,
                name = "My List",
                slug = "my-list",
            )
            val wrapper = stubBookListWithBooks(
                bookList = bookListEntity,
                listBooks = emptyList(),
            )

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.id shouldBe 20
            result.name shouldBe "My List"
            result.slug shouldBe "my-list"
        }

        @Test
        fun `maps empty listBooks to empty books list`() {
            // ----- Arrange -----
            val wrapper = stubBookListWithBooks(listBooks = emptyList())

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books shouldBe emptyList()
        }

        @Test
        fun `maps each ListBookFull to a ListBook model`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(
                listId = 20,
                listBookId = 99,
            )
            val listBookFull = stubListBookFull(listBook = listBookEntity)
            val wrapper = stubBookListWithBooks(listBooks = listOf(listBookFull))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books.size shouldBe 1
            result.books[0].listId shouldBe 20
            result.books[0].listBookId shouldBe 99
        }

        @Test
        fun `sorts list books by addedAt descending`() {
            // ----- Arrange -----
            val older = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 1,
                    addedAt = "2024-01-01",
                ),
            )
            val newer = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 2,
                    addedAt = "2024-06-01",
                ),
            )
            val wrapper = stubBookListWithBooks(listBooks = listOf(older, newer))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 2
            result.books[1].listBookId shouldBe 1
        }

        @Test
        fun `places null addedAt entries last`() {
            // ----- Arrange -----
            val withDate = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 1,
                    addedAt = "2024-01-01",
                ),
            )
            val nullDate = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 2,
                    addedAt = null,
                ),
            )
            val wrapper = stubBookListWithBooks(listBooks = listOf(nullDate, withDate))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 1
            result.books[1].listBookId shouldBe 2
        }

        @Test
        fun `uses listBookId descending as tiebreaker when addedAt values are equal`() {
            // ----- Arrange -----
            val lowerIdEntry = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 10,
                    addedAt = "2024-03-01",
                ),
            )
            val higherIdEntry = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 20,
                    addedAt = "2024-03-01",
                ),
            )
            val wrapper = stubBookListWithBooks(listBooks = listOf(lowerIdEntry, higherIdEntry))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 20
            result.books[1].listBookId shouldBe 10
        }

        @Test
        fun `drops list entries whose book or edition relation is null`() {
            // ----- Arrange -----
            val fullyHydrated = stubListBookFull(
                listBook = stubListBookEntity(listBookId = 1),
            )
            val nullBook = stubListBookFull(
                listBook = stubListBookEntity(listBookId = 2),
                book = null,
            )
            val nullEdition = stubListBookFull(
                listBook = stubListBookEntity(listBookId = 3),
                edition = null,
            )

            val wrapper = stubBookListWithBooks(
                listBooks = listOf(fullyHydrated, nullBook, nullEdition),
            )

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books.size shouldBe 1
            result.books[0].listBookId shouldBe 1
        }

        @Test
        fun `positioned books come before unpositioned ones regardless of addedAt`() {
            // ----- Arrange -----
            val unpositioned = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 1,
                    addedAt = "2024-12-01",
                ),
            )
            val positioned = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 2,
                    position = 0,
                    addedAt = "2023-01-01",
                ),
            )
            val wrapper = stubBookListWithBooks(listBooks = listOf(unpositioned, positioned))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 2
            result.books[1].listBookId shouldBe 1
        }

        @Test
        fun `multiple positioned books are ordered by ascending position`() {
            // ----- Arrange -----
            val pos2 = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 10,
                    position = 2,
                ),
            )
            val pos0 = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 11,
                    position = 0,
                ),
            )
            val pos1 = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 12,
                    position = 1,
                ),
            )
            val wrapper = stubBookListWithBooks(listBooks = listOf(pos2, pos0, pos1))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 11
            result.books[1].listBookId shouldBe 12
            result.books[2].listBookId shouldBe 10
        }

        @Test
        fun `unpositioned books fall back to addedAt descending after all positioned books`() {
            // ----- Arrange -----
            val positioned = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 1,
                    position = 0,
                    addedAt = "2023-01-01",
                ),
            )
            val newerUnpositioned = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 2,
                    addedAt = "2024-06-01",
                ),
            )
            val olderUnpositioned = stubListBookFull(
                listBook = stubListBookEntity(
                    listBookId = 3,
                    addedAt = "2023-03-01",
                ),
            )
            val wrapper = stubBookListWithBooks(
                listBooks = listOf(olderUnpositioned, newerUnpositioned, positioned),
            )

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 1
            result.books[1].listBookId shouldBe 2
            result.books[2].listBookId shouldBe 3
        }

        @Test
        fun `preserves list_books edition_id for owned slug even when userBook edition differs`() {
            // ----- Arrange -----
            val topLevelEditionId = 10
            val userBookEditionId = 55

            val topLevelEditionEntity = stubBookEditionEntity(
                id = topLevelEditionId,
                bookId = 1,
            )
            val topLevelEditionView = stubBookEditionView(
                entity = topLevelEditionEntity,
                isOwned = false,
            )
            val topLevelEditionWithAuthors = stubBookEditionWithAuthors(editionView = topLevelEditionView)

            val userBookEditionEntity = stubBookEditionEntity(
                id = userBookEditionId,
                bookId = 1,
            )
            val userBookEditionView = stubBookEditionView(
                entity = userBookEditionEntity,
                isOwned = false,
            )
            val userBookEditionWithAuthors = stubBookEditionWithAuthors(editionView = userBookEditionView)

            val userBookEntity = stubUserBookEntity(editionId = userBookEditionId)
            val userBookWithJournals = stubUserBookWithJournals(userBook = userBookEntity)

            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = userBookEditionId,
            )
            val bookFullEntity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(topLevelEditionWithAuthors, userBookEditionWithAuthors),
                userBookWithJournals = userBookWithJournals,
            )

            val listBookEntity = stubListBookEntity(
                bookId = 1,
                editionId = topLevelEditionId,
            )
            val listBookFull = stubListBookFull(
                listBook = listBookEntity,
                book = bookFullEntity,
                edition = topLevelEditionWithAuthors,
            )

            val bookListEntity = stubBookListEntity(slug = "owned")
            val wrapper = stubBookListWithBooks(
                bookList = bookListEntity,
                listBooks = listOf(listBookFull),
            )

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].editionId shouldBe topLevelEditionId
        }

        @Test
        fun `reads ranked=true from entity`() {
            // ----- Arrange -----
            val bookListEntity = stubBookListEntity(ranked = true)
            val wrapper = stubBookListWithBooks(bookList = bookListEntity)

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.ranked shouldBe true
        }

        @Test
        fun `reads ranked=false from entity`() {
            // ----- Arrange -----
            val bookListEntity = stubBookListEntity(ranked = false)
            val wrapper = stubBookListWithBooks(bookList = bookListEntity)

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.ranked shouldBe false
        }

        @Test
        fun `reads non-null signature from BookListEntity into model`() {
            // ----- Arrange -----
            val bookListEntity = stubBookListEntity(signature = "sig-xyz")
            val wrapper = stubBookListWithBooks(bookList = bookListEntity)

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.signature shouldBe "sig-xyz"
        }

        @Test
        fun `reads null signature from BookListEntity into model`() {
            // ----- Arrange -----
            val bookListEntity = stubBookListEntity(signature = null)
            val wrapper = stubBookListWithBooks(bookList = bookListEntity)

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.signature shouldBe null
        }
    }
}
