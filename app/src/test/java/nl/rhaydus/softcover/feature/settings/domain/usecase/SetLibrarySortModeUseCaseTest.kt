package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetLibrarySortModeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetLibrarySortModeUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetLibrarySortModeUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository and returns success`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            val result = useCase(
                tabId = "reading",
                mode = LibrarySortMode.AUTHOR,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.setLibrarySortModeForTab(
                    tabId = "reading",
                    mode = LibrarySortMode.AUTHOR,
                )
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("storage failure")

            coEvery {
                settingsRepository.setLibrarySortModeForTab(any(), any())
            } throws error

            // ----- Act -----
            val result = useCase(
                tabId = "reading",
                mode = LibrarySortMode.TITLE,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
