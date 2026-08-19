package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository

class RefreshRoadmapUseCaseTest {
    private lateinit var roadmapRepository: RoadmapRepository
    private lateinit var useCase: RefreshRoadmapUseCase

    @BeforeEach
    fun setUp() {
        roadmapRepository = mockk(relaxed = true)
        useCase = RefreshRoadmapUseCase(roadmapRepository = roadmapRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when repository call succeeds`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            val result = useCase(force = true)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                roadmapRepository.refreshRoadmap(force = any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(force = true)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `delegates to the repository with force set to true`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(force = true)

            // ----- Assert -----
            coVerify {
                roadmapRepository.refreshRoadmap(force = true)
            }
        }

        @Test
        fun `delegates to the repository with force set to false`() = runTest {
            // ----- Arrange -----
            // (repository is relaxed)

            // ----- Act -----
            useCase(force = false)

            // ----- Assert -----
            coVerify {
                roadmapRepository.refreshRoadmap(force = false)
            }
        }
    }
}
