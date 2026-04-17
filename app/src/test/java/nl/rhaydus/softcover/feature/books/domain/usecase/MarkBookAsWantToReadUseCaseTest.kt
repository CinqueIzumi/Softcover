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

class MarkBookAsWantToReadUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: MarkBookAsWantToReadUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = MarkBookAsWantToReadUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `marks book as want to read then caches the updated book`() = runTest {
            // ----- Arrange -----
            val bookId = 77
            val updatedBook = mockk<Book>()

            coEvery {
                booksRepository.markBookAsWantToRead(bookId = bookId)
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.markBookAsWantToRead(bookId = bookId) }
            coVerify(exactly = 1) { booksRepository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `returns failure when markBookAsWantToRead throws`() = runTest {
            // ----- Arrange -----
            val bookId = 77
            val expectedError = RuntimeException("network error")

            coEvery {
                booksRepository.markBookAsWantToRead(bookId = bookId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }

        @Test
        fun `returns failure when cacheBook throws`() = runTest {
            // ----- Arrange -----
            val bookId = 77
            val updatedBook = mockk<Book>()
            val expectedError = RuntimeException("cache error")

            coEvery {
                booksRepository.markBookAsWantToRead(bookId = bookId)
            } returns updatedBook

            coEvery {
                booksRepository.cacheBook(book = updatedBook)
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
