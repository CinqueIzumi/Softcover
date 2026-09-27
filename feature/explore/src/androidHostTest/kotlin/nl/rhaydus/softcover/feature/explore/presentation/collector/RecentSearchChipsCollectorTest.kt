package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentSearchChipsCollectorTest {
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
        dependencies = mockk<ExploreDependencies>(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets recentSearchChips and recentSearchQueryByChipKey from previousSearchQueries`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = RecentSearchChipsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                stateFlow.value = ExploreScreenUiState(previousSearchQueries = listOf("kotlin", "android"))

                // ----- Assert -----
                stateFlow.value.recentSearchChips.map { it.key } shouldBe listOf("recent:kotlin", "recent:android")
                stateFlow.value.recentSearchQueryByChipKey shouldBe mapOf(
                    "recent:kotlin" to "kotlin",
                    "recent:android" to "android",
                )
                job.cancel()
            }

        @Test
        fun `updates to empty chips and map when previousSearchQueries becomes empty`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(previousSearchQueries = listOf("kotlin"))
                val collector = RecentSearchChipsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(previousSearchQueries = emptyList())

                // ----- Assert -----
                stateFlow.value.recentSearchChips shouldBe persistentListOf()
                stateFlow.value.recentSearchQueryByChipKey shouldBe emptyMap()
                job.cancel()
            }

        @Test
        fun `preserves other state fields when updating`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "existing query",
                isLoading = true,
            )
            val collector = RecentSearchChipsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(previousSearchQueries = listOf("dune"))

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "existing query"
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `unrelated state change that leaves previousSearchQueries equal does not retrigger recompute`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(previousSearchQueries = listOf("kotlin"))
                val collector = RecentSearchChipsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }
                val chipsAfterFirstEmit = stateFlow.value.recentSearchChips
                val mapAfterFirstEmit = stateFlow.value.recentSearchQueryByChipKey

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isLoading = true)

                // ----- Assert -----
                (stateFlow.value.recentSearchChips === chipsAfterFirstEmit) shouldBe true
                (stateFlow.value.recentSearchQueryByChipKey === mapAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
