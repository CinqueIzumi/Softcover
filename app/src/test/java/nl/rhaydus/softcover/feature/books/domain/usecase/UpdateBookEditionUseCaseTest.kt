package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateBookEditionUseCaseTest {

    private lateinit var repository: BooksRepository
    private lateinit var useCase: UpdateBookEditionUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = UpdateBookEditionUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `updates edition then caches the returned book`() = runTest {
            // ----- Arrange -----
            val userBook = mockk<UserBook>()
            val newEditionId = 99
            val updatedBook = mockk<Book>()

            coEvery {
                repository.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            } returns updatedBook

            coJustRun {
                repository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(
                userBook = userBook,
                newEditionId = newEditionId,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                repository.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            }
            coVerify(exactly = 1) { repository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `returns failure when updateBookEdition throws`() = runTest {
            // ----- Arrange -----
            val userBook = mockk<UserBook>()
            val newEditionId = 99
            val expectedError = RuntimeException("network error")

            coEvery {
                repository.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                userBook = userBook,
                newEditionId = newEditionId,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { repository.cacheBook(book = any()) }
        }

        @Test
        fun `returns failure when cacheBook throws`() = runTest {
            // ----- Arrange -----
            val userBook = mockk<UserBook>()
            val newEditionId = 99
            val updatedBook = mockk<Book>()
            val expectedError = RuntimeException("cache error")

            coEvery {
                repository.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            } returns updatedBook

            coEvery {
                repository.cacheBook(book = updatedBook)
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                userBook = userBook,
                newEditionId = newEditionId,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
