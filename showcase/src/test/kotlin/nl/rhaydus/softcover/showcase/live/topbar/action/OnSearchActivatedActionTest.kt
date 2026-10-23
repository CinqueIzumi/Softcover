package nl.rhaydus.softcover.showcase.live.topbar.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.mapper.topbarLiveState
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSearchActivatedActionTest {
    private lateinit var dependencies: TopbarLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<TopbarLiveUiState>
    private lateinit var scope: ActionScope<TopbarLiveUiState, TopbarLiveEvent, TopbarLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(topbarLiveState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(TopbarLiveLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `activates and focuses the search without touching the query`() = runTest {
            // ----- Arrange -----
            OnSearchQueryChangedAction(query = "kindred").execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnSearchDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )
            val action = OnSearchActivatedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.search.active shouldBe true
            stateFlow.value.search.focused shouldBe true
            stateFlow.value.search.query shouldBe "kindred"
        }
    }
}
