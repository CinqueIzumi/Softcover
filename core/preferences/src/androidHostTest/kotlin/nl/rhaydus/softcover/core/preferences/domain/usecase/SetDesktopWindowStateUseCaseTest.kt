package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.WindowPlacement
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetDesktopWindowStateUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetDesktopWindowStateUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase =
            SetDesktopWindowStateUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(state = DesktopWindowState())

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given state`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState(
                width = 1440,
                height = 900,
                x = 50,
                y = 80,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            useCase(state = state)

            // ----- Assert -----
            coVerify {
                settingsRepository.setDesktopWindowState(state = state)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState()
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setDesktopWindowState(state = state)
            } throws expectedError

            // ----- Act -----
            val result = useCase(state = state)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `delegates correctly for a maximized state`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState(placement = WindowPlacement.MAXIMIZED)

            // ----- Act -----
            useCase(state = state)

            // ----- Assert -----
            coVerify {
                settingsRepository.setDesktopWindowState(state = state)
            }
        }
    }
}
