package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetLibrarySortUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetLibrarySortUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetLibrarySortUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository setLibrarySortForTab and returns success`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(
                tabId = "reading",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setLibrarySortForTab(
                    tabId = "reading",
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.ASCENDING,
                )
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setLibrarySortForTab(
                    tabId = any(),
                    mode = any(),
                    direction = any(),
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                tabId = "reading",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
