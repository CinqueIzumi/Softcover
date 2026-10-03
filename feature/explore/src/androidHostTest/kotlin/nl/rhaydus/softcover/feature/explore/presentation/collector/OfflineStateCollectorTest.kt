package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OfflineStateCollectorTest {
    private val emptyState = EmptyStateUiModel(
        title = "Offline",
        body = "Reconnect to continue",
    )

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
        val offlineCopy = mockk<OfflineCopy>()
        coEvery {
            offlineCopy.emptyState()
        } returns emptyState
        dependencies = mockk(relaxed = true)
        every {
            dependencies.offlineCopy
        } returns offlineCopy
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets the offline empty state from the offline copy`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = OfflineStateCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.offlineEmptyState.shouldNotBeNull() shouldBe emptyState
        }
    }
}
