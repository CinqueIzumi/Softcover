package nl.rhaydus.softcover.showcase.live.selection.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.live.selection.event.SelectionLiveEvent
import nl.rhaydus.softcover.showcase.live.selection.mapper.selectionLiveState
import nl.rhaydus.softcover.showcase.live.selection.screenmodel.SelectionLiveDependencies
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnCoverToggledActionTest {
    private lateinit var dependencies: SelectionLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<SelectionLiveUiState>
    private lateinit var scope: ActionScope<SelectionLiveUiState, SelectionLiveEvent, SelectionLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(selectionLiveState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SelectionLiveLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `flips only the tapped cover`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value.covers
            val tapped = before[1]
            val action = OnCoverToggledAction(key = tapped.key)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val after = stateFlow.value.covers
            after[1].selected shouldBe tapped.selected.not()
            after.filterIndexed { index, _ -> index != 1 } shouldBe before.filterIndexed { index, _ -> index != 1 }
        }

        @Test
        fun `tapping twice restores the original selection`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value.covers
            val action = OnCoverToggledAction(key = before.first().key)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.covers shouldBe before
        }

        @Test
        fun `an unknown key changes no cover`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value.covers
            val action = OnCoverToggledAction(key = "missing")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.covers shouldBe before
        }
    }
}
