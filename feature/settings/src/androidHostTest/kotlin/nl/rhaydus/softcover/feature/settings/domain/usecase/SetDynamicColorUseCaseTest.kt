package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetDynamicColorUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetDynamicColorUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetDynamicColorUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `persists the requested value and succeeds`() = runTest {
            // ----- Act -----
            val result = useCase(enabled = true)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.setDynamicColorEnabled(enabled = true)
            }
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setDynamicColorEnabled(enabled = false)
            } throws expectedError

            // ----- Act -----
            val result = useCase(enabled = false)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
