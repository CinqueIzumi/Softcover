package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MarkBookAsReadingUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: MarkBookAsReadingUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = MarkBookAsReadingUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `marks book as reading and returns Applied`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.WantToRead

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = null,
                )
            } returns updatedBook

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe ShelfMutationOutcome.Applied
            coVerify(exactly = 1) {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = null,
                )
            }
        }

        @Test
        fun `forwards a non-null editionId to the repository`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val editionId = 99
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.WantToRead

            every {
                inputBook.userBook
            } returns null

            coEvery {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = editionId,
                )
            } returns updatedBook

            // ----- Act -----
            useCase(
                book = inputBook,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = editionId,
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
            } returns BookStatus.WantToRead

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = null,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `forwards the current edition when none is requested and the book is off the shelf`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val currentEdition = mockk<BookEdition>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.WantToRead

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns currentEdition

            every {
                currentEdition.id
            } returns 7

            coEvery {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = 7,
                )
            } returns updatedBook

            // ----- Act -----
            useCase(inputBook)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.markBookAsReading(
                    book = inputBook,
                    editionId = 7,
                )
            }
        }

        @Test
        fun `returns NoChange and does not touch repository when book is already in target status`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Reading

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.NoChange
            coVerify(exactly = 0) {
                booksRepository.markBookAsReading(
                    book = any(),
                    editionId = any(),
                )
            }
        }
    }
}
