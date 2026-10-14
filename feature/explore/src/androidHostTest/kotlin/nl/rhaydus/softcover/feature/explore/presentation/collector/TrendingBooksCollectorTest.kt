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
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class TrendingBooksCollectorTest {
    private lateinit var observeTrendingBooksUseCase: ObserveTrendingBooksUseCase
    private lateinit var observeTrendingFetchedAtUseCase: ObserveTrendingFetchedAtUseCase
    private lateinit var refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var trendingFlow: MutableStateFlow<List<Book>>
    private lateinit var fetchedAtFlow: MutableStateFlow<Instant?>

    @BeforeEach
    fun setUp() {
        trendingFlow = MutableStateFlow(emptyList())
        fetchedAtFlow = MutableStateFlow(null)
        observeTrendingBooksUseCase = mockk()
        observeTrendingFetchedAtUseCase = mockk()
        refreshTrendingBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeTrendingBooksUseCase()
        } returns trendingFlow

        every {
            observeTrendingFetchedAtUseCase()
        } returns fetchedAtFlow

        coEvery {
            refreshTrendingBooksUseCase(any())
        } returns Result.success(Unit)

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeTrendingBooksUseCase
            } returns observeTrendingBooksUseCase

            every {
                mock.observeTrendingFetchedAtUseCase
            } returns observeTrendingFetchedAtUseCase

            every {
                mock.refreshTrendingBooksUseCase
            } returns refreshTrendingBooksUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `follows the observed flow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val first = mockk<Book>()
            val second = mockk<Book>()
            val collector = TrendingBooksCollector()
            fetchedAtFlow.value = Instant.fromEpochSeconds(1)
            trendingFlow.value = listOf(first)
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            trendingFlow.value = listOf(first, second)

            // ----- Assert -----
            stateFlow.value.trendingBooks shouldBe listOf(first, second)
        }

        @Test
        fun `is loading while never fetched and a refresh is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val pending = CompletableDeferred<Result<Unit>>()
            coEvery {
                refreshTrendingBooksUseCase(any())
            } coAnswers { pending.await() }
            val collector = TrendingBooksCollector()

            // ----- Act -----
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.loadingTrendingBooks shouldBe true
        }

        @Test
        fun `stops loading once a fetched-at exists even while the refresh is in flight`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val pending = CompletableDeferred<Result<Unit>>()
                coEvery {
                    refreshTrendingBooksUseCase(any())
                } coAnswers { pending.await() }
                val collector = TrendingBooksCollector()
                backgroundScope.launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                fetchedAtFlow.value = Instant.fromEpochSeconds(1)

                // ----- Assert -----
                stateFlow.value.loadingTrendingBooks shouldBe false
            }

        @Test
        fun `is not loading when fetched with an empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val pending = CompletableDeferred<Result<Unit>>()
            coEvery {
                refreshTrendingBooksUseCase(any())
            } coAnswers { pending.await() }
            fetchedAtFlow.value = Instant.fromEpochSeconds(1)
            trendingFlow.value = emptyList()
            val collector = TrendingBooksCollector()

            // ----- Act -----
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.loadingTrendingBooks shouldBe false
        }

        @Test
        fun `stops loading when the refresh fails and nothing was ever fetched`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshTrendingBooksUseCase(any())
            } returns Result.failure(IllegalStateException("offline"))
            val collector = TrendingBooksCollector()

            // ----- Act -----
            backgroundScope.launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.loadingTrendingBooks shouldBe false
        }
    }
}
