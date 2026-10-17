package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetColorPaletteUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetColorPaletteUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetColorPaletteUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `writes the palette and switches dynamic colour off`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(palette = ColorPalette.INK)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.setColorPalette(palette = ColorPalette.INK)
                settingsRepository.setDynamicColorEnabled(enabled = false)
            }
        }

        @Test
        fun `returns failure and leaves dynamic colour untouched when writing the palette throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setColorPalette(palette = ColorPalette.FOXED)
            } throws expectedError

            // ----- Act -----
            val result = useCase(palette = ColorPalette.FOXED)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) {
                settingsRepository.setDynamicColorEnabled(enabled = any())
            }
        }
    }
}
