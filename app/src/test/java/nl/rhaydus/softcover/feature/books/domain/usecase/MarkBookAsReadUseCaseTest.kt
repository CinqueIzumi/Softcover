package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MarkBookAsReadUseCaseTest {

    private lateinit var repository: BooksRepository
    private lateinit var useCase: MarkBookAsReadUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = MarkBookAsReadUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `marks book as read then caches the updated book`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every { inputBook.status } returns BookStatus.Reading

            coEvery {
                repository.markBookAsRead(book = inputBook, editionId = null)
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe ShelfMutationOutcome.Applied
            coVerify(exactly = 1) { repository.markBookAsRead(book = inputBook, editionId = null) }
            coVerify(exactly = 1) { repository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `forwards a non-null editionId to the repository`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val editionId = 99
            val updatedBook = mockk<Book>()

            every { inputBook.status } returns BookStatus.Reading

            coEvery {
                repository.markBookAsRead(book = inputBook, editionId = editionId)
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            useCase(book = inputBook, editionId = editionId)

            // ----- Assert -----
            coVerify(exactly = 1) { repository.markBookAsRead(book = inputBook, editionId = editionId) }
        }

        @Test
        fun `forwards null editionId to the repository when omitted`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every { inputBook.status } returns BookStatus.Reading

            coEvery {
                repository.markBookAsRead(book = inputBook, editionId = null)
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            useCase(book = inputBook)

            // ----- Assert -----
            coVerify(exactly = 1) { repository.markBookAsRead(book = inputBook, editionId = null) }
        }

        @Test
        fun `returns failure when markBookAsRead throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val expectedError = RuntimeException("network error")

            every { inputBook.status } returns BookStatus.Reading

            coEvery {
                repository.markBookAsRead(book = inputBook, editionId = null)
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { repository.cacheBook(book = any()) }
        }

        @Test
        fun `returns failure when cacheBook throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()
            val expectedError = RuntimeException("cache error")

            every { inputBook.status } returns BookStatus.Reading

            coEvery {
                repository.markBookAsRead(book = inputBook, editionId = null)
            } returns updatedBook

            coEvery {
                repository.cacheBook(book = updatedBook)
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns NoChange and does not touch repository when book is already in target status`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every { inputBook.status } returns BookStatus.Read

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.NoChange
            coVerify(exactly = 0) { repository.markBookAsRead(any(), any()) }
            coVerify(exactly = 0) { repository.cacheBook(any()) }
        }
    }
}
