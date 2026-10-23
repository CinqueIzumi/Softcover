package nl.rhaydus.softcover.showcase.live.topbar.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.mapper.topbarLiveState
import nl.rhaydus.softcover.showcase.live.topbar.mapper.topbarSidebar
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarMode
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBarSelectedActionTest {
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
        fun `switches the mode and rebuilds the sidebar around it`() = runTest {
            // ----- Arrange -----
            val action = OnBarSelectedAction(mode = TopbarMode.Editorial)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.mode shouldBe TopbarMode.Editorial
            stateFlow.value.sidebar shouldBe topbarSidebar(mode = TopbarMode.Editorial)
        }

        @Test
        fun `leaves the bars themselves untouched`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value
            val action = OnBarSelectedAction(mode = TopbarMode.Search)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.topBar shouldBe before.topBar
            stateFlow.value.search shouldBe before.search
        }
    }
}
