package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesBookUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: DismissContinueSeriesBookUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk(relaxed = true)
        useCase = DismissContinueSeriesBookUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository dismissContinueSeriesBook with the same book, including seriesId and seriesPosition`() = runTest {
            // ----- Arrange -----
            val book = DismissedSeriesBook(
                bookId = 42,
                title = "Dune",
                coverUrl = "cover.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
                seriesId = 7,
                seriesPosition = 2.0,
            )

            // ----- Act -----
            useCase(book = book)

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeriesBook(book = book)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("db error")
            val book = DismissedSeriesBook(
                bookId = 1,
                title = null,
                coverUrl = null,
                authorText = null,
                seriesName = null,
                seriesId = null,
                seriesPosition = null,
            )

            coEvery {
                exploreRepository.dismissContinueSeriesBook(any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(book = book)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
