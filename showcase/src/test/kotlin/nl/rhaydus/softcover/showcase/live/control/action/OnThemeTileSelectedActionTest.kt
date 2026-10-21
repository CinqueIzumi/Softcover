package nl.rhaydus.softcover.showcase.live.control.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.control.ThemeTilePainting
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.mapper.controlLiveState
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnThemeTileSelectedActionTest {
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
        fun `selects only the tapped theme tile`() = runTest {
            // ----- Arrange -----
            val action = OnThemeTileSelectedAction(painting = ThemeTilePainting.DIM)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val selected = stateFlow.value.themeTiles.filter { it.selected }.map { it.painting }
            selected shouldBe listOf(ThemeTilePainting.DIM)
        }

        @Test
        fun `moves the selection when another tile is tapped afterwards`() = runTest {
            // ----- Arrange -----
            OnThemeTileSelectedAction(painting = ThemeTilePainting.DIM).execute(
                dependencies = dependencies,
                scope = scope,
            )
            val action = OnThemeTileSelectedAction(painting = ThemeTilePainting.DARK)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val selected = stateFlow.value.themeTiles.filter { it.selected }.map { it.painting }
            selected shouldBe listOf(ThemeTilePainting.DARK)
        }
    }
}
