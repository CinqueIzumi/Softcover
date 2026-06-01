package nl.rhaydus.softcover.core.identity.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetUserIdAsFlowUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetUserIdAsFlowUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetUserIdAsFlowUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns the userId flow from the repository`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(42)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe 42
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple userId values when the repository emits them`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.getUserId()
            } returns flowOf(1, 2, 3)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe 1
                awaitItem() shouldBe 2
                awaitItem() shouldBe 3
                awaitComplete()
            }
        }
    }
}
