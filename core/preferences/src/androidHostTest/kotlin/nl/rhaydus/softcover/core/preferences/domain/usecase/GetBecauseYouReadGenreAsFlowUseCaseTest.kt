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

class GetBecauseYouReadGenreAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetBecauseYouReadGenreAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase =
            GetBecauseYouReadGenreAsFlowUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns settingsRepository becauseYouReadGenre as-is when a genre is set`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.becauseYouReadGenre
            } returns flowOf("Fantasy")

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe "Fantasy"
                awaitComplete()
            }
        }

        @Test
        fun `returns null when no override is set`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.becauseYouReadGenre
            } returns flowOf(null)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }
    }
}
