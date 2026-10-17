package nl.rhaydus.softcover.feature.session.presentation.action

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnPageFocusGainedActionTest {
    private lateinit var dependencies: FocusModeDependencies
    private lateinit var stateFlow: MutableStateFlow<FocusModeUiState>
    private lateinit var scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(FocusModeUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalFocusModeVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class Execute {
        @Test
        fun `selects the whole draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = FocusModeUiState(pageDraft = TextFieldValue(text = "123"))

            // ----- Act -----
            OnPageFocusGainedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "123"
            stateFlow.value.pageDraft.selection shouldBe TextRange(
                start = 0,
                end = 3,
            )
        }
    }
}
