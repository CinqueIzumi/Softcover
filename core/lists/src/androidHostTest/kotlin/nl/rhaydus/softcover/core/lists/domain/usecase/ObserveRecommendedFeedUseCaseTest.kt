package nl.rhaydus.softcover.core.lists.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveRecommendedFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshRecommendedBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ObserveRecommendedFeedUseCaseTest {
    private lateinit var booksFlow: MutableStateFlow<List<Book>>
    private lateinit var fetchedAtFlow: MutableStateFlow<Instant?>
    private lateinit var refreshResult: CompletableDeferred<Result<Unit>>
    private lateinit var useCase: ObserveRecommendedFeedUseCase

    @BeforeEach
    fun setUp() {
        booksFlow = MutableStateFlow(emptyList())
        fetchedAtFlow = MutableStateFlow(null)
        refreshResult = CompletableDeferred()
        val getRecommendedBooks = mockk<GetRecommendedBooksUseCase>()
        val observeRecommendedFetchedAt = mockk<ObserveRecommendedFetchedAtUseCase>()
        val refreshRecommendedBooks = mockk<RefreshRecommendedBooksUseCase>()

        every {
            getRecommendedBooks(limit = any())
        } returns booksFlow

        every {
            observeRecommendedFetchedAt()
        } returns fetchedAtFlow

        coEvery {
            refreshRecommendedBooks(maxAge = any())
        } coAnswers {
            refreshResult.await()
        }

        useCase = ObserveRecommendedFeedUseCase(
            getRecommendedBooks = getRecommendedBooks,
            observeRecommendedFetchedAt = observeRecommendedFetchedAt,
            refreshRecommendedBooks = refreshRecommendedBooks,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `is loading while nothing is cached and the refresh is in flight`() = runTest {
            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `stops loading when the refresh succeeds with nothing cached`() = runTest {
            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true

                refreshResult.complete(Result.success(Unit))

                awaitItem().isLoading shouldBe false
            }
        }

        @Test
        fun `stops loading when the refresh fails with nothing cached`() = runTest {
            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true

                refreshResult.complete(Result.failure(IllegalStateException("offline")))

                awaitItem().isLoading shouldBe false
            }
        }

        @Test
        fun `is not loading when a fetch time is cached even while the refresh is in flight`() = runTest {
            // ----- Arrange -----
            fetchedAtFlow.value = Instant.fromEpochSeconds(epochSeconds = 1_000)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits the books in order and updates when they change`() = runTest {
            // ----- Arrange -----
            val first = mockk<Book>()
            val second = mockk<Book>()
            val third = mockk<Book>()
            fetchedAtFlow.value = Instant.fromEpochSeconds(epochSeconds = 1_000)
            booksFlow.value = listOf(first, second)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().books shouldBe listOf(first, second)

                booksFlow.value = listOf(third, first, second)

                awaitItem().books shouldBe listOf(third, first, second)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
