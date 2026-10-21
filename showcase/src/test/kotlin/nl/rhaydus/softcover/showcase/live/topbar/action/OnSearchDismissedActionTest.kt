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

class OnSearchDismissedActionTest {
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
        fun `drops focus but stays active while a query is typed`() = runTest {
            // ----- Arrange -----
            OnSearchQueryChangedAction(query = "gilead").execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnSearchActivatedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )
            val action = OnSearchDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.search.focused shouldBe false
            stateFlow.value.search.active shouldBe true
            stateFlow.value.search.query shouldBe "gilead"
        }

        @Test
        fun `deactivates the search when the query is empty`() = runTest {
            // ----- Arrange -----
            OnSearchActivatedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )
            val action = OnSearchDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.search.focused shouldBe false
            stateFlow.value.search.active shouldBe false
        }
    }
}
