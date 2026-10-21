package nl.rhaydus.softcover.showcase.live.control.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.mapper.controlLiveState
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRowToggledActionTest {
    private lateinit var dependencies: ControlLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<ControlLiveUiState>
    private lateinit var scope: ActionScope<ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(controlLiveState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ControlLiveLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets the checked value of only the matching row`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value.toggleRows
            val target = before.first()
            val action = OnRowToggledAction(
                key = target.key,
                checked = target.checked.not(),
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val after = stateFlow.value.toggleRows
            after.first().checked shouldBe target.checked.not()
            after.drop(1) shouldBe before.drop(1)
        }

        @Test
        fun `an unknown key changes no row`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value.toggleRows
            val action = OnRowToggledAction(
                key = "missing",
                checked = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.toggleRows shouldBe before
        }
    }
}
