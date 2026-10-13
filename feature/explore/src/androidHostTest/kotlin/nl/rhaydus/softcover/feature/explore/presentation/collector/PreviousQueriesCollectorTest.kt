package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
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
        fun `updates to the latest list when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PreviousQueriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            queriesFlow.emit(listOf("first"))
            queriesFlow.emit(listOf("second", "third"))

            // ----- Assert -----
            stateFlow.value.previousSearchQueries shouldBe listOf("second", "third")
            job.cancel()
        }
    }
}
