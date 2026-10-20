package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveRecommendedFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshRecommendedBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetRecommendedBooksUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class RecommendedBooksCollectorTest {
    private lateinit var getRecommendedBooksUseCase: GetRecommendedBooksUseCase
    private lateinit var observeRecommendedFetchedAtUseCase: ObserveRecommendedFetchedAtUseCase
    private lateinit var refreshRecommendedBooksUseCase: RefreshRecommendedBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var booksFlow: MutableStateFlow<List<Book>>
    private lateinit var fetchedAtFlow: MutableStateFlow<Instant?>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableStateFlow(emptyList())
        fetchedAtFlow = MutableStateFlow(null)
        getRecommendedBooksUseCase = mockk()
        observeRecommendedFetchedAtUseCase = mockk()
        refreshRecommendedBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getRecommendedBooksUseCase()
        } returns booksFlow

        every {
            observeRecommendedFetchedAtUseCase()
        } returns fetchedAtFlow

        coEvery {
            refreshRecommendedBooksUseCase(maxAge = any())
        } returns Result.success(Unit)

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getRecommendedBooksUseCase
            } returns getRecommendedBooksUseCase

            every {
                mock.observeRecommendedFetchedAtUseCase
            } returns observeRecommendedFetchedAtUseCase

            every {
                mock.refreshRecommendedBooksUseCase
            } returns refreshRecommendedBooksUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the books in the order the use case emits them`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val first = mockk<Book>()
            val second = mockk<Book>()
            val collector = RecommendedBooksCollector()
            fetchedAtFlow.value = Instant.fromEpochSeconds(1)
            booksFlow.value = listOf(first)
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.value = listOf(second, first)

            // ----- Assert -----
            stateFlow.value.recommendedBooks shouldBe listOf(second, first)
        }

        @Test
        fun `is loading while never fetched and a refresh is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val pending = CompletableDeferred<Result<Unit>>()
            coEvery {
                refreshRecommendedBooksUseCase(maxAge = any())
            } coAnswers { pending.await() }
            val collector = RecommendedBooksCollector()

            // ----- Act -----
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.loadingRecommendedBooks shouldBe true
        }

        @Test
        fun `stops loading once the refresh settles`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val pending = CompletableDeferred<Result<Unit>>()
            coEvery {
                refreshRecommendedBooksUseCase(maxAge = any())
            } coAnswers { pending.await() }
            val collector = RecommendedBooksCollector()
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            pending.complete(Result.success(Unit))

            // ----- Assert -----
            stateFlow.value.loadingRecommendedBooks shouldBe false
        }

        @Test
        fun `stops loading when the refresh fails and nothing was ever fetched`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                coEvery {
                    refreshRecommendedBooksUseCase(maxAge = any())
                } returns Result.failure(IllegalStateException("offline"))
                val collector = RecommendedBooksCollector()

                // ----- Act -----
                backgroundScope.launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.loadingRecommendedBooks shouldBe false
            }

        @Test
        fun `is not loading once a fetched-at exists even while the refresh is in flight`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val pending = CompletableDeferred<Result<Unit>>()
                coEvery {
                    refreshRecommendedBooksUseCase(maxAge = any())
                } coAnswers { pending.await() }
                val collector = RecommendedBooksCollector()
                backgroundScope.launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                fetchedAtFlow.value = Instant.fromEpochSeconds(1)

                // ----- Assert -----
                stateFlow.value.loadingRecommendedBooks shouldBe false
            }
    }
}
