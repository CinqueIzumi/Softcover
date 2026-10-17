package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.baseBook
import nl.rhaydus.softcover.core.book.domain.model.BookImport
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.exception.BookImportPendingException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddBookByIsbnUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var fetchBookByIdUseCase: FetchBookByIdUseCase
    private lateinit var useCase: AddBookByIsbnUseCase

    private val isbn = "9780374710781"
    private val bookId = 42
    private val editionId = 99
    private val book = baseBook

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        fetchBookByIdUseCase = mockk()
        useCase = AddBookByIsbnUseCase(
            booksRepository = booksRepository,
            fetchBookByIdUseCase = fetchBookByIdUseCase,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `success returns Found with hydrated book and editionId from CreatedBook`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } returns BookImport.Created(
                bookId = bookId,
                editionId = editionId,
            )

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.success(book)

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe IsbnLookupResult.Found(
                book = book,
                editionId = editionId,
            )
        }

        @Test
        fun `null editionId in CreatedBook falls back to hydrated book currentEdition id`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } returns BookImport.Created(
                bookId = bookId,
                editionId = null,
            )

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.success(book)

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isSuccess shouldBe true

            // PreviewData.baseBook.currentEdition resolves to baseEdition (id = 1) via defaultEdition
            result.getOrNull() shouldBe IsbnLookupResult.Found(
                book = book,
                editionId = 1,
            )
        }

        @Test
        fun `repository addBookByIsbn throws returns failure`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } throws OfflineException()

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isFailure shouldBe true
        }

        @Test
        fun `fetchBookByIdUseCase returns failure causes overall failure via getOrThrow`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } returns BookImport.Created(
                bookId = bookId,
                editionId = editionId,
            )

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.failure(RuntimeException("hydration failed"))

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isFailure shouldBe true
        }

        @Test
        fun `Fetching then Created re-calls the repository and succeeds with the hydrated book`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } returnsMany listOf(
                BookImport.Fetching,
                BookImport.Created(
                    bookId = bookId,
                    editionId = editionId,
                ),
            )

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.success(book)

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.getOrNull() shouldBe IsbnLookupResult.Found(
                book = book,
                editionId = editionId,
            )
            coVerify(exactly = 2) {
                booksRepository.addBookByIsbn(isbn = isbn)
            }
        }

        @Test
        fun `always Fetching fails with BookImportPendingException once the wait budget is spent`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } returns BookImport.Fetching

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.exceptionOrNull().shouldBeInstanceOf<BookImportPendingException>()
            testScheduler.currentTime shouldBe 60_000L
            coVerify(exactly = 0) {
                fetchBookByIdUseCase(id = any())
            }
        }

        @Test
        fun `spaces Fetching attempts by at least one second and at most the backoff cap`() = runTest {
            // ----- Arrange -----
            val callTimes = mutableListOf<Long>()

            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } coAnswers {
                callTimes += testScheduler.currentTime
                BookImport.Fetching
            }

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isFailure shouldBe true

            val gaps = callTimes.zipWithNext { earlier, later -> later - earlier }

            gaps.forEach { gap ->
                (gap in 1_000L..5_000L) shouldBe true
            }
            gaps.first() shouldBe 1_000L
            testScheduler.currentTime shouldBe callTimes.last()
        }

        @Test
        fun `first call is made without waiting`() = runTest {
            // ----- Arrange -----
            var firstCallTime: Long? = null

            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } coAnswers {
                if (firstCallTime == null) firstCallTime = testScheduler.currentTime
                BookImport.Fetching
            }

            // ----- Act -----
            useCase(isbn)

            // ----- Assert -----
            firstCallTime shouldBe 0L
        }
    }
}
