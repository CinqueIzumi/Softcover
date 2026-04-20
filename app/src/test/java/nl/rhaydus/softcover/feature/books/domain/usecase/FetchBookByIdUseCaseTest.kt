package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FetchBookByIdUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: FetchBookByIdUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = FetchBookByIdUseCase(booksRepository = booksRepository)
    }

    private fun buildEdition(id: Int, owned: Boolean = false): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 0,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList<Author>(),
        releaseYear = 2020,
        format = "Paperback",
        owned = owned,
    )

    private fun buildBook(
        id: Int,
        editions: List<BookEdition> = emptyList(),
    ): Book = Book(
        id = id,
        title = "Title",
        editions = editions,
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList<Author>(),
        usersCount = 0,
        bookSeries = null,
        positionInSeries = null,
        userBook = null,
        userBookRead = null,
    )

    @Nested
    inner class Invoke {

        @Test
        fun `returns success with remote book and its fetched editions`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteEditions = listOf(buildEdition(id = 1), buildEdition(id = 2))
            val remoteBook = buildBook(id = bookId)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe remoteBook.copy(editions = remoteEditions)
        }

        @Test
        fun `result book has the editions returned by getEditionsByBookId`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val edition1 = buildEdition(id = 10)
            val edition2 = buildEdition(id = 20)
            val remoteBook = buildBook(id = bookId)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns listOf(edition1, edition2)

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            val editions = result.getOrNull()!!.editions
            editions.size shouldBe 2
            editions[0] shouldBe edition1
            editions[1] shouldBe edition2
        }

        @Test
        fun `calls getEditionsByBookId exactly once with the original id when book id matches`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteBook = buildBook(id = bookId)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns emptyList()

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = bookId) }
        }

        @Test
        fun `re-fetches editions with canonical id when fetchBookById returns a book with a different id`() = runTest {
            // ----- Arrange -----
            val requestedId = 100
            val canonicalId = 200
            val initialEditions = listOf(buildEdition(id = 1))
            val canonicalEditions = listOf(buildEdition(id = 2), buildEdition(id = 3))
            val remoteBook = buildBook(id = canonicalId)

            coEvery { booksRepository.fetchBookById(id = requestedId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = requestedId) } returns initialEditions
            coEvery { booksRepository.getEditionsByBookId(bookId = canonicalId) } returns canonicalEditions

            // ----- Act -----
            useCase(requestedId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = requestedId) }
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = canonicalId) }
        }

        @Test
        fun `uses canonical editions (not original editions) when book id is redirected`() = runTest {
            // ----- Arrange -----
            val requestedId = 100
            val canonicalId = 200
            val initialEditions = listOf(buildEdition(id = 1))
            val canonicalEditions = listOf(buildEdition(id = 2), buildEdition(id = 3))
            val remoteBook = buildBook(id = canonicalId)

            coEvery { booksRepository.fetchBookById(id = requestedId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = requestedId) } returns initialEditions
            coEvery { booksRepository.getEditionsByBookId(bookId = canonicalId) } returns canonicalEditions

            // ----- Act -----
            val result = useCase(requestedId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull()!!.editions shouldBe canonicalEditions
        }

        @Test
        fun `does not call getEditionsByBookId a second time when book id matches the requested id`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteBook = buildBook(id = bookId)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns emptyList()

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = any()) }
        }

        @Test
        fun `returns failure when fetchBookById throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedError = IllegalStateException("network error")

            coEvery { booksRepository.fetchBookById(id = bookId) } throws expectedError
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns emptyList()

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when getEditionsByBookId throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val remoteBook = buildBook(id = bookId)
            val expectedError = IllegalStateException("editions fetch failed")

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } throws expectedError

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
