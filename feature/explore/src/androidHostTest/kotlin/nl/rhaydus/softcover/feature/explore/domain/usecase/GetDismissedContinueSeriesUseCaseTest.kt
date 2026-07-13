package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetDismissedContinueSeriesUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: GetDismissedContinueSeriesUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk()
        useCase = GetDismissedContinueSeriesUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `is wired to repository dismissedContinueSeries`() = runTest {
            // ----- Arrange -----
            val expected = listOf(
                DismissedSeries(
                    seriesId = 10,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                ),
            )

            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(expected)

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe expected
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when repository emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                exploreRepository.dismissedContinueSeries
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
