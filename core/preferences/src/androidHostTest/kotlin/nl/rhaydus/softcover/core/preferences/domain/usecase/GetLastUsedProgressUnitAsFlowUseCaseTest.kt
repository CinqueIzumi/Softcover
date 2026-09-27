package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetLastUsedProgressUnitAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetLastUsedProgressUnitAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase =
            GetLastUsedProgressUnitAsFlowUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns settingsRepository lastUsedProgressUnit as-is when PAGE`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.lastUsedProgressUnit
            } returns flowOf(ProgressUnit.PAGE)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe ProgressUnit.PAGE
                awaitComplete()
            }
        }

        @Test
        fun `returns settingsRepository lastUsedProgressUnit as-is when TIME`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.lastUsedProgressUnit
            } returns flowOf(ProgressUnit.TIME)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe ProgressUnit.TIME
                awaitComplete()
            }
        }

        @Test
        fun `returns settingsRepository lastUsedProgressUnit as-is when PERCENTAGE`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.lastUsedProgressUnit
            } returns flowOf(ProgressUnit.PERCENTAGE)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe ProgressUnit.PERCENTAGE
                awaitComplete()
            }
        }
    }
}
