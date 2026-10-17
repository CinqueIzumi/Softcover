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
        fun `stores the given key and returns success`() = runTest {
            // ----- Arrange -----
            val key = "abc-123"

            // ----- Act -----
            val result = useCase(key = key)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.updateApiKey(key = key)
            }
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
