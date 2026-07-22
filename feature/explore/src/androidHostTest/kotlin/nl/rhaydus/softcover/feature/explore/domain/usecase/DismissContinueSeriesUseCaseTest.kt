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

class DismissContinueSeriesUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: DismissContinueSeriesUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk(relaxed = true)
        useCase = DismissContinueSeriesUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository dismissContinueSeries with correct seriesId`() = runTest {
            // ----- Arrange -----
            val seriesId = 99

            // ----- Act -----
            useCase(seriesId = seriesId)

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeries(seriesId = seriesId)
            }
        }

        @Test
        fun `forwards authorText and bookCount to the repository`() = runTest {
            // ----- Arrange -----
            val seriesId = 42
            val seriesName = "Foundation"
            val coverUrl = "https://example.com/foundation.jpg"
            val authorText = "Isaac Asimov"
            val bookCount = 7

            // ----- Act -----
            useCase(
                seriesId = seriesId,
                seriesName = seriesName,
                coverUrl = coverUrl,
                authorText = authorText,
                bookCount = bookCount,
            )

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeries(
                    seriesId = seriesId,
                    seriesName = seriesName,
                    coverUrl = coverUrl,
                    authorText = authorText,
                    bookCount = bookCount,
                )
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("db error")

            coEvery {
                exploreRepository.dismissContinueSeries(any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(seriesId = 1)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
