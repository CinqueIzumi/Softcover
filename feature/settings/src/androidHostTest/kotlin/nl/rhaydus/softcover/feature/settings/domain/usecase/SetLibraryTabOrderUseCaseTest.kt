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

class SetLibraryTabOrderUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SetLibraryTabOrderUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        useCase = SetLibraryTabOrderUseCase(settingsRepository = settingsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `persists the order as given and succeeds`() = runTest {
            // ----- Act -----
            val result = useCase(order = listOf("b", "a", "c"))

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.setLibraryTabOrder(order = listOf("b", "a", "c"))
            }
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.setLibraryTabOrder(order = emptyList())
            } throws expectedError

            // ----- Act -----
            val result = useCase(order = emptyList())

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
