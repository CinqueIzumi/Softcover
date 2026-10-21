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

class OnSearchClearedActionTest {
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
        fun `empties the query and closes the search`() = runTest {
            // ----- Arrange -----
            OnSearchQueryChangedAction(query = "middlemarch").execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnSearchActivatedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )
            val action = OnSearchClearedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.search.query shouldBe ""
            stateFlow.value.search.active shouldBe false
            stateFlow.value.search.focused shouldBe false
        }
    }
}
