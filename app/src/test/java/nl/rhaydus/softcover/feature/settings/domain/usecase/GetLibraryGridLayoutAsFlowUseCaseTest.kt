package nl.rhaydus.softcover.feature.settings.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetLibraryGridLayoutAsFlowUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetLibraryGridLayoutAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetLibraryGridLayoutAsFlowUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns the libraryGridLayout flow from the repository`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.libraryGridLayout
            } returns flowOf(LibraryGridLayout.GRID_TWO_COLUMNS)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple layout values when the repository emits them`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.libraryGridLayout
            } returns flowOf(LibraryGridLayout.GRID_TWO_COLUMNS, LibraryGridLayout.GRID_THREE_COLUMNS)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
                awaitItem() shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
                awaitComplete()
            }
        }

        @Test
        fun `returns each possible layout value`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.libraryGridLayout
            } returns flowOf(
                LibraryGridLayout.LIST_COMPACT,
                LibraryGridLayout.LIST_LARGE,
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe LibraryGridLayout.LIST_COMPACT
                awaitItem() shouldBe LibraryGridLayout.LIST_LARGE
                awaitComplete()
            }
        }
    }
}
