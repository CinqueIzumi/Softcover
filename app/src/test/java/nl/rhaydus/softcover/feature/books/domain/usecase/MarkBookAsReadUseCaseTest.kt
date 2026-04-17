package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
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

            coEvery {
                repository.markBookAsRead(book = inputBook)
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { repository.markBookAsRead(book = inputBook) }
            coVerify(exactly = 1) { repository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `returns failure when markBookAsRead throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val expectedError = RuntimeException("network error")

            coEvery {
                repository.markBookAsRead(book = inputBook)
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

            coEvery {
                repository.markBookAsRead(book = inputBook)
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
    }
}
