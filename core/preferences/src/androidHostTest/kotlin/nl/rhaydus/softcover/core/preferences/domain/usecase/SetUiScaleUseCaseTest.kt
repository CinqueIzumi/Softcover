package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetUiScaleUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetUiScaleUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase =
            SetUiScaleUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository setUiScale and returns success`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(scale = UiScale.PERCENT_150)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setUiScale(scale = UiScale.PERCENT_150)
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setUiScale(scale = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(scale = UiScale.PERCENT_150)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
