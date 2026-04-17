package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetBottomBarStyleUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetBottomBarStyleUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetBottomBarStyleUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(newStyle = BottomBarStyle.FLOATING)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given style`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(newStyle = BottomBarStyle.DOCKED)

            // ----- Assert -----
            coVerify {
                settingsRepository.setBottomBarStyle(style = BottomBarStyle.DOCKED)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setBottomBarStyle(style = BottomBarStyle.FLOATING)
            } throws expectedError

            // ----- Act -----
            val result = useCase(newStyle = BottomBarStyle.FLOATING)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
