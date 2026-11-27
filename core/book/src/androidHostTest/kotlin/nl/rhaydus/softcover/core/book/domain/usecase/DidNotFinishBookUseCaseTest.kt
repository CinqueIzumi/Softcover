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

class DidNotFinishBookUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: DidNotFinishBookUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = DidNotFinishBookUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `marks book as did-not-finish and returns Applied`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Reading

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsDidNotFinish(
                    book = inputBook,
                    editionId = null,
                )
            } returns updatedBook

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.Applied

            coVerify(exactly = 1) {
                booksRepository.markBookAsDidNotFinish(
                    book = inputBook,
                    editionId = null,
                )
            }
        }

        @Test
        fun `returns failure when markBookAsDidNotFinish throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val expectedError = RuntimeException("network error")

            every {
                inputBook.status
            } returns BookStatus.Reading

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsDidNotFinish(
                    book = inputBook,
                    editionId = null,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns NoChange and does not touch repository when book is already DidNotFinish`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.DidNotFinish

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.NoChange

            coVerify(exactly = 0) {
                booksRepository.markBookAsDidNotFinish(
                    book = any(),
                    editionId = any(),
                )
            }
        }
    }
}
