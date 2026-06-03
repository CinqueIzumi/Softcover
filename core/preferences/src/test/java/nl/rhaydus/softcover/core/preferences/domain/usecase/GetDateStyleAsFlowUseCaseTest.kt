package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetDateStyleAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetDateStyleAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetDateStyleAsFlowUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the dateStyle flow from the repository`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.dateStyle
            } returns flowOf(DateStyle.MONTH_DAY_YEAR)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe DateStyle.MONTH_DAY_YEAR
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple date style values when the repository emits them`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.dateStyle
            } returns flowOf(
                DateStyle.DAY_MONTH_YEAR,
                DateStyle.YEAR_MONTH_DAY,
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe DateStyle.DAY_MONTH_YEAR
                awaitItem() shouldBe DateStyle.YEAR_MONTH_DAY
                awaitComplete()
            }
        }
    }
}
