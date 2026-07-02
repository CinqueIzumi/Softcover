package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetUiScaleAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetUiScaleAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase =
            GetUiScaleAsFlowUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns settingsRepository uiScale as-is`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.uiScale
            } returns flowOf(UiScale.PERCENT_150)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe UiScale.PERCENT_150
                awaitComplete()
            }
        }

        @Test
        fun `returns settingsRepository uiScale default when repository emits default`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.uiScale
            } returns flowOf(UiScale.DEFAULT)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe UiScale.PERCENT_100
                awaitComplete()
            }
        }
    }
}
