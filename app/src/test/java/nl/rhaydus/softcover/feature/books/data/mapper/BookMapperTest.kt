package nl.rhaydus.softcover.feature.books.data.mapper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import nl.rhaydus.softcover.fragment.BookDetailFragment
import nl.rhaydus.softcover.fragment.EditionDetailFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Progress_updated_journal.Companion.readingJournalFragment as progressUpdatedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Status_currently_reading_journal.Companion.readingJournalFragment as statusCurrentlyReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Status_stopped_journal.Companion.readingJournalFragment as statusStoppedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_finished_journal.Companion.readingJournalFragment as userBookReadFinishedJournalFragment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookMapperTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

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
        canonicalId: Int? = null,
        bookId: Int = 1,
        publisher: String? = "Publisher",
        title: String? = "Edition Title",
        url: String? = "https://example.com/cover.jpg",
        localImagePath: String? = null,
        isbn10: String? = "1234567890",
        pages: Int? = 300,
        audioSeconds: Int? = null,
        authors: List<Author> = emptyList(),
        releaseYear: Int = 2020,
        format: String = "Paperback",
        owned: Boolean = false,
    ): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.canonicalId
        } returns canonicalId

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
            this@mockk.localImagePath
        } returns localImagePath

        every {
            this@mockk.isbn10
        } returns isbn10

        every {
            this@mockk.pages
        } returns pages

        every {
            this@mockk.audioSeconds
        } returns audioSeconds

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
        bookId: Int = 1,
        editionId: Int = 10,
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
            this@mockk.addedAt
        } returns addedAt

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
        currentSeconds: Int? = null,
        progress: Float = 0.14f,
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
            this@mockk.currentSeconds
        } returns currentSeconds

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
        createdAt: String? = null,
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
            this@mockk.createdAt
        } returns createdAt

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
        canonicalId: Int? = null,
        bookId: Int = 1,
        publisher: String? = "Publisher",
        title: String? = "Edition Title",
        url: String? = "https://example.com/cover.jpg",
        localImagePath: String? = null,
        isbn10: String? = "1234567890",
        pages: Int? = 300,
        audioSeconds: Int? = null,
        releaseYear: Int = 2020,
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
        pages = pages,
        audioSeconds = audioSeconds,
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

    private fun stubUserBookReadEntity(
        id: Int = 7,
        userBookId: Int = 3,
        currentPage: Int? = 42,
        currentSeconds: Int? = null,
        progress: Float = 0.14f,
        startedAt: String? = "2024-01-01",
        finishedAt: String? = null,
    ): UserBookReadEntity = UserBookReadEntity(
        id = id,
        userBookId = userBookId,
        currentPage = currentPage,
        currentSeconds = currentSeconds,
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
        addedAt: String? = null,
    ): ListBookEntity = ListBookEntity(
        listId = listId,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        addedAt = addedAt,
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
        fun `maps absent nullable fields to null and progress to zero`() {
            // ----- Arrange -----
            val userBookRead = stubUserBookRead(
                currentPage = null,
                progress = 0f,
                startedAt = null,
                finishedAt = null,
            )

            // ----- Act -----
            val result = userBookRead.toEntity(userBookId = 3)

            // ----- Assert -----
            result.currentPage shouldBe null
            result.progress shouldBe 0f
            result.startedAt shouldBe null
            result.finishedAt shouldBe null
        }

        @Test
        fun `maps non-null currentSeconds to entity`() {
            // ----- Arrange -----
            val userBookRead = stubUserBookRead(currentSeconds = 3600)

            // ----- Act -----
            val result = userBookRead.toEntity(userBookId = 3)

            // ----- Assert -----
            result.currentSeconds shouldBe 3600
        }

        @Test
        fun `maps null currentSeconds to entity as null`() {
            // ----- Arrange -----
            val userBookRead = stubUserBookRead(currentSeconds = null)

            // ----- Act -----
            val result = userBookRead.toEntity(userBookId = 3)

            // ----- Assert -----
            result.currentSeconds shouldBe null
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
                createdAt = "2024-01-02",
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
            result.createdAt shouldBe "2024-01-02"
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
                createdAt = null,
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
            result.createdAt shouldBe null
            result.editionId shouldBe null
            result.lastReadDate shouldBe null
            result.rating shouldBe null
            result.referrerUserId shouldBe null
            result.reviewedAt shouldBe null
            result.updatedAt shouldBe null
        }

        @Test
        fun `propagates non-null createdAt to UserBookEntity`() {
            // ----- Arrange -----
            val userBook = stubUserBook(createdAt = "2024-03-10")

            // ----- Act -----
            val result = userBook.toEntity(bookId = 1)

            // ----- Assert -----
            result.createdAt shouldBe "2024-03-10"
        }

        @Test
        fun `propagates null createdAt to UserBookEntity as null`() {
            // ----- Arrange -----
            val userBook = stubUserBook(createdAt = null)

            // ----- Act -----
            val result = userBook.toEntity(bookId = 1)

            // ----- Assert -----
            result.createdAt shouldBe null
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
                canonicalId = null,
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
            result.canonicalId shouldBe null
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
        fun `passes through non-null canonicalId to entity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(canonicalId = 42)

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.canonicalId shouldBe 42
        }

        @Test
        fun `passes through null canonicalId to entity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(canonicalId = null)

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.canonicalId shouldBe null
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

        @Test
        fun `passes through non-null localImagePath to entity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(localImagePath = "/data/edition_images/10")

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.localImagePath shouldBe "/data/edition_images/10"
        }

        @Test
        fun `passes through null localImagePath to entity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(localImagePath = null)

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.localImagePath shouldBe null
        }

        @Test
        fun `passes through non-null audioSeconds to entity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(audioSeconds = 3600)

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.audioSeconds shouldBe 3600
        }

        @Test
        fun `passes through null audioSeconds to entity`() {
            // ----- Arrange -----
            val edition = stubBookEdition(audioSeconds = null)

            // ----- Act -----
            val result = edition.toEntity()

            // ----- Assert -----
            result.audioSeconds shouldBe null
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
                canonicalId = null,
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
            result.canonicalId shouldBe null
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
        fun `passes through non-null canonicalId from entity to model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity(canonicalId = 77)

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.canonicalId shouldBe 77
        }

        @Test
        fun `passes through null canonicalId from entity to model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity(canonicalId = null)

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.canonicalId shouldBe null
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

        @Test
        fun `passes through non-null localImagePath to model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity(localImagePath = "/data/edition_images/10")

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.localImagePath shouldBe "/data/edition_images/10"
        }

        @Test
        fun `passes through null localImagePath to model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity(localImagePath = null)

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.localImagePath shouldBe null
        }

        @Test
        fun `passes through non-null audioSeconds from entity to model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity(audioSeconds = 5400)

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.audioSeconds shouldBe 5400
        }

        @Test
        fun `passes through null audioSeconds from entity to model`() {
            // ----- Arrange -----
            val entity = stubBookEditionEntity(audioSeconds = null)

            // ----- Act -----
            val result = entity.toModel(
                authors = emptyList(),
                owned = false,
            )

            // ----- Assert -----
            result.audioSeconds shouldBe null
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
        fun `maps absent nullable fields to null and progress to zero`() {
            // ----- Arrange -----
            val entity = stubUserBookReadEntity(
                currentPage = null,
                progress = 0f,
                startedAt = null,
                finishedAt = null,
            )

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.currentPage shouldBe null
            result.progress shouldBe 0f
            result.startedAt shouldBe null
            result.finishedAt shouldBe null
        }

        @Test
        fun `maps non-null currentSeconds from entity to model`() {
            // ----- Arrange -----
            val entity = stubUserBookReadEntity(currentSeconds = 7200)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.currentSeconds shouldBe 7200
        }

        @Test
        fun `maps null currentSeconds from entity to model as null`() {
            // ----- Arrange -----
            val entity = stubUserBookReadEntity(currentSeconds = null)

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.currentSeconds shouldBe null
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
                createdAt = "2024-01-02",
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
            result.createdAt shouldBe "2024-01-02"
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
        fun `propagates non-null createdAt from entity to model`() {
            // ----- Arrange -----
            val entity = stubUserBookEntity(createdAt = "2024-04-15")

            // ----- Act -----
            val result = entity.toModel(journals = emptyList())

            // ----- Assert -----
            result.createdAt shouldBe "2024-04-15"
        }

        @Test
        fun `propagates null createdAt from entity to model as null`() {
            // ----- Arrange -----
            val entity = stubUserBookEntity(createdAt = null)

            // ----- Act -----
            val result = entity.toModel(journals = emptyList())

            // ----- Assert -----
            result.createdAt shouldBe null
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
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.listId shouldBe 20
            result.listBookId shouldBe 99
            result.bookId shouldBe 1
            result.editionId shouldBe 10
        }

        @Test
        fun `populates book and edition from Room join data`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(bookId = 1, editionId = 10)
            val editionEntity = stubBookEditionEntity(id = 10, bookId = 1)
            val editionView = stubBookEditionView(entity = editionEntity, isOwned = false)
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
            val result = listBookFull.toModel()

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
            val result = listBookFull.toModel()

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
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.edition?.owned shouldBe false
        }

        @Test
        fun `copies addedAt from ListBookEntity`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(addedAt = "2024-07-20")
            val listBookFull = stubListBookFull(listBook = listBookEntity)

            // ----- Act -----
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.addedAt shouldBe "2024-07-20"
        }

        @Test
        fun `copies null addedAt from ListBookEntity`() {
            // ----- Arrange -----
            val listBookEntity = stubListBookEntity(addedAt = null)
            val listBookFull = stubListBookFull(listBook = listBookEntity)

            // ----- Act -----
            val result = listBookFull.toModel()

            // ----- Assert -----
            result.addedAt shouldBe null
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
                listBook = stubListBookEntity(listBookId = 1, addedAt = "2024-01-01"),
            )
            val newer = stubListBookFull(
                listBook = stubListBookEntity(listBookId = 2, addedAt = "2024-06-01"),
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
                listBook = stubListBookEntity(listBookId = 1, addedAt = "2024-01-01"),
            )
            val nullDate = stubListBookFull(
                listBook = stubListBookEntity(listBookId = 2, addedAt = null),
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
                listBook = stubListBookEntity(listBookId = 10, addedAt = "2024-03-01"),
            )
            val higherIdEntry = stubListBookFull(
                listBook = stubListBookEntity(listBookId = 20, addedAt = "2024-03-01"),
            )
            val wrapper = stubBookListWithBooks(listBooks = listOf(lowerIdEntry, higherIdEntry))

            // ----- Act -----
            val result = wrapper.toModel()

            // ----- Assert -----
            result.books[0].listBookId shouldBe 20
            result.books[1].listBookId shouldBe 10
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

    // =========================================================
    // DTO -> UI (GraphQL fragment) mappers
    // =========================================================

    @Nested
    inner class EditionFragmentToBookEdition {

        @Test
        fun `maps all scalar fields from EditionFragment to BookEdition`() {
            // ----- Arrange -----
            val publisher = mockk<EditionFragment.Publisher> {
                every { name } returns "Penguin"
            }

            val image = mockk<EditionFragment.Image> {
                every { url } returns "https://example.com/cover.jpg"
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "Edition Title"
                every { book_id } returns 1
                every { isbn_10 } returns "1234567890"
                every { pages } returns 300
                every { this@mockk.publisher } returns publisher
                every { this@mockk.image } returns image
                every { release_year } returns 2020
                every { edition_format } returns "Paperback"
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.id shouldBe 10
            result.title shouldBe "Edition Title"
            result.bookId shouldBe 1
            result.isbn10 shouldBe "1234567890"
            result.pages shouldBe 300
            result.publisher shouldBe "Penguin"
            result.url shouldBe "https://example.com/cover.jpg"
            result.releaseYear shouldBe 2020
            result.format shouldBe "Paperback"
            result.owned shouldBe false
        }

        @Test
        fun `maps authors from provided list parameter`() {
            // ----- Arrange -----
            val author = Author(id = 7, name = "Jane Austen")

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition(authors = listOf(author))

            // ----- Assert -----
            result.authors.size shouldBe 1
            result.authors[0].id shouldBe 7
            result.authors[0].name shouldBe "Jane Austen"
        }

        @Test
        fun `maps null publisher name as null`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.publisher shouldBe null
        }

        @Test
        fun `url uses image url when image is present`() {
            // ----- Arrange -----
            val image = mockk<EditionFragment.Image> {
                every { url } returns "https://example.com/primary.jpg"
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { this@mockk.image } returns image
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe "https://example.com/primary.jpg"
        }

        @Test
        fun `url falls back to first fallbackImages url when image is null`() {
            // ----- Arrange -----
            val fallback = mockk<EditionFragment.FallbackImage> {
                every { url } returns "https://example.com/fallback.jpg"
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns listOf(fallback)
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe "https://example.com/fallback.jpg"
        }

        @Test
        fun `url is null when image is null and fallbackImages is empty`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe null
        }

        @Test
        fun `url is null when image is null and all fallbackImages have null url`() {
            // ----- Arrange -----
            val fallback = mockk<EditionFragment.FallbackImage> {
                every { url } returns null
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns listOf(fallback)
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe null
        }

        @Test
        fun `maps null release_year as -1`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.releaseYear shouldBe -1
        }

        @Test
        fun `maps null edition_format as empty string`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.format shouldBe ""
        }

        @Test
        fun `defaults authors to empty list when not supplied`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }

        @Test
        fun `maps non-null canonical_id on fragment to canonicalId on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns 55
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.canonicalId shouldBe 55
        }

        @Test
        fun `maps null canonical_id on fragment to null canonicalId on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.canonicalId shouldBe null
        }

        @Test
        fun `maps non-null audio_seconds from fragment to audioSeconds on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns 3600
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.audioSeconds shouldBe 3600
        }

        @Test
        fun `maps null audio_seconds from fragment to null audioSeconds on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.audioSeconds shouldBe null
        }
    }

    @Nested
    inner class EditionDetailFragmentToBookEdition {

        @Test
        fun `extracts authors from contributions and maps them`() {
            // ----- Arrange -----
            val authorInner = mockk<EditionDetailFragment.Contribution.Author> {
                every { id } returns 5
                every { name } returns "George Orwell"
            }

            val contribution = mockk<EditionDetailFragment.Contribution> {
                every { author } returns authorInner
            }

            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns "Animal Farm"
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { pages } returns 112
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns 1945
                every { edition_format } returns "Hardcover"
                every { audio_seconds } returns null
                every { contributions } returns listOf(contribution)
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors.size shouldBe 1
            result.authors[0].id shouldBe 5
            result.authors[0].name shouldBe "George Orwell"
        }

        @Test
        fun `skips contributions whose author is null`() {
            // ----- Arrange -----
            val contribution = mockk<EditionDetailFragment.Contribution> {
                every { author } returns null
            }

            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { contributions } returns listOf(contribution)
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }

        @Test
        fun `produces empty authors list when contributions is empty`() {
            // ----- Arrange -----
            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }

        @Test
        fun `maps scalar edition fields correctly`() {
            // ----- Arrange -----
            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 77
                every { canonical_id } returns null
                every { title } returns "1984"
                every { book_id } returns 3
                every { isbn_10 } returns "0451524934"
                every { pages } returns 328
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns 1949
                every { edition_format } returns "Paperback"
                every { audio_seconds } returns null
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.id shouldBe 77
            result.title shouldBe "1984"
            result.bookId shouldBe 3
            result.isbn10 shouldBe "0451524934"
            result.pages shouldBe 328
            result.releaseYear shouldBe 1949
            result.format shouldBe "Paperback"
        }

        @Test
        fun `maps non-null audio_seconds from EditionDetailFragment to audioSeconds on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns 7200
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.audioSeconds shouldBe 7200
        }

        @Test
        fun `always sets localImagePath to null on the resulting BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.localImagePath shouldBe null
        }
    }

    @Nested
    inner class ListBookFragmentToListBook {

        private fun stubListBookFragment(
            id: Int = 99,
            listId: Int = 20,
            bookId: Int = 1,
            editionId: Int? = 10,
            createdAt: String? = null,
        ): nl.rhaydus.softcover.fragment.ListBookFragment = mockk {
            every { this@mockk.id } returns id
            every { list_id } returns listId
            every { book_id } returns bookId
            every { edition_id } returns editionId
            every { created_at } returns createdAt
        }

        @Test
        fun `returns null when edition_id is null`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = null)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns ids-only ListBook when edition_id is present`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(
                id = 99,
                listId = 20,
                bookId = 1,
                editionId = 10,
            )

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.listBookId shouldBe 99
            result?.listId shouldBe 20
            result?.bookId shouldBe 1
            result?.editionId shouldBe 10
        }

        @Test
        fun `book and edition are null on the GraphQL path`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.book shouldBe null
            result?.edition shouldBe null
        }

        @Test
        fun `propagates created_at into addedAt`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10, createdAt = "2024-03-15")

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.addedAt shouldBe "2024-03-15"
        }

        @Test
        fun `propagates null created_at as null addedAt`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10, createdAt = null)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.addedAt shouldBe null
        }
    }

    @Nested
    inner class UserBookFragmentToBook {

        @Test
        fun `returns null when bookListFragment companion extension returns null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)

            val bookInner = mockk<UserBookFragment.Book>()
            val fragment = mockk<UserBookFragment> {
                every { book } returns bookInner
            }

            every {
                with(UserBookFragment.Book.Companion) { bookInner.bookListFragment() }
            } returns null

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when edition is null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns null
                every { progress_updated_journal } returns emptyList()
                every { status_currently_reading_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns null
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns mockk {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "Test"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns non-null Book when edition is present and all fields map correctly`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "My Edition"
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns 4.0
                every { image } returns null
                every { release_year } returns 2020
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { users_count } returns 250
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { status_currently_reading_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.id shouldBe 100
            result?.title shouldBe "My Book"
            result?.defaultEdition shouldBe null
            result?.description shouldBe ""
            result?.usersCount shouldBe 250
            result?.editions?.size shouldBe 1
            result?.editions?.get(0)?.id shouldBe 10
        }

        @Test
        fun `maps usersCount from BookListFragment users_count when non-null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { users_count } returns 1234
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { status_currently_reading_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.usersCount shouldBe 1234
        }

        @Test
        fun `sets canonicalId when canonical id differs from book id`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val canonical = mockk<nl.rhaydus.softcover.fragment.BookListFragment.Canonical> {
                every { id } returns 999
            }

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { this@mockk.canonical } returns canonical
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { users_count } returns 0
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { status_currently_reading_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe 999
        }

        @Test
        fun `sets canonicalId to null when canonical id equals book id`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val canonical = mockk<nl.rhaydus.softcover.fragment.BookListFragment.Canonical> {
                every { id } returns 100
            }

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { this@mockk.canonical } returns canonical
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { users_count } returns 0
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { status_currently_reading_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe null
        }

        @Test
        fun `overrides edition bookId with parent book id when edition book_id differs`() {
            // ----- Arrange -----
            // Canonicalized editions can have a book_id that points to a different book than the
            // parent list entry. The mapper must copy the parent's id so that Room's @Relation join
            // on book_editions.bookId = books.id keeps the edition attached to the correct book.
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 999 // differs from parent book id
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100 // parent book id
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { users_count } returns 0
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { status_currently_reading_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.editions?.get(0)?.bookId shouldBe 100
        }

        // ---- journal alias tests ----

        private fun stubMinimalUserBookFragment(
            progressUpdated: List<UserBookFragment.Progress_updated_journal> = emptyList(),
            statusCurrentlyReading: List<UserBookFragment.Status_currently_reading_journal> = emptyList(),
            userBookReadFinished: List<UserBookFragment.User_book_read_finished_journal> = emptyList(),
            statusStopped: List<UserBookFragment.Status_stopped_journal> = emptyList(),
            createdAt: String = "2024-01-01",
        ): UserBookFragment {
            val bookInner = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }
            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "Test Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { users_count } returns 0
            }

            every {
                with(UserBookFragment.Book.Companion) { bookInner.bookListFragment() }
            } returns bookListFragmentModel
            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            return mockk<UserBookFragment> {
                every { book } returns bookInner
                every { edition } returns editionInner
                every { progress_updated_journal } returns progressUpdated
                every { status_currently_reading_journal } returns statusCurrentlyReading
                every { user_book_read_finished_journal } returns userBookReadFinished
                every { status_stopped_journal } returns statusStopped
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns createdAt
            }
        }

        private fun stubReadingJournalFragment(event: String, updatedAt: String = "2024-01-01"): ReadingJournalFragment = mockk {
            every { this@mockk.event } returns event
            every { this@mockk.updated_at } returns updatedAt
        }

        @Test
        fun `collects journals from progress_updated_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Progress_updated_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "progress_updated")
            val journalEntry = mockk<UserBookFragment.Progress_updated_journal> {
                every { with(UserBookFragment.Progress_updated_journal.Companion) { progressUpdatedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(progressUpdated = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "progress_updated"
        }

        @Test
        fun `collects journals from status_currently_reading_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Status_currently_reading_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "status_currently_reading")
            val journalEntry = mockk<UserBookFragment.Status_currently_reading_journal> {
                every { with(UserBookFragment.Status_currently_reading_journal.Companion) { statusCurrentlyReadingJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(statusCurrentlyReading = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "status_currently_reading"
        }

        @Test
        fun `collects journals from user_book_read_finished_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read_finished_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "user_book_read_finished")
            val journalEntry = mockk<UserBookFragment.User_book_read_finished_journal> {
                every { with(UserBookFragment.User_book_read_finished_journal.Companion) { userBookReadFinishedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(userBookReadFinished = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "user_book_read_finished"
        }

        @Test
        fun `collects journals from status_stopped_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Status_stopped_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "status_stopped")
            val journalEntry = mockk<UserBookFragment.Status_stopped_journal> {
                every { with(UserBookFragment.Status_stopped_journal.Companion) { statusStoppedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(statusStopped = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "status_stopped"
        }

        @Test
        fun `combines journals from all four aliased lists`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Progress_updated_journal.Companion)
            mockkObject(UserBookFragment.Status_currently_reading_journal.Companion)
            mockkObject(UserBookFragment.User_book_read_finished_journal.Companion)
            mockkObject(UserBookFragment.Status_stopped_journal.Companion)

            val progressEntry = mockk<UserBookFragment.Progress_updated_journal> {
                every { with(UserBookFragment.Progress_updated_journal.Companion) { progressUpdatedJournalFragment() } } returns stubReadingJournalFragment("progress_updated")
            }
            val currentlyReadingEntry = mockk<UserBookFragment.Status_currently_reading_journal> {
                every { with(UserBookFragment.Status_currently_reading_journal.Companion) { statusCurrentlyReadingJournalFragment() } } returns stubReadingJournalFragment("status_currently_reading")
            }
            val readEntry = mockk<UserBookFragment.User_book_read_finished_journal> {
                every { with(UserBookFragment.User_book_read_finished_journal.Companion) { userBookReadFinishedJournalFragment() } } returns stubReadingJournalFragment("user_book_read_finished")
            }
            val stoppedEntry = mockk<UserBookFragment.Status_stopped_journal> {
                every { with(UserBookFragment.Status_stopped_journal.Companion) { statusStoppedJournalFragment() } } returns stubReadingJournalFragment("status_stopped")
            }
            val fragment = stubMinimalUserBookFragment(
                progressUpdated = listOf(progressEntry),
                statusCurrentlyReading = listOf(currentlyReadingEntry),
                userBookReadFinished = listOf(readEntry),
                statusStopped = listOf(stoppedEntry),
            )

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            val events = result?.userBook?.journals?.map { it.event }
            events?.size shouldBe 4
            events shouldBe listOf(
                "progress_updated",
                "status_currently_reading",
                "user_book_read_finished",
                "status_stopped",
            )
        }

        @Test
        fun `propagates created_at from fragment to UserBook createdAt`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            val fragment = stubMinimalUserBookFragment(createdAt = "2024-05-20")

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.createdAt shouldBe "2024-05-20"
        }

        @Test
        fun `propagates a different created_at value from fragment to UserBook createdAt`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            val fragment = stubMinimalUserBookFragment(createdAt = "2023-11-01")

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.createdAt shouldBe "2023-11-01"
        }
    }

    @Nested
    inner class BookDetailFragmentToBook {

        @Test
        fun `returns null when default_physical_edition is null`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_physical_edition.Companion)

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { description } returns null
                every { users_count } returns 0
                every { default_physical_edition } returns null
            }

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns non-null Book when default_physical_edition is present`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_physical_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_physical_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "Hardcover Edition"
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns 400
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns 2021
                every { edition_format } returns "Hardcover"
                every { audio_seconds } returns null
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns 4.5
                every { image } returns null
                every { release_year } returns 2021
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { description } returns "A great book."
                every { users_count } returns 500
                every { default_physical_edition } returns defaultEditionInner
            }

            every {
                with(BookDetailFragment.Default_physical_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.id shouldBe 1
            result?.title shouldBe "My Book"
            result?.description shouldBe "A great book."
            result?.usersCount shouldBe 500
            result?.userBook shouldBe null
            result?.userBookRead shouldBe null
            result?.defaultEdition?.id shouldBe 10
            result?.editions?.size shouldBe 1
            result?.editions?.get(0)?.id shouldBe 10
        }

        @Test
        fun `maps description as empty string when null`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_physical_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_physical_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { description } returns null
                every { users_count } returns 0
                every { default_physical_edition } returns defaultEditionInner
            }

            every {
                with(BookDetailFragment.Default_physical_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.description shouldBe ""
        }

        @Test
        fun `sets canonicalId when canonical id differs from book id`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_physical_edition.Companion)

            val canonical = mockk<BookDetailFragment.Canonical> {
                every { id } returns 999
            }

            val defaultEditionInner = mockk<BookDetailFragment.Default_physical_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { this@mockk.canonical } returns canonical
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { description } returns null
                every { users_count } returns 0
                every { default_physical_edition } returns defaultEditionInner
            }

            every {
                with(BookDetailFragment.Default_physical_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe 999
        }

        @Test
        fun `sets canonicalId to null when canonical id equals book id`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_physical_edition.Companion)

            val canonical = mockk<BookDetailFragment.Canonical> {
                every { id } returns 1
            }

            val defaultEditionInner = mockk<BookDetailFragment.Default_physical_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { this@mockk.canonical } returns canonical
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { description } returns null
                every { users_count } returns 0
                every { default_physical_edition } returns defaultEditionInner
            }

            every {
                with(BookDetailFragment.Default_physical_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe null
        }

        @Test
        fun `overrides edition bookId with parent book id when edition book_id differs`() {
            // ----- Arrange -----
            // Canonicalized editions can have a book_id that points to a different book than the
            // parent list entry. The mapper must copy the parent's id so that Room's @Relation join
            // on book_editions.bookId = books.id keeps the edition attached to the correct book.
            mockkObject(BookDetailFragment.Default_physical_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_physical_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 999 // differs from parent book id
                every { isbn_10 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1 // parent book id
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { book_series } returns emptyList()
                every { contributions } returns emptyList()
                every { description } returns null
                every { users_count } returns 0
                every { default_physical_edition } returns defaultEditionInner
            }

            every {
                with(BookDetailFragment.Default_physical_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.defaultEdition?.bookId shouldBe 1
            result?.editions?.get(0)?.bookId shouldBe 1
        }
    }

    @Nested
    inner class BookEditionIsAudiobook {

        @Test
        fun `returns true when audioSeconds is positive`() {
            // ----- Arrange -----
            val edition = BookEdition(
                id = 1,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                pages = null,
                audioSeconds = 3600,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Audiobook",
                owned = false,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe true
        }

        @Test
        fun `returns false when audioSeconds is zero`() {
            // ----- Arrange -----
            val edition = BookEdition(
                id = 1,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                pages = null,
                audioSeconds = 0,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Audiobook",
                owned = false,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `returns false when audioSeconds is null`() {
            // ----- Arrange -----
            val edition = BookEdition(
                id = 1,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                pages = null,
                audioSeconds = null,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Paperback",
                owned = false,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }
    }
}
