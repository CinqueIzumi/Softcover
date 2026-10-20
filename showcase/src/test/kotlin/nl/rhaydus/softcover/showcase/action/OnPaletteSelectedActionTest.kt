package nl.rhaydus.softcover.showcase.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.mapper.showcasePaletteChips
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseDependencies
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnPaletteSelectedActionTest {
    private lateinit var stateFlow: MutableStateFlow<ShowcaseUiState>
    private lateinit var scope: ActionScope<ShowcaseUiState, ShowcaseEvent, ShowcaseLocalVariables>
    private val dependencies = mockk<ShowcaseDependencies>(relaxed = true)

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ShowcaseUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ShowcaseLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `selection lands in state and marks only its chip`() = runTest {
            // ----- Arrange -----
            val target = SpinePalette.entries.last()
            val action = OnPaletteSelectedAction(palette = target)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedPalette shouldBe target
            stateFlow.value.paletteChips shouldBe showcasePaletteChips(selected = target)
            stateFlow.value.paletteChips.chips.count { it.selected } shouldBe 1
        }
    }
}
