package nl.rhaydus.softcover.feature.explore.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PreviousQueriesCollectorTest {

    private lateinit var getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var queriesFlow: MutableSharedFlow<List<String>>

    @BeforeEach
    fun setUp() {
        queriesFlow = MutableSharedFlow()
        getPreviousSearchQueriesUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getPreviousSearchQueriesUseCase()
        } returns queriesFlow

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getPreviousSearchQueriesUseCase
            } returns getPreviousSearchQueriesUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates previousSearchQueries when flow emits a list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PreviousQueriesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            queriesFlow.emit(listOf("kotlin", "android"))

            // ----- Assert -----
            stateFlow.value.previousSearchQueries shouldBe listOf("kotlin", "android")
            job.cancel()
        }

        @Test
        fun `updates previousSearchQueries to empty list when flow emits empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PreviousQueriesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            queriesFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.previousSearchQueries shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `updates to the latest list when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PreviousQueriesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            queriesFlow.emit(listOf("first"))
            queriesFlow.emit(listOf("second", "third"))

            // ----- Assert -----
            stateFlow.value.previousSearchQueries shouldBe listOf("second", "third")
            job.cancel()
        }

        @Test
        fun `does not change previousSearchQueries before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PreviousQueriesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.previousSearchQueries shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `retains the last emitted list after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PreviousQueriesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }
            queriesFlow.emit(listOf("dune", "foundation"))
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.previousSearchQueries shouldBe listOf("dune", "foundation")
        }

        @Test
        fun `preserves all other state fields when updating previousSearchQueries`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "existing query",
                isLoading = true,
                previousSearchQueries = emptyList(),
            )
            val collector = PreviousQueriesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            queriesFlow.emit(listOf("new query"))

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "existing query"
            stateFlow.value.isLoading shouldBe true
            stateFlow.value.previousSearchQueries shouldBe listOf("new query")
            job.cancel()
        }
    }
}
