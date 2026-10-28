package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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
        useCase = UpdateBookProgressUseCase(
            repository = repository,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `updates progress and returns success`() = runTest {
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
        }

        @Test
        fun `forwards actionAt to the repository`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val newPage = 150
            val actionAt = "2026-07-21T21:00:00Z"
            val updatedBook = mockk<Book>()

            coEvery {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                    actionAt = actionAt,
                )
            } returns updatedBook

            // ----- Act -----
            val result = useCase(
                book = inputBook,
                newPage = newPage,
                actionAt = actionAt,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                repository.updateBookProgress(
                    book = inputBook,
                    newPage = newPage,
                    actionAt = actionAt,
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
        }
    }
}
