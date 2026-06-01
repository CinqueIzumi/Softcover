package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateBookProgressUseCaseTest {

    private lateinit var repository: BooksRepository
    private lateinit var useCase: UpdateBookProgressUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = UpdateBookProgressUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `updates progress then caches the returned book`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val newPage = 150
            val updatedBook = mockk<Book>()

            coEvery {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                )
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(
                book = inputBook,
                newPage = newPage,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                )
            }
            coVerify(exactly = 1) { repository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `passes the exact page number to the repository`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val newPage = 0
            val updatedBook = mockk<Book>()

            coEvery {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                )
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(
                book = inputBook,
                newPage = newPage,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = 0,
                )
            }
        }

        @Test
        fun `returns failure when updateBookProgress throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val newPage = 150
            val expectedError = RuntimeException("network error")

            coEvery {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                book = inputBook,
                newPage = newPage,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { repository.cacheBook(book = any()) }
        }

        @Test
        fun `returns failure when cacheBook throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val newPage = 150
            val updatedBook = mockk<Book>()
            val expectedError = RuntimeException("cache error")

            coEvery {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                )
            } returns updatedBook

            coEvery {
                repository.cacheBook(book = updatedBook)
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                book = inputBook,
                newPage = newPage,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
