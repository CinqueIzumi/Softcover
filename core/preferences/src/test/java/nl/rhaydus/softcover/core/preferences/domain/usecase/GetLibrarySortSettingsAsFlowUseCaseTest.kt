package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetLibrarySortSettingsAsFlowUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetLibrarySortSettingsAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetLibrarySortSettingsAsFlowUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns settingsRepository librarySortSettingsByTab as-is`() = runTest {
            // ----- Arrange -----
            val expected = mapOf(
                "reading" to LibrarySortSettings(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.ASCENDING,
                ),
            )

            every {
                settingsRepository.librarySortSettingsByTab
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
                settingsRepository.librarySortSettingsByTab
            } returns flowOf(emptyMap())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyMap()
                awaitComplete()
            }
        }
    }
}
