package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetEnabledListIdsUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetEnabledListIdsUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetEnabledListIdsUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(ids = setOf(10, 20, 30))

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given ids`() = runTest {
            // ----- Arrange -----
            val ids = setOf(10, 20, 30)

            // ----- Act -----
            useCase(ids = ids)

            // ----- Assert -----
            coVerify {
                settingsRepository.setEnabledListIds(ids = ids)
            }
        }

        @Test
        fun `delegates to the repository with an empty set`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(ids = emptySet())

            // ----- Assert -----
            coVerify {
                settingsRepository.setEnabledListIds(ids = emptySet())
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setEnabledListIds(ids = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(ids = setOf(5))

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
