package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetLibraryGridLayoutUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetLibraryGridLayoutUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetLibraryGridLayoutUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(newLayout = LibraryGridLayout.GRID_TWO_COLUMNS)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given layout`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(newLayout = LibraryGridLayout.GRID_THREE_COLUMNS)

            // ----- Assert -----
            coVerify {
                settingsRepository.setLibraryGridLayout(layout = LibraryGridLayout.GRID_THREE_COLUMNS)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setLibraryGridLayout(layout = LibraryGridLayout.LIST_COMPACT)
            } throws expectedError

            // ----- Act -----
            val result = useCase(newLayout = LibraryGridLayout.LIST_COMPACT)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `delegates correctly for each layout variant`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(newLayout = LibraryGridLayout.LIST_LARGE)

            // ----- Assert -----
            coVerify {
                settingsRepository.setLibraryGridLayout(layout = LibraryGridLayout.LIST_LARGE)
            }
        }
    }
}
