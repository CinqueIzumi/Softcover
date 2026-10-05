package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissPlanTodayUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: DismissPlanTodayUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase =
            DismissPlanTodayUseCase(
                settingsRepository = settingsRepository,
            )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `records the dismissal with the date formatted as an ISO string`() = runTest {
            // ----- Arrange -----
            val today = LocalDate(
                2025,
                5,
                13,
            )

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
    }
}
