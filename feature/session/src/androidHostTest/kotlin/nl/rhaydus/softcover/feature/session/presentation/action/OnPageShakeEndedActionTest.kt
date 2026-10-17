package nl.rhaydus.softcover.feature.session.presentation.action

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

class OnPageShakeEndedActionTest {
    private lateinit var dependencies: FocusModeDependencies
    private lateinit var stateFlow: MutableStateFlow<FocusModeUiState>
    private lateinit var scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(FocusModeUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalFocusModeVariables(isFirstFocusEvent = false)),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears the rejected flag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = FocusModeUiState(pageDraftRejected = true)

            // ----- Act -----
            OnPageShakeEndedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pageDraftRejected shouldBe false
        }
    }
}
