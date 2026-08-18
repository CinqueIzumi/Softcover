package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

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
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(mode = ThemeMode.DARK)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given mode`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(mode = ThemeMode.LIGHT)

            // ----- Assert -----
            coVerify {
                settingsRepository.setThemeMode(mode = ThemeMode.LIGHT)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setThemeMode(mode = ThemeMode.SYSTEM)
            } throws expectedError

            // ----- Act -----
            val result = useCase(mode = ThemeMode.SYSTEM)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
