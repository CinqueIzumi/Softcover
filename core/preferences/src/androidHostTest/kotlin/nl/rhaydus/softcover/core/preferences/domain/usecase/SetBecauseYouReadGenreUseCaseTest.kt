package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetBecauseYouReadGenreUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetBecauseYouReadGenreUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase =
            SetBecauseYouReadGenreUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository setBecauseYouReadGenre and returns success for a genre`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(genre = "Fantasy")

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setBecauseYouReadGenre(genre = "Fantasy")
            }
        }

        @Test
        fun `delegates to repository setBecauseYouReadGenre with null to clear the override`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(genre = null)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setBecauseYouReadGenre(genre = null)
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setBecauseYouReadGenre(genre = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(genre = "Fantasy")

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
