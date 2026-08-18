package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

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
        fun `returns success when repository calls succeed`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(palette = ColorPalette.SEA)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given palette`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(palette = ColorPalette.INK)

            // ----- Assert -----
            coVerify {
                settingsRepository.setColorPalette(palette = ColorPalette.INK)
            }
        }

        @Test
        fun `also switches dynamic colour off`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(palette = ColorPalette.VELLUM)

            // ----- Assert -----
            coVerify {
                settingsRepository.setDynamicColorEnabled(enabled = false)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setColorPalette(palette = ColorPalette.FOXED)
            } throws expectedError

            // ----- Act -----
            val result = useCase(palette = ColorPalette.FOXED)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
