package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetThemeConfigurationUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetThemeConfigurationUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetThemeConfigurationUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the theme configuration flow from the repository`() = runTest {
            // ----- Arrange -----
            val configuration = ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED)

            every {
                settingsRepository.getThemeConfig()
            } returns flowOf(configuration)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe configuration
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple ThemeConfiguration values when the repository emits them`() = runTest {
            // ----- Arrange -----
            val first = ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING)
            val second = ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED)

            every {
                settingsRepository.getThemeConfig()
            } returns flowOf(
                first,
                second,
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe first
                awaitItem() shouldBe second
                awaitComplete()
            }
        }
    }
}
