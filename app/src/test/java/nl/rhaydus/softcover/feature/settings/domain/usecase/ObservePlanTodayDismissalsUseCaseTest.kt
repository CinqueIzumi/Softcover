package nl.rhaydus.softcover.feature.settings.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObservePlanTodayDismissalsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: ObservePlanTodayDismissalsUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = ObservePlanTodayDismissalsUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns the dismissedPlanTodayByBook flow from the repository`() = runTest {
            // ----- Arrange -----
            val expected = mapOf(1 to "2025-05-13", 7 to "2025-05-12")

            every {
                settingsRepository.dismissedPlanTodayByBook
            } returns flowOf(expected)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe expected
                awaitComplete()
            }
        }

        @Test
        fun `emits empty map when no dismissals stored`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.dismissedPlanTodayByBook
            } returns flowOf(emptyMap())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyMap()
                awaitComplete()
            }
        }
    }
}
