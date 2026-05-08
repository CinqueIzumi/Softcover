package nl.rhaydus.softcover.feature.explore.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContinueSeriesBooksCollectorTest {

    private lateinit var getContinueSeriesBooksUseCase: GetContinueSeriesBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        getContinueSeriesBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getContinueSeriesBooksUseCase
            } returns getContinueSeriesBooksUseCase
        }
    }

    // ----- OnLaunch -----

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates continueSeriesBooks and clears loading flag on success`() = runTest {
            // ----- Arrange -----
            val book: Book = mockk()

            every {
                getContinueSeriesBooksUseCase()
            } returns flowOf(listOf(book))

            val collector = ContinueSeriesBooksCollector()

            // ----- Act -----
            collector.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.continueSeriesBooks shouldBe listOf(book)
            stateFlow.value.loadingContinueSeriesBooks shouldBe false
        }

        @Test
        fun `clears loading flag and leaves continueSeriesBooks at default on failure`() = runTest {
            // ----- Arrange -----
            every {
                getContinueSeriesBooksUseCase()
            } returns flow { throw RuntimeException("use case error") }

            val collector = ContinueSeriesBooksCollector()

            // ----- Act -----
            collector.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.loadingContinueSeriesBooks shouldBe false
            stateFlow.value.continueSeriesBooks shouldBe emptyList()
        }

        @Test
        fun `preserves other state fields when updating continueSeriesBooks on success`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "fantasy",
                trendingBooks = emptyList(),
                loadingTrendingBooks = false,
            )
            val book: Book = mockk()

            every {
                getContinueSeriesBooksUseCase()
            } returns flowOf(listOf(book))

            val collector = ContinueSeriesBooksCollector()

            // ----- Act -----
            collector.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "fantasy"
            stateFlow.value.loadingTrendingBooks shouldBe false
        }

        @Test
        fun `preserves other state fields when clearing loading on failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "sci-fi",
                loadingTrendingBooks = false,
            )

            every {
                getContinueSeriesBooksUseCase()
            } returns flow { throw RuntimeException("error") }

            val collector = ContinueSeriesBooksCollector()

            // ----- Act -----
            collector.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "sci-fi"
            stateFlow.value.loadingTrendingBooks shouldBe false
        }
    }
}
