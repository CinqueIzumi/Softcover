package nl.rhaydus.softcover.core.identity.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateApiKeyUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: UpdateApiKeyUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = UpdateApiKeyUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(key = "my-api-key")

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given key`() = runTest {
            // ----- Arrange -----
            val key = "abc-123"

            // ----- Act -----
            useCase(key = key)

            // ----- Assert -----
            coVerify {
                settingsRepository.updateApiKey(key = key)
            }
        }

        @Test
        fun `returns success when key is empty string`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(key = "")

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val key = "bad-key"
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.updateApiKey(key = key)
            } throws expectedError

            // ----- Act -----
            val result = useCase(key = key)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
