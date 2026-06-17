package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetEnabledStatusCodesUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetEnabledStatusCodesUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetEnabledStatusCodesUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(codes = setOf(1, 3, 5))

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to the repository with the given codes`() = runTest {
            // ----- Arrange -----
            val codes = setOf(1, 3, 5)

            // ----- Act -----
            useCase(codes = codes)

            // ----- Assert -----
            coVerify {
                settingsRepository.setEnabledStatusCodes(codes = codes)
            }
        }

        @Test
        fun `delegates to the repository with an empty set`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(codes = emptySet())

            // ----- Assert -----
            coVerify {
                settingsRepository.setEnabledStatusCodes(codes = emptySet())
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setEnabledStatusCodes(codes = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(codes = setOf(1))

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
