package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class UndoContinueSeriesDismissalUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: UndoContinueSeriesDismissalUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk(relaxed = true)
        useCase = UndoContinueSeriesDismissalUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository undoContinueSeriesDismissal with correct seriesId`() = runTest {
            // ----- Arrange -----
            val seriesId = 55

            // ----- Act -----
            useCase(seriesId = seriesId)

            // ----- Assert -----
            coVerify {
                exploreRepository.undoContinueSeriesDismissal(seriesId = seriesId)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("db error")

            coEvery {
                exploreRepository.undoContinueSeriesDismissal(any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(seriesId = 1)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
