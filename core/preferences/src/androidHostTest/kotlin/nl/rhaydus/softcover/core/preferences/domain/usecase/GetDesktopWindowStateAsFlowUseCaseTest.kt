package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.WindowPlacement
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetDesktopWindowStateAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetDesktopWindowStateAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase =
            GetDesktopWindowStateAsFlowUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the desktopWindowState flow from the repository`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState()

            every {
                settingsRepository.desktopWindowState
            } returns flowOf(state)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe state
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple state values when the repository emits them`() = runTest {
            // ----- Arrange -----
            val first = DesktopWindowState(
                width = 1280,
                height = 800,
                x = null,
                y = null,
                placement = WindowPlacement.FLOATING,
            )
            val second = DesktopWindowState(
                width = 1920,
                height = 1080,
                x = 100,
                y = 200,
                placement = WindowPlacement.MAXIMIZED,
            )

            every {
                settingsRepository.desktopWindowState
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

        @Test
        fun `returns a fullscreen state value`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState(placement = WindowPlacement.FULLSCREEN)

            every {
                settingsRepository.desktopWindowState
            } returns flowOf(state)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe state
                awaitComplete()
            }
        }
    }
}
