package nl.rhaydus.softcover.feature.books.data.mapper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEditionView
import nl.rhaydus.softcover.feature.books.data.model.BookEditionWithAuthors
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListWithBooks
import nl.rhaydus.softcover.feature.books.data.model.BookSeriesEntity
import nl.rhaydus.softcover.feature.books.data.model.ListBookEntity
import nl.rhaydus.softcover.feature.books.data.model.ListBookFull
import nl.rhaydus.softcover.feature.books.data.model.ReadingJournalEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookReadEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookWithJournals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookMapperTest {

    // region Shared stubs for UI -> Entity section

    private fun stubAuthor(
        id: Int = 1,
        name: String = "Author Name",
    ): Author = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.name
        } returns name
    }

    private fun stubBookEdition(
        id: Int = 10,
        bookId: Int = 1,
        publisher: String? = "Publisher",
        title: String? = "Edition Title",
        url: String? = "https://example.com/cover.jpg",
        isbn10: String? = "1234567890",
        pages: Int? = 300,
        authors: List<Author> = emptyList(),
        releaseYear: Int = 2020,
        format: String = "Paperback",
        owned: Boolean = false,
    ): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.bookId
        } returns bookId

        every {
            this@mockk.publisher
        } returns publisher

        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.url
        } returns url

        every {
            this@mockk.isbn10
        } returns isbn10

        every {
            this@mockk.pages
        } returns pages

        every {
            this@mockk.authors
        } returns authors

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.format
        } returns format

        every {
            this@mockk.owned
        } returns owned
    }

    private fun stubBookSeries(
        id: Int = 5,
        name: String = "Test Series",
        amountOfBooks: Int = 3,
    ): BookSeries = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.name
        } returns name

        every {
            this@mockk.amountOfBooks
        } returns amountOfBooks
    }

    private fun stubBookList(
        id: Int = 20,
        name: String = "My List",
        slug: String = "my-list",
        books: List<ListBook> = emptyList(),
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
            this@mockk.books
        } returns books
    }

    private fun stubListBook(
        listId: Int = 20,
        listBookId: Int = 99,
        book: Book = stubBook(),
        edition: BookEdition = stubBookEdition(),
    ): ListBook = mockk {
        every {
            this@mockk.listId
        } returns listId

        every {
            this@mockk.listBookId
        } returns listBookId

        every {
            this@mockk.book
        } returns book

        every {
            this@mockk.edition
        } returns edition
    }

    private fun stubUserBookRead(
        id: Int = 7,
        currentPage: Int? = 42,
        progress: Float? = 0.14f,
        startedAt: String? = "2024-01-01",
        finishedAt: String? = null,
    ): UserBookRead = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.currentPage
        } returns currentPage

        every {
            this@mockk.progress
        } returns progress

        every {
            this@mockk.startedAt
        } returns startedAt

        every {
            this@mockk.finishedAt
        } returns finishedAt
    }

    private fun stubUserBook(
        id: Int = 3,
        status: BookStatus = BookStatus.Reading,
        dateAdded: String = "2024-01-01",
        privacySettingId: Int = 1,
        reviewHasSpoilers: Boolean = false,
        editionId: Int? = 10,
        lastReadDate: String? = null,
        rating: Double? = null,
        referrerUserId: Int? = null,
        reviewedAt: String? = null,
        updatedAt: String? = null,
        journals: List<ReadingJournal> = emptyList(),
    ): UserBook = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.status
        } returns status

        every {
            this@mockk.dateAdded
        } returns dateAdded

        every {
            this@mockk.privacySettingId
        } returns privacySettingId

        every {
            this@mockk.reviewHasSpoilers
        } returns reviewHasSpoilers

        every {
            this@mockk.editionId
        } returns editionId

        every {
            this@mockk.lastReadDate
        } returns lastReadDate

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.referrerUserId
        } returns referrerUserId

        every {
            this@mockk.reviewedAt
        } returns reviewedAt

        every {
            this@mockk.updatedAt
        } returns updatedAt

        every {
            this@mockk.journals
        } returns journals
    }

    private fun stubReadingJournal(
        updatedAt: String = "2024-06-01T12:00:00",
        event: String? = "status:1",
    ): ReadingJournal = mockk {
        every {
            this@mockk.updatedAt
        } returns updatedAt

        every {
            this@mockk.event
        } returns event
    }

    private fun stubBook(
        id: Int = 1,
        title: String = "Test Book",
        editions: List<BookEdition> = emptyList(),
        defaultEdition: BookEdition? = null,
        rating: Double = 4.2,
        description: String = "A great book.",
        releaseYear: Int = 2019,
        coverUrl: String = "https://example.com/book.jpg",
        authors: List<Author> = emptyList(),
        usersCount: Int = 100,
        bookSeries: BookSeries? = null,
        positionInSeries: Int? = null,
        userBook: UserBook? = null,
        userBookRead: UserBookRead? = null,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.editions
        } returns editions

        every {
            this@mockk.defaultEdition
        } returns defaultEdition

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.description
        } returns description

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.coverUrl
        } returns coverUrl

        every {
            this@mockk.authors
        } returns authors

        every {
            this@mockk.usersCount
        } returns usersCount

        every {
            this@mockk.bookSeries
        } returns bookSeries

        every {
            this@mockk.positionInSeries
        } returns positionInSeries

        every {
            this@mockk.userBook
        } returns userBook

        every {
            this@mockk.userBookRead
        } returns userBookRead
    }

    // endregion

    // region Shared stubs for Entity -> UI section

    private fun stubAuthorEntity(
        id: Int = 1,
        name: String = "Author Name",
    ): AuthorEntity = AuthorEntity(id = id, name = name)

    private fun stubBookEditionEntity(
        id: Int = 10,
        bookId: Int = 1,
        publisher: String? = "Publisher",
        title: String? = "Edition Title",
        url: String? = "https://example.com/cover.jpg",
        isbn10: String? = "1234567890",
        pages: Int? = 300,
        releaseYear: Int = 2020,
        format: String = "Paperback",
    ): BookEditionEntity = BookEditionEntity(
        id = id,
        bookId = bookId,
        publisher = publisher,
        title = title,
        url = url,
        isbn10 = isbn10,
        pages = pages,
        releaseYear = releaseYear,
        format = format,
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
        description: String = "A great book.",
        releaseYear: Int = 2019,
        coverUrl: String = "https://example.com/book.jpg",
        usersCount: Int = 100,
        positionInSeries: Int? = null,
        seriesId: Int? = null,
    ): BookEntity = BookEntity(
        id = id,
        title = title,
        defaultEditionId = defaultEditionId,
        rating = rating,
        description = description,
        releaseYear = releaseYear,
        coverUrl = coverUrl,
        usersCount = usersCount,
        positionInSeries = positionInSeries,
        seriesId = seriesId,
    )

    private fun stubUserBookEntity(
        id: Int = 3,
        bookId: Int = 1,
        statusCode: Int = BookStatus.Reading.code,
        dateAdded: String = "2024-01-01",
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
        privacySettingId = privacySettingId,
        reviewHasSpoilers = reviewHasSpoilers,
        editionId = editionId,
        lastReadDate = lastReadDate,
        rating = rating,
        referrerUserId = referrerUserId,
        reviewedAt = reviewedAt,
        updatedAt = updatedAt,
    )

    private fun stubUserBookReadEntity(
        id: Int = 7,
        userBookId: Int = 3,
        currentPage: Int? = 42,
        progress: Float? = 0.14f,
        startedAt: String? = "2024-01-01",
        finishedAt: String? = null,
    ): UserBookReadEntity = UserBookReadEntity(
        id = id,
        userBookId = userBookId,
        currentPage = currentPage,
        progress = progress,
        startedAt = startedAt,
        finishedAt = finishedAt,
    )

    private fun stubReadingJournalEntity(
        userBookId: Int = 3,
        event: String = "status:1",
        updatedAt: String = "2024-06-01T12:00:00",
    ): ReadingJournalEntity = ReadingJournalEntity(
        userBookId = userBookId,
        event = event,
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

    private fun stubBookSeriesEntity(
        id: Int = 5,
        name: String = "Test Series",
        amountOfBooks: Int = 3,
    ): BookSeriesEntity = BookSeriesEntity(
        id = id,
        name = name,
        amountOfBooks = amountOfBooks,
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
    )

    private fun stubListBookEntity(
        listId: Int = 20,
        bookId: Int = 1,
        editionId: Int = 10,
        listBookId: Int = 99,
    ): ListBookEntity = ListBookEntity(
        listId = listId,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
    )

    private fun stubListBookFull(
        listBook: ListBookEntity = stubListBookEntity(),
        book: BookFullEntity = stubBookFullEntity(),
        edition: BookEditionWithAuthors = stubBookEditionWithAuthors(),
    ): ListBookFull = ListBookFull(
        listBook = listBook,
        book = book,
        edition = edition,
    )

    private fun stubBookListEntity(
        id: Int = 20,
        name: String = "My List",
        slug: String = "my-list",
    ): BookListEntity = BookListEntity(
        id = id,
        name = name,
        slug = slug,
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
    }

    @Nested
    inner class BookSeriesToEntity {

        @Test
        fun `maps all fields from BookSeries to BookSeriesEntity`() {
            // ----- Arrange -----
            val bookSeries = stubBookSeries(
                id = 5,
                name = "Test Series",
                amountOfBooks = 3,
            )

            // ----- Act -----
            val result = bookSeries.toEntity()

            // ----- Assert -----
            result.id shouldBe 5
            result.name shouldBe "Test Series"
            result.amountOfBooks shouldBe 3
        }
    }

    @Nested
    inner class ListBookToEntity {

        @Test
        fun `maps listId, bookId, editionId and listBookId from ListBook to ListBookEntity`() {
            // ----- Arrange -----
            val innerBook = stubBook(id = 1)
            val innerEdition = stubBookEdition(id = 10)
            val listBook = stubListBook(
                listId = 20,
                listBookId = 99,
                book = innerBook,
                edition = innerEdition,
            )

            // ----- Act -----
            val result = listBook.toEntity()

            // ----- Assert -----
            result.listId shouldBe 20
            result.bookId shouldBe 1
            result.editionId shouldBe 10
            result.listBookId shouldBe 99
        }
    }

    @Nested
    inner class BookToEntity {

        @Test
        fun `maps all scalar fields from Book to BookEntity`() {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                title = "Test Book",
                rating = 4.2,
                description = "A great book.",
                releaseYear = 2019,
                coverUrl = "https://example.com/book.jpg",
                usersCount = 100,
                positionInSeries = 2,
            )

            // ----- Act -----
            val result = book.toEntity()

            // ----- Assert -----
            result.id shouldBe 1
            result.title shouldBe "Test Book"
            result.rating shouldBe 4.2
            result.description shouldBe "A great book."
            result.releaseYear shouldBe 2019
            result.coverUrl shouldBe "https://example.com/book.jpg"
            result.usersCount shouldBe 100
            result.positionInSeries shouldBe 2
        }

        @Test
        fun `maps defaultEditionId as edition id when defaultEdition is non-null`() {
            // ----- Arrange -----
            val edition = stubBookEdition(id = 10)
            val book = stubBook(defaultEdition = edition)

            // ----- Act -----
            val result = book.toEntity()

            // ----- Assert -----
            result.defaultEditionId shouldBe 10
        }

        @Test
        fun `maps defaultEditionId as null when defaultEdition is null`() {
            // ----- Arrange -----
            val book = stubBook(defaultEdition = null)

            // ----- Act -----
            val result = book.toEntity()

            // ----- Assert -----
            result.defaultEditionId shouldBe null
        }

        @Test
        fun `maps seriesId as series id when bookSeries is non-null`() {
            // ----- Arrange -----
            val series = stubBookSeries(id = 5)
            val book = stubBook(bookSeries = series)

            // ----- Act -----
            val result = book.toEntity()

            // ----- Assert -----
            result.seriesId shouldBe 5
        }

        @Test
        fun `maps seriesId as null when bookSeries is null`() {
            // ----- Arrange -----
            val book = stubBook(bookSeries = null)

            // ----- Act -----
            val result = book.toEntity()

            // ----- Assert -----
            result.seriesId shouldBe null
        }
    }

    @Nested
    inner class UserBookReadToEntity {

        @Test
        fun `maps all fields from UserBookRead to UserBookReadEntity`() {
            // ----- Arrange -----
            val userBookRead = stubUserBookRead(
                id = 7,
                currentPage = 42,
                progress = 0.14f,
                startedAt = "2024-01-01",
                finishedAt = "2024-06-01",
            )

            // ----- Act -----
            val result = userBookRead.toEntity(userBookId = 3)

            // ----- Assert -----
            result.id shouldBe 7
            result.currentPage shouldBe 42
            result.progress shouldBe 0.14f
            result.startedAt shouldBe "2024-01-01"
            result.finishedAt shouldBe "2024-06-01"
            result.userBookId shouldBe 3
        }

        @Test
        fun `maps nullable fields as null when absent`() {
            // ----- Arrange -----
            val userBookRead = stubUserBookRead(
                currentPage = null,
                progress = null,
                startedAt = null,
                finishedAt = null,
            )

            // ----- Act -----
            val result = userBookRead.toEntity(userBookId = 3)

            // ----- Assert -----
            result.currentPage shouldBe null
            result.progress shouldBe null
            result.startedAt shouldBe null
            result.finishedAt shouldBe null
        }
    }

    @Nested
    inner class UserBookToEntity {

        @Test
        fun `maps all fields from UserBook to UserBookEntity`() {
            // ----- Arrange -----
            val userBook = stubUserBook(
                id = 3,
                status = BookStatus.Reading,
                dateAdded = "2024-01-01",
                privacySettingId = 1,
                reviewHasSpoilers = true,
                editionId = 10,
                lastReadDate = "2024-05-01",
                rating = 4.5,
                referrerUserId = 55,
                reviewedAt = "2024-05-02",
                updatedAt = "2024-05-03",
            )

            // ----- Act -----
            val result = userBook.toEntity(bookId = 1)

            // ----- Assert -----
            result.id shouldBe 3
            result.statusCode shouldBe BookStatus.Reading.code
            result.dateAdded shouldBe "2024-01-01"
            result.privacySettingId shouldBe 1
            result.reviewHasSpoilers shouldBe true
            result.editionId shouldBe 10
            result.lastReadDate shouldBe "2024-05-01"
            result.rating shouldBe 4.5
            result.referrerUserId shouldBe 55
            result.reviewedAt shouldBe "2024-05-02"
            result.updatedAt shouldBe "2024-05-03"
            result.bookId shouldBe 1
        }

        @Test
        fun `maps nullable fields as null when absent`() {
            // ----- Arrange -----
            val userBook = stubUserBook(
                editionId = null,
                lastReadDate = null,
                rating = null,
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
            )

            // ----- Act -----
            val result = userBook.toEntity(bookId = 1)

            // ----- Assert -----
            result.editionId shouldBe null
            result.lastReadDate shouldBe null
            result.rating shouldBe null
            result.referrerUserId shouldBe null
            result.reviewedAt shouldBe null
            result.updatedAt shouldBe null
        }

        @Test
        fun `statusCode is taken from status enum code`() {
            // ----- Arrange -----
            val userBook = stubUserBook(status = BookStatus.WantToRead)

            // ----- Act -----
            val result = userBook.toEntity(bookId = 1)

            // ----- Assert -----
            result.statusCode shouldBe BookStatus.WantToRead.code
        }
    }

    @Nested
    inner class ReadingJournalToEntity {

        @Test
        fun `maps updatedAt and non-null event from ReadingJournal to ReadingJournalEntity`() {
            // ----- Arrange -----
            val journal = stubReadingJournal(
                updatedAt = "2024-06-01T12:00:00",
                event = "status:3",
            )

            // ----- Act -----
            val result = journal.toEntity(userBookId = 3)

            // ----- Assert -----
            result.updatedAt shouldBe "2024-06-01T12:00:00"
            result.event shouldBe "status:3"
            result.userBookId shouldBe 3
        }

        @Test
        fun `maps null event as empty string`() {
            // ----- Arrange -----
            val journal = stubReadingJournal(event = null)

            // ----- Act -----
            val result = journal.toEntity(userBookId = 3)

            // ----- Assert -----
            result.event shouldBe ""
        }
    }

    @Nested
    inner class BookEditionToEntity {

        @Test
        fun `maps all fields from BookEdition to BookEditionEntity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                id = 10,
                bookId = 1,
                publisher = "Publisher",
                title = "Edition Title",
                url = "https://example.com/cover.jpg",
                isbn10 = "1234567890",
                pages = 300,
                releaseYear = 2020,
                format = "Paperback",
            )

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.id shouldBe 10
            result.bookId shouldBe 1
            result.publisher shouldBe "Publisher"
            result.title shouldBe "Edition Title"
            result.url shouldBe "https://example.com/cover.jpg"
            result.isbn10 shouldBe "1234567890"
            result.pages shouldBe 300
            result.releaseYear shouldBe 2020
            result.format shouldBe "Paperback"
        }

        @Test
        fun `maps nullable fields as null when absent`() {
            // ----- Arrange -----
            val edition = stubBookEdition(
                publisher = null,
                title = null,
                url = null,
                isbn10 = null,
                pages = null,
            )

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.publisher shouldBe null
            result.title shouldBe null
            result.url shouldBe null
            result.isbn10 shouldBe null
            result.pages shouldBe null
        }
    }

    @Nested
    inner class AuthorToEntity {

        @Test
        fun `maps name and id from Author to AuthorEntity`() {
            // ----- Arrange -----
            val author = stubAuthor(
                id = 1,
                name = "Jane Austen",
            )

            // ----- Act -----
            val result = author.toEntity()

            // ----- Assert -----
            result.id shouldBe 1
            result.name shouldBe "Jane Austen"
        }
    }

    @Nested
    inner class BookToBookAuthorRefs {

        @Test
        fun `produces one cross-ref per author using id from map`() {
            // ----- Arrange -----
            val authorA = stubAuthor(
                id = 1,
                name = "Alice",
            )
            val authorB = stubAuthor(
                id = 2,
                name = "Bob",
            )
            val book = stubBook(
                id = 42,
                authors = listOf(
                    authorA,
                    authorB,
                ),
            )
            val authorIdsByName = mapOf(
                "Alice" to 10,
                "Bob" to 20,
            )

            // ----- Act -----
            val result = book.toBookAuthorRefs(authorIdsByName)

            // ----- Assert -----
            result.size shouldBe 2
            result[0].bookId shouldBe 42
            result[0].authorId shouldBe 10
            result[1].bookId shouldBe 42
            result[1].authorId shouldBe 20
        }

        @Test
        fun `produces empty list when book has no authors`() {
            // ----- Arrange -----
            val book = stubBook(authors = emptyList())
            val authorIdsByName = emptyMap<String, Int>()

            // ----- Act -----
            val result = book.toBookAuthorRefs(authorIdsByName)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `throws NoSuchElementException when author name is absent from map`() {
            // ----- Arrange -----
            val author = stubAuthor(name = "Unknown Author")
            val book = stubBook(authors = listOf(author))
            val authorIdsByName = emptyMap<String, Int>()

            // ----- Act & Assert -----
            shouldThrow<NoSuchElementException> {
                book.toBookAuthorRefs(authorIdsByName)
            }
        }
    }

    @Nested
    inner class BookToEditionAuthorRefs {

        @Test
        fun `flattens cross-refs across all editions`() {
            // ----- Arrange -----
            val authorA = stubAuthor(
                id = 1,
                name = "Alice",
            )
            val authorB = stubAuthor(
                id = 2,
                name = "Bob",
            )
            val editionOne = stubBookEdition(
                id = 10,
                authors = listOf(authorA),
            )
            val editionTwo = stubBookEdition(
                id = 11,
                authors = listOf(authorB),
            )
            val book = stubBook(
                editions = listOf(
                    editionOne,
                    editionTwo,
                ),
            )
            val authorIdsByName = mapOf(
                "Alice" to 10,
                "Bob" to 20,
            )

            // ----- Act -----
            val result = book.toEditionAuthorRefs(authorIdsByName)

            // ----- Assert -----
            result.size shouldBe 2
            result[0].editionId shouldBe 10
            result[0].authorId shouldBe 10
            result[1].editionId shouldBe 11
            result[1].authorId shouldBe 20
        }

        @Test
        fun `produces empty list when book has no editions`() {
            // ----- Arrange -----
            val book = stubBook(editions = emptyList())
            val authorIdsByName = emptyMap<String, Int>()

            // ----- Act -----
            val result = book.toEditionAuthorRefs(authorIdsByName)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `throws NoSuchElementException when edition author name is absent from map`() {
            // ----- Arrange -----
            val author = stubAuthor(name = "Unknown Author")
            val edition = stubBookEdition(
                id = 10,
                authors = listOf(author),
            )
            val book = stubBook(editions = listOf(edition))
            val authorIdsByName = emptyMap<String, Int>()

            // ----- Act & Assert -----
            shouldThrow<NoSuchElementException> {
                book.toEditionAuthorRefs(authorIdsByName)
            }
        }
    }

    @Nested
    inner class BookEditionToEditionAuthorRefs {

        @Test
        fun `produces one cross-ref per author using id from map`() {
            // ----- Arrange -----
            val authorA = stubAuthor(
                id = 1,
                name = "Alice",
            )
            val authorB = stubAuthor(
                id = 2,
                name = "Bob",
            )
            val edition = stubBookEdition(
                id = 10,
                authors = listOf(
                    authorA,
                    authorB,
                ),
            )
            val authorIdsByName = mapOf(
                "Alice" to 10,
                "Bob" to 20,
            )

            // ----- Act -----
            val result = edition.toEditionAuthorRefs(authorIdsByName)

            // ----- Assert -----
            result.size shouldBe 2
            result[0].editionId shouldBe 10
            result[0].authorId shouldBe 10
            result[1].editionId shouldBe 10
            result[1].authorId shouldBe 20
        }

        @Test
        fun `produces empty list when edition has no authors`() {
            // ----- Arrange -----
            val edition = stubBookEdition(authors = emptyList())
            val authorIdsByName = emptyMap<String, Int>()

            // ----- Act -----
            val result = edition.toEditionAuthorRefs(authorIdsByName)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `throws NoSuchElementException when author name is absent from map`() {
            // ----- Arrange -----
            val author = stubAuthor(name = "Unknown Author")
            val edition = stubBookEdition(
                id = 10,
                authors = listOf(author),
            )
            val authorIdsByName = emptyMap<String, Int>()

            // ----- Act & Assert -----
            shouldThrow<NoSuchElementException> {
                edition.toEditionAuthorRefs(authorIdsByName)
            }
        }
    }

    // =========================================================
    // Entity -> UI mappers
    // =========================================================

    @Nested
    inner class AuthorEntityToModel {

        @Test
        fun `maps name and id from AuthorEntity to Author`() {
            // ----- Arrange -----
            val entity = stubAuthorEntity(
                id = 1,
                name = "Jane Austen",
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.id shouldBe 1
            result.name shouldBe "Jane Austen"
        }
    }

    @Nested
    inner class BookEditionEntityToModel {

        @Test
        fun `maps all fields from BookEditionEntity to BookEdition`() {
            // ----- Arrange -----
            val authorEntity = stubAuthorEntity(
                id = 1,
                name = "Jane Austen",
            )
            val entity = stubBookEditionEntity(
                id = 10,
                bookId = 1,
                publisher = "Publisher",
                title = "Edition Title",
                url = "https://example.com/cover.jpg",
                isbn10 = "1234567890",
                pages = 300,
                releaseYear = 2020,
                format = "Paperback",
            )

            // ----- Act -----
            val result = entity.toModel(
                authors = listOf(authorEntity),
                owned = false,
            )

            // ----- Assert -----
            result.id shouldBe 10
            result.bookId shouldBe 1
            result.publisher shouldBe "Publisher"
            result.title shouldBe "Edition Title"
            result.url shouldBe "https://example.com/cover.jpg"
            result.isbn10 shouldBe "1234567890"
            result.pages shouldBe 300
            result.releaseYear shouldBe 2020
            result.format shouldBe "Paperback"
            result.authors.size shouldBe 1
            result.authors[0].id shouldBe 1
            result.authors[0].name shouldBe "Jane Austen"
        }

        @Test
        fun `maps owned as true when owned flag is true`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity()

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = true,
            )

            // ----- Assert -----
            result.owned shouldBe true
        }

        @Test
        fun `maps owned as false when owned flag is false`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity()

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.owned shouldBe false
        }

        @Test
        fun `maps empty authors list to empty authors on model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity()

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }
    }

    @Nested
    inner class UserBookReadEntityToModel {

        @Test
        fun `maps all fields from UserBookReadEntity to UserBookRead`() {
            // ----- Arrange -----
            val entity = stubUserBookReadEntity(
                id = 7,
                userBookId = 3,
                currentPage = 42,
                progress = 0.14f,
                startedAt = "2024-01-01",
                finishedAt = "2024-06-01",
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.id shouldBe 7
            result.currentPage shouldBe 42
            result.progress shouldBe 0.14f
            result.startedAt shouldBe "2024-01-01"
            result.finishedAt shouldBe "2024-06-01"
        }

        @Test
        fun `maps nullable fields as null when absent`() {
            // ----- Arrange -----
            val entity = stubUserBookReadEntity(
                currentPage = null,
                progress = null,
                startedAt = null,
                finishedAt = null,
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.currentPage shouldBe null
            result.progress shouldBe null
            result.startedAt shouldBe null
            result.finishedAt shouldBe null
        }
    }

    @Nested
    inner class UserBookEntityToModel {

        @Test
        fun `maps all fields and resolves BookStatus from statusCode`() {
            // ----- Arrange -----
            val journalModel = ReadingJournal(
                updatedAt = "2024-06-01T12:00:00",
                event = "status:3",
            )
            val entity = stubUserBookEntity(
                id = 3,
                bookId = 1,
                statusCode = BookStatus.Read.code,
                dateAdded = "2024-01-01",
                privacySettingId = 1,
                reviewHasSpoilers = true,
                editionId = 10,
                lastReadDate = "2024-05-01",
                rating = 4.5,
                referrerUserId = 55,
                reviewedAt = "2024-05-02",
                updatedAt = "2024-05-03",
            )

            // ----- Act -----
            val result = entity.toModel(journals = listOf(journalModel))

            // ----- Assert -----
            result.id shouldBe 3
            result.status shouldBe BookStatus.Read
            result.dateAdded shouldBe "2024-01-01"
            result.privacySettingId shouldBe 1
            result.reviewHasSpoilers shouldBe true
            result.editionId shouldBe 10
            result.lastReadDate shouldBe "2024-05-01"
            result.rating shouldBe 4.5
            result.referrerUserId shouldBe 55
            result.reviewedAt shouldBe "2024-05-02"
            result.updatedAt shouldBe "2024-05-03"
            result.journals shouldBe listOf(journalModel)
        }

        @Test
        fun `resolves unknown statusCode to BookStatus None`() {
            // ----- Arrange -----
            val entity = stubUserBookEntity(statusCode = 999)

            // ----- Act -----
            val result = entity.toModel(journals = emptyList())

            // ----- Assert -----
            result.status shouldBe BookStatus.None
        }

        @Test
        fun `maps empty journals list to empty journals on model`() {
            // ----- Arrange -----
            val entity = stubUserBookEntity()

            // ----- Act -----
            val result = entity.toModel(journals = emptyList())

            // ----- Assert -----
            result.journals shouldBe emptyList()
        }

        @Test
        fun `maps all known BookStatus codes correctly`() {
            // ----- Arrange -----
            val statusCases = BookStatus.entries.filter { it != BookStatus.None }

            statusCases.forEach { expectedStatus ->
                val entity = stubUserBookEntity(statusCode = expectedStatus.code)

                // ----- Act -----
                val result = entity.toModel(journals = emptyList())

                // ----- Assert -----
                result.status shouldBe expectedStatus
            }
        }
    }

    @Nested
    inner class ReadingJournalEntityToModel {

        @Test
        fun `maps updatedAt and event from ReadingJournalEntity to ReadingJournal`() {
            // ----- Arrange -----
            val entity = stubReadingJournalEntity(
                updatedAt = "2024-06-01T12:00:00",
                event = "status:3",
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.updatedAt shouldBe "2024-06-01T12:00:00"
            result.event shouldBe "status:3"
        }

        @Test
        fun `maps empty event string`() {
            // ----- Arrange -----
            val entity = stubReadingJournalEntity(event = "")

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.event shouldBe ""
        }
    }

    @Nested
    inner class ListBookFullToModel {

        @Test
        fun `maps listId and listBookId from ListBookEntity`() {
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
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.listId shouldBe 20
            result.listBookId shouldBe 99
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
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.edition.owned shouldBe true
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
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.edition.owned shouldBe false
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
    }

    @Nested
    inner class BookSeriesEntityToModel {

        @Test
        fun `maps all fields from BookSeriesEntity to BookSeries`() {
            // ----- Arrange -----
            val entity = stubBookSeriesEntity(
                id = 5,
                name = "Test Series",
                amountOfBooks = 3,
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.id shouldBe 5
            result.name shouldBe "Test Series"
            result.amountOfBooks shouldBe 3
        }
    }

    @Nested
    inner class BookFullEntityToModel {

        @Test
        fun `maps all scalar fields from BookEntity`() {
            // ----- Arrange -----
            val bookEntity = stubBookEntity(
                id = 1,
                title = "Test Book",
                rating = 4.2,
                description = "A great book.",
                releaseYear = 2019,
                coverUrl = "https://example.com/book.jpg",
                usersCount = 100,
                positionInSeries = 2,
            )
            val entity = stubBookFullEntity(book = bookEntity)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.id shouldBe 1
            result.title shouldBe "Test Book"
            result.rating shouldBe 4.2
            result.description shouldBe "A great book."
            result.releaseYear shouldBe 2019
            result.coverUrl shouldBe "https://example.com/book.jpg"
            result.usersCount shouldBe 100
            result.positionInSeries shouldBe 2
        }

        @Test
        fun `resolves defaultEdition when defaultEditionId matches a mapped edition`() {
            // ----- Arrange -----
            val editionEntity = stubBookEditionEntity(
                id = 10,
                bookId = 1,
            )
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = false,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(editionView = editionView)
            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = 10,
            )
            val entity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(editionWithAuthors),
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.defaultEdition?.id shouldBe 10
        }

        @Test
        fun `defaultEdition is null when defaultEditionId is null`() {
            // ----- Arrange -----
            val bookEntity = stubBookEntity(defaultEditionId = null)
            val entity = stubBookFullEntity(book = bookEntity)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.defaultEdition shouldBe null
        }

        @Test
        fun `defaultEdition is null when defaultEditionId does not match any edition`() {
            // ----- Arrange -----
            val editionEntity = stubBookEditionEntity(
                id = 10,
                bookId = 1,
            )
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = false,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(editionView = editionView)
            val bookEntity = stubBookEntity(
                id = 1,
                defaultEditionId = 999,
            )
            val entity = stubBookFullEntity(
                book = bookEntity,
                editions = listOf(editionWithAuthors),
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.defaultEdition shouldBe null
        }

        @Test
        fun `journals defaults to empty list when userBookWithJournals is null`() {
            // ----- Arrange -----
            val entity = stubBookFullEntity(userBookWithJournals = null)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBook shouldBe null
        }

        @Test
        fun `userBook is null when userBookWithJournals is null`() {
            // ----- Arrange -----
            val entity = stubBookFullEntity(userBookWithJournals = null)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBook shouldBe null
        }

        @Test
        fun `userBookRead is null when userBookWithJournals is null`() {
            // ----- Arrange -----
            val entity = stubBookFullEntity(userBookWithJournals = null)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBookRead shouldBe null
        }

        @Test
        fun `userBook is mapped when userBookWithJournals is non-null`() {
            // ----- Arrange -----
            val userBookEntity = stubUserBookEntity(
                id = 3,
                statusCode = BookStatus.Reading.code,
            )
            val withJournals = stubUserBookWithJournals(
                userBook = userBookEntity,
                journals = emptyList(),
            )
            val entity = stubBookFullEntity(userBookWithJournals = withJournals)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBook?.id shouldBe 3
            result.userBook?.status shouldBe BookStatus.Reading
        }

        @Test
        fun `userBookRead is null when userBookWithJournals has null userBookRead`() {
            // ----- Arrange -----
            val withJournals = stubUserBookWithJournals(userBookRead = null)
            val entity = stubBookFullEntity(userBookWithJournals = withJournals)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBookRead shouldBe null
        }

        @Test
        fun `userBookRead is mapped when userBookWithJournals has non-null userBookRead`() {
            // ----- Arrange -----
            val readEntity = stubUserBookReadEntity(
                id = 7,
                currentPage = 42,
            )
            val withJournals = stubUserBookWithJournals(userBookRead = readEntity)
            val entity = stubBookFullEntity(userBookWithJournals = withJournals)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBookRead?.id shouldBe 7
            result.userBookRead?.currentPage shouldBe 42
        }

        @Test
        fun `journals from userBookWithJournals are mapped and passed to userBook`() {
            // ----- Arrange -----
            val journalEntity = stubReadingJournalEntity(
                event = "status:3",
                updatedAt = "2024-06-01T12:00:00",
            )
            val withJournals = stubUserBookWithJournals(journals = listOf(journalEntity))
            val entity = stubBookFullEntity(userBookWithJournals = withJournals)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.userBook?.journals?.size shouldBe 1
            result.userBook?.journals?.get(0)?.event shouldBe "status:3"
        }

        @Test
        fun `bookSeries is null when series entity is null`() {
            // ----- Arrange -----
            val entity = stubBookFullEntity(series = null)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.bookSeries shouldBe null
        }

        @Test
        fun `bookSeries is mapped when series entity is non-null`() {
            // ----- Arrange -----
            val seriesEntity = stubBookSeriesEntity(
                id = 5,
                name = "Test Series",
                amountOfBooks = 3,
            )
            val entity = stubBookFullEntity(series = seriesEntity)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.bookSeries?.id shouldBe 5
            result.bookSeries?.name shouldBe "Test Series"
            result.bookSeries?.amountOfBooks shouldBe 3
        }

        @Test
        fun `bookAuthors are mapped to authors on Book model`() {
            // ----- Arrange -----
            val authorEntity = stubAuthorEntity(
                id = 1,
                name = "Jane Austen",
            )
            val entity = stubBookFullEntity(bookAuthors = listOf(authorEntity))

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.authors.size shouldBe 1
            result.authors[0].id shouldBe 1
            result.authors[0].name shouldBe "Jane Austen"
        }

        @Test
        fun `editions are mapped using isOwned from BookEditionView`() {
            // ----- Arrange -----
            val editionEntity = stubBookEditionEntity(
                id = 10,
                bookId = 1,
            )
            val editionView = stubBookEditionView(
                entity = editionEntity,
                isOwned = true,
            )
            val editionWithAuthors = stubBookEditionWithAuthors(editionView = editionView)
            val entity = stubBookFullEntity(editions = listOf(editionWithAuthors))

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.editions.size shouldBe 1
            result.editions[0].id shouldBe 10
            result.editions[0].owned shouldBe true
        }
    }
}
