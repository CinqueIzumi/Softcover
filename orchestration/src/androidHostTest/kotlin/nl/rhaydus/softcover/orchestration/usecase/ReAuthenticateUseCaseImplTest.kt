package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileStatsUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReAuthenticateUseCaseImplTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var resetUserDataUseCase: ResetUserDataUseCase
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var refreshUserProfileStatsUseCase: RefreshUserProfileStatsUseCase
    private lateinit var useCase: ReAuthenticateUseCaseImpl

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        resetUserDataUseCase = mockk(relaxed = true)
        refreshLibraryUseCase = mockk(relaxed = true)
        refreshUserProfileStatsUseCase = mockk(relaxed = true)
        useCase = ReAuthenticateUseCaseImpl(
            settingsRepository = settingsRepository,
            resetUserDataUseCase = resetUserDataUseCase,
            refreshLibraryUseCase = refreshLibraryUseCase,
            refreshUserProfileStatsUseCase = refreshUserProfileStatsUseCase,
        )

        coEvery {
            resetUserDataUseCase()
        } returns Result.success(Unit)
        coEvery {
            refreshLibraryUseCase()
        } returns Result.success(Unit)
        coEvery {
            refreshUserProfileStatsUseCase()
        } returns Result.success(Unit)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `same account logging in again keeps local data, stores the key and the id, and refreshes the library`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 42

            // ----- Act -----
            val result = useCase(apiKey = "valid-token")

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { resetUserDataUseCase() }
            coVerify(exactly = 1) { settingsRepository.updateApiKey(key = "valid-token") }
            coVerify { settingsRepository.updateUserId(id = 42) }
            coVerify { refreshLibraryUseCase() }
        }

        @Test
        fun `a different account wipes the previous data, restores the key and stores the new id`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99

            // ----- Act -----
            val result = useCase(apiKey = "new-token")

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify { resetUserDataUseCase() }
            coVerify(exactly = 2) { settingsRepository.updateApiKey(key = "new-token") }
            coVerify { settingsRepository.updateUserId(id = 99) }
        }

        @Test
        fun `first login does not wipe data and stores the backend id`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(-1)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99

            // ----- Act -----
            useCase(apiKey = "first-token")

            // ----- Assert -----
            coVerify(exactly = 0) { resetUserDataUseCase() }
            coVerify { settingsRepository.updateUserId(id = 99) }
        }

        @Test
        fun `an invalid token fails without wiping data or storing a user id`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } throws RuntimeException("invalid token")

            // ----- Act -----
            val result = useCase(apiKey = "bad-token")

            // ----- Assert -----
            result.isFailure shouldBe true
            coVerify(exactly = 0) { resetUserDataUseCase() }
            coVerify(exactly = 0) { settingsRepository.updateUserId(id = any()) }
        }

        @Test
        fun `a failed wipe for a different account fails without storing the new user id`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99
            coEvery {
                resetUserDataUseCase()
            } returns Result.failure(RuntimeException("wipe failed"))

            // ----- Act -----
            val result = useCase(apiKey = "new-token")

            // ----- Assert -----
            result.isFailure shouldBe true
            coVerify(exactly = 0) { settingsRepository.updateUserId(id = any()) }
        }

        @Test
        fun `strips Bearer prefix and surrounding whitespace from api key`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(-1)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99

            // ----- Act -----
            useCase(apiKey = "Bearer  abc-123 ")

            // ----- Assert -----
            coVerify { settingsRepository.updateApiKey(key = "abc-123") }
        }
    }
}
