package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
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

class MastheadCollectorTest {
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
        fun `swaps to a different masthead and disables refresh once isRefreshing turns on`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(isRefreshing = false)
                val job = launch {
                    MastheadCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val idleMasthead = stateFlow.value.masthead
                stateFlow.value.mastheadRefresh.enabled shouldBe true

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isRefreshing = true)

                // ----- Assert -----
                stateFlow.value.masthead shouldNotBe idleMasthead
                stateFlow.value.mastheadRefresh.enabled shouldBe false
                job.cancel()
            }

        @Test
        fun `restores the idle masthead and re-enables refresh when isRefreshing flips back off`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(isRefreshing = false)
                val job = launch {
                    MastheadCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val idleMasthead = stateFlow.value.masthead
                stateFlow.value = stateFlow.value.copy(isRefreshing = true)

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isRefreshing = false)

                // ----- Assert -----
                stateFlow.value.masthead shouldBe idleMasthead
                stateFlow.value.mastheadRefresh.enabled shouldBe true
                job.cancel()
            }
    }
}
