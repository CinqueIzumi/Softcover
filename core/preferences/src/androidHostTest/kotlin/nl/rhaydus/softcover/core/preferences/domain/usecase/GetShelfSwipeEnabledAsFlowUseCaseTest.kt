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
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetShelfSwipeEnabledAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetShelfSwipeEnabledAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase =
            GetShelfSwipeEnabledAsFlowUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns settingsRepository shelfSwipeEnabled as-is when enabled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.shelfSwipeEnabled
            } returns flowOf(true)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `returns settingsRepository shelfSwipeEnabled as-is when disabled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.shelfSwipeEnabled
            } returns flowOf(false)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe false
                awaitComplete()
            }
        }
    }
}
