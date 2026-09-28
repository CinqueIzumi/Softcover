package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
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

    private val idleMasthead = PageMastheadUiModel(
        eyebrow = "Discover",
        title = "Explore",
        subtitle = "Trending books, the next in your series, and anything you search for.",
    )

    private val refreshingMasthead = idleMasthead.copy(eyebrow = "Finding fresh picks…")

    private val idleRefresh = MastheadRefreshUiModel(
        label = "Refresh",
        enabled = true,
    )

    private val refreshingRefresh = MastheadRefreshUiModel(
        label = "Refreshing…",
        enabled = false,
    )

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
        fun `sets idle masthead and refresh label when not refreshing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(isRefreshing = false)
            val collector = MastheadCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.masthead shouldBe idleMasthead
            stateFlow.value.mastheadRefresh shouldBe idleRefresh
            job.cancel()
        }

        @Test
        fun `sets refreshing masthead and disabled refresh label when refreshing`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(isRefreshing = true)
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.masthead shouldBe refreshingMasthead
                stateFlow.value.mastheadRefresh shouldBe refreshingRefresh
                job.cancel()
            }

        @Test
        fun `re-derives masthead and refresh label when isRefreshing flips back to idle`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(isRefreshing = true)
                val collector = MastheadCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isRefreshing = false)

                // ----- Assert -----
                stateFlow.value.masthead shouldBe idleMasthead
                stateFlow.value.mastheadRefresh shouldBe idleRefresh
                job.cancel()
            }

        @Test
        fun `default ExploreScreenUiState matches the collector's idle output`() {
            // ----- Arrange / Act -----
            val defaultState = ExploreScreenUiState()

            // ----- Assert -----
            defaultState.masthead shouldBe idleMasthead
            defaultState.mastheadRefresh shouldBe idleRefresh
        }
    }
}
