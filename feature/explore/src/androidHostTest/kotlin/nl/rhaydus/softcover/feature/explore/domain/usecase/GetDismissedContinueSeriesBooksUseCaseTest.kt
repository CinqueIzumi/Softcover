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
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetDismissedContinueSeriesBooksUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: GetDismissedContinueSeriesBooksUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk()
        useCase = GetDismissedContinueSeriesBooksUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `is wired to repository dismissedContinueSeriesBooks`() = runTest {
            // ----- Arrange -----
            val expected = listOf(
                DismissedSeriesBook(
                    bookId = 1,
                    title = "Dune",
                    coverUrl = "cover.jpg",
                    authorText = "Frank Herbert",
                    seriesName = "Dune Saga",
                    seriesId = 7,
                    seriesPosition = 2.0,
                ),
            )

            every {
                exploreRepository.dismissedContinueSeriesBooks
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
                exploreRepository.dismissedContinueSeriesBooks
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
