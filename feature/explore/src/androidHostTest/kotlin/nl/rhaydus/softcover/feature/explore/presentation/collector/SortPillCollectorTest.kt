package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toSortPillButton
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SortPillCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sortPill reflects the initial sortMode`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(sortMode = ExploreSortMode.POPULARITY)
            val collector = SortPillCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.sortPill shouldBe ExploreSortMode.POPULARITY.toSortPillButton()
            job.cancel()
        }

        @Test
        fun `sortPill follows sortMode changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SortPillCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(sortMode = ExploreSortMode.POPULARITY)

            // ----- Assert -----
            stateFlow.value.sortPill shouldBe ExploreSortMode.POPULARITY.toSortPillButton()
            job.cancel()
        }

        @Test
        fun `sortPill returns to the first mode when sortMode switches back`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SortPillCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(sortMode = ExploreSortMode.POPULARITY)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(sortMode = ExploreSortMode.RELEVANCE)

            // ----- Assert -----
            stateFlow.value.sortPill shouldBe ExploreSortMode.RELEVANCE.toSortPillButton()
            job.cancel()
        }
    }
}
