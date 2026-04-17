package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FetchBookByIdUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var useCase: FetchBookByIdUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        getAllUserBooksUseCase = mockk()
        useCase = FetchBookByIdUseCase(
            booksRepository = booksRepository,
            getAllUserBooksUseCase = getAllUserBooksUseCase,
        )
    }

    private fun stubEditions(): List<BookEdition> = listOf(mockk(), mockk())

    /**
     * Creates a Book mock that can survive a `.copy(editions, userBook, userBookRead)` call.
     * The [copyResult] is what the `copy` invocation returns; when null the mock returns itself.
     */
    private fun stubBook(
        id: Int,
        userBook: UserBook? = null,
        userBookRead: UserBookRead? = null,
        copyResult: Book? = null,
        editions: List<BookEdition> = emptyList(),
    ): Book = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.userBook } returns userBook
        every { this@mockk.userBookRead } returns userBookRead
        every { this@mockk.editions } returns editions
        every {
            this@mockk.copy(
                editions = any(),
                userBook = any(),
                userBookRead = any(),
            )
        } returns (copyResult ?: this@mockk)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success with remote book metadata and overlaid local userBook and userBookRead`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteEditions = stubEditions()
            val localUserBook = mockk<UserBook>()
            val localUserBookRead = mockk<UserBookRead>()

            val mergedBook = mockk<Book>()
            val remoteBook = stubBook(id = bookId, copyResult = mergedBook)
            val localBook = stubBook(
                id = bookId,
                userBook = localUserBook,
                userBookRead = localUserBookRead,
            )

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } returns remoteBook

            coEvery {
                booksRepository.getEditionsByBookId(bookId = bookId)
            } returns remoteEditions

            every {
                getAllUserBooksUseCase()
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe mergedBook
        }

        @Test
        fun `overlays only userBook and userBookRead from local cache onto remote book`() = runTest {
            // ----- Arrange -----
            val bookId = 10
            val remoteEditions = stubEditions()
            val localUserBook = mockk<UserBook>()
            val localUserBookRead = mockk<UserBookRead>()
            val mergedBook = mockk<Book>()

            val remoteBook = stubBook(id = bookId, copyResult = mergedBook)
            val localBook = stubBook(
                id = bookId,
                userBook = localUserBook,
                userBookRead = localUserBookRead,
            )

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns flowOf(listOf(localBook))

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            io.mockk.verify(exactly = 1) {
                remoteBook.copy(
                    editions = remoteEditions,
                    userBook = localUserBook,
                    userBookRead = localUserBookRead,
                )
            }
        }

        @Test
        fun `returns remote book with null userBook and userBookRead when no local book matches`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteEditions = stubEditions()
            val mergedBook = mockk<Book>()

            val remoteBook = stubBook(id = bookId, copyResult = mergedBook)
            val unrelatedLocalBook = stubBook(id = 99)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns flowOf(listOf(unrelatedLocalBook))

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            io.mockk.verify(exactly = 1) {
                remoteBook.copy(
                    editions = remoteEditions,
                    userBook = null,
                    userBookRead = null,
                )
            }
        }

        @Test
        fun `returns remote book with null userBook and userBookRead when user books list is empty`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val remoteEditions = stubEditions()
            val mergedBook = mockk<Book>()
            val remoteBook = stubBook(id = bookId, copyResult = mergedBook)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns flowOf(emptyList())

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            io.mockk.verify(exactly = 1) {
                remoteBook.copy(
                    editions = remoteEditions,
                    userBook = null,
                    userBookRead = null,
                )
            }
        }

        @Test
        fun `returns remote book with null userBook and userBookRead when user books flow emits nothing`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val remoteEditions = stubEditions()
            val mergedBook = mockk<Book>()
            val remoteBook = stubBook(id = bookId, copyResult = mergedBook)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns emptyFlow()

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            io.mockk.verify(exactly = 1) {
                remoteBook.copy(
                    editions = remoteEditions,
                    userBook = null,
                    userBookRead = null,
                )
            }
        }

        @Test
        fun `fetches book and initial editions in parallel`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val remoteEditions = stubEditions()
            val remoteBook = stubBook(id = bookId)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns flowOf(emptyList())

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.fetchBookById(id = bookId) }
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = bookId) }
        }

        @Test
        fun `re-fetches editions with canonical id when fetchBookById returns a redirected book`() = runTest {
            // ----- Arrange -----
            val requestedId = 100
            val canonicalId = 200
            val initialEditions = listOf<BookEdition>(mockk())
            val canonicalEditions = listOf<BookEdition>(mockk(), mockk())
            val mergedBook = mockk<Book>()

            val remoteBook = stubBook(id = canonicalId, copyResult = mergedBook)

            coEvery { booksRepository.fetchBookById(id = requestedId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = requestedId) } returns initialEditions
            coEvery { booksRepository.getEditionsByBookId(bookId = canonicalId) } returns canonicalEditions
            every { getAllUserBooksUseCase() } returns flowOf(emptyList())

            // ----- Act -----
            useCase(requestedId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = requestedId) }
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = canonicalId) }
            io.mockk.verify(exactly = 1) {
                remoteBook.copy(
                    editions = canonicalEditions,
                    userBook = null,
                    userBookRead = null,
                )
            }
        }

        @Test
        fun `does not issue a second getEditionsByBookId call when book id matches the requested id`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteEditions = stubEditions()
            val mergedBook = mockk<Book>()
            val remoteBook = stubBook(id = bookId, copyResult = mergedBook)

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns flowOf(emptyList())

            // ----- Act -----
            useCase(bookId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.getEditionsByBookId(bookId = bookId) }
        }

        @Test
        fun `returns failure when fetchBookById throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedError = IllegalStateException("boom")

            coEvery { booksRepository.fetchBookById(id = bookId) } throws expectedError
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns emptyList()

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when user books flow throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val remoteEditions = stubEditions()
            val remoteBook = stubBook(id = bookId)
            val expectedError = IllegalStateException("flow boom")

            coEvery { booksRepository.fetchBookById(id = bookId) } returns remoteBook
            coEvery { booksRepository.getEditionsByBookId(bookId = bookId) } returns remoteEditions
            every { getAllUserBooksUseCase() } returns flow { throw expectedError }

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
