package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResumeIfPausedUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: ResumeIfPausedUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = ResumeIfPausedUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `marks a paused book as reading and returns the updated book`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val resumedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Paused

            coEvery {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = null,
                )
            } returns resumedBook

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe resumedBook
            coVerify(exactly = 1) {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = null,
                )
            }
        }

        @Test
        fun `returns a non-paused book unchanged without touching the repository`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Reading

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe inputBook
            coVerify(exactly = 0) {
                booksRepository.markBookAsReading(
                    book = any(),
                    editionId = any(),
                )
            }
        }

        @Test
        fun `returns failure when markBookAsReading throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val expectedError = RuntimeException("network error")

            every {
                inputBook.status
            } returns BookStatus.Paused

            coEvery {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = null,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
