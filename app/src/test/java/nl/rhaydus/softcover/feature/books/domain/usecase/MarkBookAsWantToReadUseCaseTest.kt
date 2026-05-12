package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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
        fun `marks book as want to read and returns success`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()
            val updatedBook = mockk<Book>()

            coEvery {
                booksRepository.markBookAsWantToRead(book = book)
            } returns updatedBook

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { booksRepository.markBookAsWantToRead(book = book) }
        }

        @Test
        fun `returns failure when markBookAsWantToRead throws`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()
            val expectedError = RuntimeException("network error")

            coEvery {
                booksRepository.markBookAsWantToRead(book = book)
            } throws expectedError

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
