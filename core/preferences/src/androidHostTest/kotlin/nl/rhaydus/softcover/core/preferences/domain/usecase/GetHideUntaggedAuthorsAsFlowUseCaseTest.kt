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

class GetHideUntaggedAuthorsAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetHideUntaggedAuthorsAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase =
            GetHideUntaggedAuthorsAsFlowUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns settingsRepository hideUntaggedAuthors as-is when enabled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.hideUntaggedAuthors
            } returns flowOf(true)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `returns settingsRepository hideUntaggedAuthors as-is when disabled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.hideUntaggedAuthors
            } returns flowOf(false)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe false
                awaitComplete()
            }
        }
    }
}
