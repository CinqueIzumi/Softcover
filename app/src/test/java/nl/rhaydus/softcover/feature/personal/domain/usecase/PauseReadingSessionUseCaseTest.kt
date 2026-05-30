package nl.rhaydus.softcover.feature.personal.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PauseReadingSessionUseCaseTest {

    private lateinit var repository: ReadingSessionRepository
    private lateinit var useCase: PauseReadingSessionUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = PauseReadingSessionUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository pause with given id`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            useCase(id = 42L)

            // ----- Assert -----
            coVerify(exactly = 1) { repository.pause(id = 42L) }
        }
    }
}
