package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InitializeUserIdAndBooksUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: InitializeUserIdAndBooksUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        booksRepository = mockk(relaxed = true)
        useCase = InitializeUserIdAndBooksUseCase(
            settingsRepository = settingsRepository,
            booksRepository = booksRepository,
        )
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success when all repository calls succeed`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `calls refreshUserBooks with the userId returned from the backend`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                booksRepository.refreshUserBooks(userId = userId, scope = RefreshScope.All)
            }
        }

        @Test
        fun `calls updateUserId with the userId returned from the backend`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                settingsRepository.updateUserId(id = userId)
            }
        }

        @Test
        fun `returns failure when getUserIdFromBackend throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("backend error")

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when refreshUserBooks throws`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val expectedError = RuntimeException("books init error")

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                booksRepository.refreshUserBooks(userId = userId, scope = RefreshScope.All)
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when updateUserId throws`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val expectedError = RuntimeException("update userId error")

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                settingsRepository.updateUserId(id = userId)
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
