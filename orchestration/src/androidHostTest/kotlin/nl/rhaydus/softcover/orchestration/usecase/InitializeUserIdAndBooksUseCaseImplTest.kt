package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
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
        fun `stores the backend user id, refreshes the library once and returns success`() = runTest {
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
            coVerify { settingsRepository.updateUserId(id = userId) }
            coVerify(exactly = 1) { refreshLibraryUseCase() }
        }

        @Test
        fun `marks the read-throughs backfilled after a successful refresh`() = runTest {
            // ----- Arrange -----
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 7
            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) { settingsRepository.markReadThroughsBackfilled() }
        }

        @Test
        fun `does not mark the read-throughs backfilled when the refresh fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 7
            coEvery {
                refreshLibraryUseCase()
            } returns Result.failure(RuntimeException("refresh error"))

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 0) { settingsRepository.markReadThroughsBackfilled() }
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
