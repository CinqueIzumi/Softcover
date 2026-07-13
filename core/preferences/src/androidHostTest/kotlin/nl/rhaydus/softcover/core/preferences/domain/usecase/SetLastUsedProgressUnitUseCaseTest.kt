package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetLastUsedProgressUnitUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetLastUsedProgressUnitUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase =
            SetLastUsedProgressUnitUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository setLastUsedProgressUnit and returns success for PAGE`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(unit = ProgressUnit.PAGE)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setLastUsedProgressUnit(unit = ProgressUnit.PAGE)
            }
        }

        @Test
        fun `delegates to repository setLastUsedProgressUnit and returns success for TIME`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(unit = ProgressUnit.TIME)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setLastUsedProgressUnit(unit = ProgressUnit.TIME)
            }
        }

        @Test
        fun `delegates to repository setLastUsedProgressUnit and returns success for PERCENTAGE`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(unit = ProgressUnit.PERCENTAGE)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                settingsRepository.setLastUsedProgressUnit(unit = ProgressUnit.PERCENTAGE)
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setLastUsedProgressUnit(unit = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(unit = ProgressUnit.PAGE)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
