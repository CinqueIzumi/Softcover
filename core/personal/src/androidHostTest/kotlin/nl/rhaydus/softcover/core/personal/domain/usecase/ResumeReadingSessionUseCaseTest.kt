package nl.rhaydus.softcover.core.personal.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

class ResumeReadingSessionUseCaseTest {
    private lateinit var repository: ReadingSessionRepository
    private lateinit var useCase: ResumeReadingSessionUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ResumeReadingSessionUseCase(repository = repository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository resume with given id`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            useCase(id = 7L)

            // ----- Assert -----
            coVerify(exactly = 1) { repository.resume(id = 7L) }
        }
    }
}
