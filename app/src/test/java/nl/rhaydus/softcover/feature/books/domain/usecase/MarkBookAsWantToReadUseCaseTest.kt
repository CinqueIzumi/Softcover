package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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

            every { book.status } returns BookStatus.None

            coEvery {
                booksRepository.markBookAsWantToRead(book = book, editionId = null)
            } returns updatedBook

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe ShelfMutationOutcome.Applied

            coVerify(exactly = 1) { booksRepository.markBookAsWantToRead(book = book, editionId = null) }
        }

        @Test
        fun `forwards a non-null editionId to the repository`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()
            val editionId = 42
            val updatedBook = mockk<Book>()

            every { book.status } returns BookStatus.None

            coEvery {
                booksRepository.markBookAsWantToRead(book = book, editionId = editionId)
            } returns updatedBook

            // ----- Act -----
            useCase(book = book, editionId = editionId)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.markBookAsWantToRead(book = book, editionId = editionId) }
        }

        @Test
        fun `forwards null editionId to the repository when omitted`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()
            val updatedBook = mockk<Book>()

            every { book.status } returns BookStatus.None

            coEvery {
                booksRepository.markBookAsWantToRead(book = book, editionId = null)
            } returns updatedBook

            // ----- Act -----
            useCase(book = book)

            // ----- Assert -----
            coVerify(exactly = 1) { booksRepository.markBookAsWantToRead(book = book, editionId = null) }
        }

        @Test
        fun `returns failure when markBookAsWantToRead throws`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()
            val expectedError = RuntimeException("network error")

            every { book.status } returns BookStatus.None

            coEvery {
                booksRepository.markBookAsWantToRead(book = book, editionId = null)
            } throws expectedError

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns NoChange and does not touch repository when book is already in target status`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()

            every { book.status } returns BookStatus.WantToRead

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.NoChange
            coVerify(exactly = 0) { booksRepository.markBookAsWantToRead(any(), any()) }
        }
    }
}
