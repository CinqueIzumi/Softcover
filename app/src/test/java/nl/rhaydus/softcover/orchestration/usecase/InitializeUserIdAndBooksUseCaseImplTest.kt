package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshLibraryUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InitializeUserIdAndBooksUseCaseImplTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var useCase: InitializeUserIdAndBooksUseCaseImpl

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        refreshLibraryUseCase = mockk()

        useCase = InitializeUserIdAndBooksUseCaseImpl(
            settingsRepository = settingsRepository,
            refreshLibraryUseCase = refreshLibraryUseCase,
        )
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success when all calls succeed`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `updateUserId is called before refreshLibraryUseCase`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerifyOrder {
                settingsRepository.updateUserId(id = userId)
                refreshLibraryUseCase()
            }
        }

        @Test
        fun `calls updateUserId with the userId returned from the backend`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                settingsRepository.updateUserId(id = userId)
            }
        }

        @Test
        fun `calls refreshLibraryUseCase after updateUserId`() = runTest {
            // ----- Arrange -----
            val userId = 7

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshLibraryUseCase()
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

        @Test
        fun `returns failure when refreshLibraryUseCase returns failure`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val expectedError = RuntimeException("refresh error")

            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns userId

            coEvery {
                refreshLibraryUseCase()
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
