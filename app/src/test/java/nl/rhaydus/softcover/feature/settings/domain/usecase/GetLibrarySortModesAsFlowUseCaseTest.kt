package nl.rhaydus.softcover.feature.settings.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetLibrarySortModesAsFlowUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetLibrarySortModesAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetLibrarySortModesAsFlowUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns the librarySortModeByTab flow from the repository`() = runTest {
            // ----- Arrange -----
            val expected = mapOf("reading" to LibrarySortMode.TITLE)

            every {
                settingsRepository.librarySortModeByTab
            } returns flowOf(expected)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe expected
                awaitComplete()
            }
        }

        @Test
        fun `emits empty map when repository emits empty map`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.librarySortModeByTab
            } returns flowOf(emptyMap())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyMap()
                awaitComplete()
            }
        }
    }
}
