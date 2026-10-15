package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetThemeModeUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetThemeModeUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetThemeModeUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `dim writes the mode and switches dynamic colour off`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(mode = ThemeMode.DIM)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.setThemeMode(mode = ThemeMode.DIM)
                settingsRepository.setDynamicColorEnabled(enabled = false)
            }
        }

        @Test
        fun `every other mode writes the mode and leaves dynamic colour untouched`() = runTest {
            ThemeMode.entries.filter { it != ThemeMode.DIM }.forEach { mode ->
                // ----- Arrange -----
                // (repository is relaxed)

                // ----- Act -----
                val result = useCase(mode = mode)

                // ----- Assert -----
                result.isSuccess shouldBe true
                coVerify { settingsRepository.setThemeMode(mode = mode) }
            }
            coVerify(exactly = 0) {
                settingsRepository.setDynamicColorEnabled(enabled = any())
            }
        }

        @Test
        fun `returns failure and leaves dynamic colour untouched when writing dim throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setThemeMode(mode = ThemeMode.DIM)
            } throws expectedError

            // ----- Act -----
            val result = useCase(mode = ThemeMode.DIM)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) {
                settingsRepository.setDynamicColorEnabled(enabled = any())
            }
        }
    }
}
