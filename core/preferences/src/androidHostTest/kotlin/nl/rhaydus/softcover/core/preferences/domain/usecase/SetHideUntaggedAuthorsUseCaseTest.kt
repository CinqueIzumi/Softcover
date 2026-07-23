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

class SetHideUntaggedAuthorsUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetHideUntaggedAuthorsUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase =
            SetHideUntaggedAuthorsUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository setHideUntaggedAuthors and returns success when enabled`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(enabled = true)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setHideUntaggedAuthors(enabled = true)
            }
        }

        @Test
        fun `delegates to repository setHideUntaggedAuthors and returns success when disabled`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(enabled = false)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setHideUntaggedAuthors(enabled = false)
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setHideUntaggedAuthors(enabled = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(enabled = true)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
