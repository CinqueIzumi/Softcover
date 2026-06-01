package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.book.domain.model.CreatedBook
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
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
    private val book = PreviewData.baseBook

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
            } returns CreatedBook(bookId = bookId, editionId = editionId)

            coEvery { fetchBookByIdUseCase(id = bookId) } returns Result.success(book)

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe IsbnLookupResult.Found(book = book, editionId = editionId)

            coVerify(exactly = 1) { booksRepository.addBookByIsbn(isbn = isbn) }
            coVerify(exactly = 1) { fetchBookByIdUseCase(id = bookId) }
        }

        @Test
        fun `null editionId in CreatedBook falls back to hydrated book currentEdition id`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.addBookByIsbn(isbn = isbn)
            } returns CreatedBook(bookId = bookId, editionId = null)

            coEvery { fetchBookByIdUseCase(id = bookId) } returns Result.success(book)

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isSuccess shouldBe true

            // PreviewData.baseBook.currentEdition resolves to baseEdition (id = 1) via defaultEdition
            result.getOrNull() shouldBe IsbnLookupResult.Found(book = book, editionId = 1)
        }

        @Test
        fun `repository addBookByIsbn throws returns failure`() = runTest {
            // ----- Arrange -----
            coEvery { booksRepository.addBookByIsbn(isbn = isbn) } throws OfflineException()

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
            } returns CreatedBook(bookId = bookId, editionId = editionId)

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.failure(RuntimeException("hydration failed"))

            // ----- Act -----
            val result = useCase(isbn)

            // ----- Assert -----
            result.isFailure shouldBe true
        }
    }
}
