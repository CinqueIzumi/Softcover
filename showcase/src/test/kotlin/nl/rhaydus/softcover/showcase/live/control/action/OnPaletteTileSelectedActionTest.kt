package nl.rhaydus.softcover.showcase.live.control.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.mapper.controlLiveState
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnPaletteTileSelectedActionTest {
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
        fun `selects only the tapped palette tile`() = runTest {
            // ----- Arrange -----
            val tapped = stateFlow.value.paletteTiles.last().palette
            val action = OnPaletteTileSelectedAction(palette = tapped)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val selected = stateFlow.value.paletteTiles.filter { it.selected }.map { it.palette }
            selected shouldBe listOf(tapped)
        }

        @Test
        fun `selects nothing when the palette has no tile`() = runTest {
            // ----- Arrange -----
            val offered = stateFlow.value.paletteTiles.map { it.palette }
            val missing = SpinePalette.entries.first { it !in offered }
            val action = OnPaletteTileSelectedAction(palette = missing)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.paletteTiles.none { it.selected } shouldBe true
        }
    }
}
