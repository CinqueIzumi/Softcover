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

class InitializeUserBooksUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: InitializeUserBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        getUserIdUseCase = mockk()
        useCase = InitializeUserBooksUseCase(
            booksRepository = booksRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success and initializes books with resolved user id`() = runTest {
            // ----- Arrange -----
            val userId = 123

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coJustRun {
                booksRepository.initializeBooks(userId = userId)
            }

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                booksRepository.initializeBooks(userId = userId)
            }
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
            coVerify(exactly = 0) {
                booksRepository.initializeBooks(userId = any())
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val userId = 42
            val expectedError = RuntimeException("db error")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                booksRepository.initializeBooks(userId = userId)
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
