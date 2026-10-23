package nl.rhaydus.softcover.showcase.live.dialog.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.mapper.datePicker
import nl.rhaydus.softcover.showcase.live.dialog.mapper.datePickerTrigger
import nl.rhaydus.softcover.showcase.live.dialog.mapper.dialogLiveTopBar
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveDependencies
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDatePickerDismissedActionTest {
    private lateinit var dependencies: DialogLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<DialogLiveUiState>
    private lateinit var eventChannel: Channel<DialogLiveEvent>
    private lateinit var scope: ActionScope<DialogLiveUiState, DialogLiveEvent, DialogLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(
            DialogLiveUiState(
                topBar = dialogLiveTopBar(),
                trigger = datePickerTrigger(pickedDate = null),
                datePicker = datePicker(pickedDate = null),
            ),
        )
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(DialogLiveLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `closes the picker`() = runTest {
            // ----- Arrange -----
            val action = OnDatePickerDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.datePicker shouldBe null
        }
    }
}
