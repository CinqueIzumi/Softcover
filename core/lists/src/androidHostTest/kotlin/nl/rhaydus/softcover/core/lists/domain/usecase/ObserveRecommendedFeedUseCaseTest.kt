package nl.rhaydus.softcover.core.lists.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveSystemVibeFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.core.lists.domain.model.RecommendedFeed
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ObserveRecommendedFeedUseCaseTest {
    private lateinit var recommendationBooks: MutableStateFlow<List<Book>>
    private lateinit var topPickBooks: MutableStateFlow<List<Book>>
    private lateinit var recommendationFetchedAt: MutableStateFlow<Instant?>
    private lateinit var topPickFetchedAt: MutableStateFlow<Instant?>
    private lateinit var recommendationRefresh: CompletableDeferred<Result<Unit>>
    private lateinit var topPickRefresh: CompletableDeferred<Result<Unit>>
    private lateinit var getRecommendedBooks: GetRecommendedBooksUseCase
    private lateinit var useCase: ObserveRecommendedFeedUseCase

    private val fetchedAt = Instant.fromEpochSeconds(epochSeconds = 1_000)

    @BeforeEach
    fun setUp() {
        recommendationBooks = MutableStateFlow(emptyList())
        topPickBooks = MutableStateFlow(emptyList())
        recommendationFetchedAt = MutableStateFlow(null)
        topPickFetchedAt = MutableStateFlow(null)
        recommendationRefresh = CompletableDeferred()
        topPickRefresh = CompletableDeferred()
        getRecommendedBooks = mockk<GetRecommendedBooksUseCase>()
        val observeSystemVibeFetchedAt = mockk<ObserveSystemVibeFetchedAtUseCase>()
        val refreshSystemVibeBooks = mockk<RefreshSystemVibeBooksUseCase>()

        every {
            getRecommendedBooks(
                vibe = SystemVibe.Recommendations,
                limit = any(),
            )
        } returns recommendationBooks

        every {
            getRecommendedBooks(
                vibe = SystemVibe.TopPicks,
                limit = any(),
            )
        } returns topPickBooks

        every {
            observeSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
        } returns recommendationFetchedAt

        every {
            observeSystemVibeFetchedAt(vibe = SystemVibe.TopPicks)
        } returns topPickFetchedAt

        coEvery {
            refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = any(),
            )
        } coAnswers {
            recommendationRefresh.await()
        }

        coEvery {
            refreshSystemVibeBooks(
                vibe = SystemVibe.TopPicks,
                maxAge = any(),
            )
        } coAnswers {
            topPickRefresh.await()
        }

        useCase = ObserveRecommendedFeedUseCase(
            getRecommendedBooks = getRecommendedBooks,
            observeSystemVibeFetchedAt = observeSystemVibeFetchedAt,
            refreshSystemVibeBooks = refreshSystemVibeBooks,
        )
    }

    @Nested
    inner class Loading {
        @Test
        fun `is loading while nothing is cached and the refreshes are in flight`() = runTest {
            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `stops loading once both refreshes succeed with nothing cached`() = runTest {
            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true

                recommendationRefresh.complete(Result.success(Unit))
                topPickRefresh.complete(Result.success(Unit))

                awaitItem() shouldBe RecommendedFeed(
                    books = emptyList(),
                    isLoading = false,
                    source = SystemVibe.Recommendations,
                )
            }
        }

        @Test
        fun `is never loading for vibes cached at collection start even while their refreshes are in flight`() = runTest {
            // ----- Arrange -----
            recommendationFetchedAt.value = fetchedAt
            topPickFetchedAt.value = fetchedAt

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem() shouldBe RecommendedFeed(
                    books = emptyList(),
                    isLoading = false,
                    source = SystemVibe.Recommendations,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `never emits an empty settled feed while Top Picks gets its first books during its refresh`() = runTest {
            // ----- Arrange -----
            val topPick = mockk<Book>()

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true

                recommendationRefresh.complete(Result.success(Unit))
                runCurrent()
                topPickFetchedAt.value = fetchedAt
                runCurrent()
                topPickBooks.value = listOf(topPick)

                awaitItem() shouldBe RecommendedFeed(
                    books = listOf(topPick),
                    isLoading = false,
                    source = SystemVibe.TopPicks,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `shows the books stored by a refresh by querying them again once it settles`() = runTest {
            // ----- Arrange -----
            val stored = mockk<Book>()
            var queries = 0

            every {
                getRecommendedBooks(
                    vibe = SystemVibe.Recommendations,
                    limit = any(),
                )
            } answers {
                queries++
                val books = if (queries == 1) emptyList() else listOf(stored)
                flow { emit(books) }
            }

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true

                recommendationRefresh.complete(Result.success(Unit))

                awaitItem() shouldBe RecommendedFeed(
                    books = listOf(stored),
                    isLoading = false,
                    source = SystemVibe.Recommendations,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class Source {
        @Test
        fun `shows the recommendations even when Top Picks also has books`() = runTest {
            // ----- Arrange -----
            val recommended = mockk<Book>()
            val topPick = mockk<Book>()
            recommendationFetchedAt.value = fetchedAt
            topPickFetchedAt.value = fetchedAt
            recommendationBooks.value = listOf(recommended)
            topPickBooks.value = listOf(topPick)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem() shouldBe RecommendedFeed(
                    books = listOf(recommended),
                    isLoading = false,
                    source = SystemVibe.Recommendations,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `falls back to Top Picks when the recommendations are settled and empty`() = runTest {
            // ----- Arrange -----
            val topPick = mockk<Book>()
            recommendationFetchedAt.value = fetchedAt
            topPickFetchedAt.value = fetchedAt
            topPickBooks.value = listOf(topPick)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem() shouldBe RecommendedFeed(
                    books = listOf(topPick),
                    isLoading = false,
                    source = SystemVibe.TopPicks,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `withholds Top Picks while the recommendations are still pending`() = runTest {
            // ----- Arrange -----
            val topPick = mockk<Book>()
            topPickFetchedAt.value = fetchedAt
            topPickBooks.value = listOf(topPick)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem() shouldBe RecommendedFeed(
                    books = emptyList(),
                    isLoading = true,
                    source = SystemVibe.Recommendations,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `shows Top Picks once the pending recommendations refresh fails with nothing cached`() = runTest {
            // ----- Arrange -----
            val topPick = mockk<Book>()
            topPickFetchedAt.value = fetchedAt
            topPickBooks.value = listOf(topPick)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().isLoading shouldBe true

                recommendationRefresh.complete(Result.failure(IllegalStateException("offline")))

                awaitItem() shouldBe RecommendedFeed(
                    books = listOf(topPick),
                    isLoading = false,
                    source = SystemVibe.TopPicks,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `switches from Top Picks to the recommendations when they later arrive`() = runTest {
            // ----- Arrange -----
            val recommended = mockk<Book>()
            val topPick = mockk<Book>()
            recommendationFetchedAt.value = fetchedAt
            topPickFetchedAt.value = fetchedAt
            topPickBooks.value = listOf(topPick)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().source shouldBe SystemVibe.TopPicks

                recommendationBooks.value = listOf(recommended)

                awaitItem() shouldBe RecommendedFeed(
                    books = listOf(recommended),
                    isLoading = false,
                    source = SystemVibe.Recommendations,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class Books {
        @Test
        fun `emits the books in order and updates when they change`() = runTest {
            // ----- Arrange -----
            val first = mockk<Book>()
            val second = mockk<Book>()
            val third = mockk<Book>()
            recommendationFetchedAt.value = fetchedAt
            topPickFetchedAt.value = fetchedAt
            recommendationBooks.value = listOf(first, second)

            // ----- Act & Assert -----
            useCase(limit = 25).test {
                awaitItem().books shouldBe listOf(first, second)

                recommendationBooks.value = listOf(third, first, second)

                awaitItem().books shouldBe listOf(third, first, second)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
