package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshUserBooksUseCaseTest {

    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: RefreshUserBooksUseCase

    @BeforeEach
    fun setUp() {
        getUserIdUseCase = mockk()
        booksRepository = mockk()
        useCase = RefreshUserBooksUseCase(
            getUserIdUseCase = getUserIdUseCase,
            booksRepository = booksRepository,
        )
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success and refreshes books with resolved user id`() = runTest {
            // ----- Arrange -----
            val userId = 55

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coJustRun {
                booksRepository.refreshUserBooks(userId = userId)
            }

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.refreshUserBooks(userId = userId) }
        }

        @Test
        fun `returns failure when getUserIdUseCase returns failure`() = runTest {
            // ----- Arrange -----
            val expectedError = IllegalStateException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { booksRepository.refreshUserBooks(userId = any()) }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val userId = 55
            val expectedError = RuntimeException("network error")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                booksRepository.refreshUserBooks(userId = userId)
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
