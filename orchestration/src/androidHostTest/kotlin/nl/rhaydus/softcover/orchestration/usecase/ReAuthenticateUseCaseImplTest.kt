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
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReAuthenticateUseCaseImplTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var resetUserDataUseCase: ResetUserDataUseCase
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase
    private lateinit var useCase: ReAuthenticateUseCaseImpl

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        resetUserDataUseCase = mockk(relaxed = true)
        refreshLibraryUseCase = mockk(relaxed = true)
        refreshUserProfileDataUseCase = mockk(relaxed = true)
        useCase = ReAuthenticateUseCaseImpl(
            settingsRepository = settingsRepository,
            resetUserDataUseCase = resetUserDataUseCase,
            refreshLibraryUseCase = refreshLibraryUseCase,
            refreshUserProfileDataUseCase = refreshUserProfileDataUseCase,
        )

        coEvery {
            resetUserDataUseCase()
        } returns Result.success(Unit)
        coEvery {
            refreshLibraryUseCase()
        } returns Result.success(Unit)
        coEvery {
            refreshUserProfileDataUseCase()
        } returns Result.success(Unit)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when same account logs in again`() = runTest {
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
        }

        @Test
        fun `does not call resetUserDataUseCase when account is the same`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 42

            // ----- Act -----
            useCase(apiKey = "valid-token")

            // ----- Assert -----
            coVerify(exactly = 0) { resetUserDataUseCase() }
        }

        @Test
        fun `calls updateApiKey once when account is the same`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 42

            // ----- Act -----
            useCase(apiKey = "valid-token")

            // ----- Assert -----
            coVerify(exactly = 1) { settingsRepository.updateApiKey(key = "valid-token") }
        }

        @Test
        fun `calls updateUserId with backend id when account is the same`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 42

            // ----- Act -----
            useCase(apiKey = "valid-token")

            // ----- Assert -----
            coVerify { settingsRepository.updateUserId(id = 42) }
        }

        @Test
        fun `calls refreshLibraryUseCase when account is the same`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 42

            // ----- Act -----
            useCase(apiKey = "valid-token")

            // ----- Assert -----
            coVerify { refreshLibraryUseCase() }
        }

        @Test
        fun `calls resetUserDataUseCase when a different account authenticates`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99

            // ----- Act -----
            useCase(apiKey = "new-token")

            // ----- Assert -----
            coVerify { resetUserDataUseCase() }
        }

        @Test
        fun `calls updateApiKey twice when a different account authenticates`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99

            // ----- Act -----
            useCase(apiKey = "new-token")

            // ----- Assert -----
            coVerify(exactly = 2) { settingsRepository.updateApiKey(key = "new-token") }
        }

        @Test
        fun `calls updateUserId with new backend id when a different account authenticates`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } returns 99

            // ----- Act -----
            useCase(apiKey = "new-token")

            // ----- Assert -----
            coVerify { settingsRepository.updateUserId(id = 99) }
        }

        @Test
        fun `does not call resetUserDataUseCase on first login`() = runTest {
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
        }

        @Test
        fun `calls updateUserId with backend id on first login`() = runTest {
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
            coVerify { settingsRepository.updateUserId(id = 99) }
        }

        @Test
        fun `returns failure when getUserIdFromBackend throws`() = runTest {
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
        }

        @Test
        fun `does not call resetUserDataUseCase when token is invalid`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } throws RuntimeException("invalid token")

            // ----- Act -----
            useCase(apiKey = "bad-token")

            // ----- Assert -----
            coVerify(exactly = 0) { resetUserDataUseCase() }
        }

        @Test
        fun `does not call updateUserId when token is invalid`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)
            coEvery {
                settingsRepository.getUserIdFromBackend()
            } throws RuntimeException("invalid token")

            // ----- Act -----
            useCase(apiKey = "bad-token")

            // ----- Assert -----
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
