package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DismissPlanTodayUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: DismissPlanTodayUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = DismissPlanTodayUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository with bookId and ISO date string and returns success`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2025, 5, 13)

            // ----- Act -----
            val result = useCase(
                bookId = 42,
                today = today,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.setPlanTodayDismissed(
                    bookId = 42,
                    isoDate = "2025-05-13",
                )
            }
        }

        @Test
        fun `wraps repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("write error")

            coEvery {
                settingsRepository.setPlanTodayDismissed(any(), any())
            } throws error

            // ----- Act -----
            val result = useCase(
                bookId = 1,
                today = LocalDate.of(2025, 1, 1),
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
