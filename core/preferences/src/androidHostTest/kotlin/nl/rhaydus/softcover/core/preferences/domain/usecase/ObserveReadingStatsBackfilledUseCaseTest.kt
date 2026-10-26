package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveReadingStatsBackfilledUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: ObserveReadingStatsBackfilledUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = ObserveReadingStatsBackfilledUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `emits true when demographics and roles are both backfilled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.authorDemographicsBackfilled
            } returns flowOf(true)
            every {
                settingsRepository.authorRolesBackfilled
            } returns flowOf(true)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `emits false when only demographics are backfilled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.authorDemographicsBackfilled
            } returns flowOf(true)
            every {
                settingsRepository.authorRolesBackfilled
            } returns flowOf(false)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `emits false when only roles are backfilled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.authorDemographicsBackfilled
            } returns flowOf(false)
            every {
                settingsRepository.authorRolesBackfilled
            } returns flowOf(true)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe false
        }
    }
}
