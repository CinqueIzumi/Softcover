package nl.rhaydus.softcover.showcase.live.snackbar.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.actionSnackbarTrigger
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.plainSnackbarTrigger
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.sheetSnackbarTrigger
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.snackbarLiveTopBar
import nl.rhaydus.softcover.showcase.live.snackbar.mapper.snackbarSheet
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSnackbarSheetDismissedActionTest {
    private lateinit var dependencies: SnackbarLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<SnackbarLiveUiState>
    private lateinit var eventChannel: Channel<SnackbarLiveEvent>
    private lateinit var scope: ActionScope<SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(
            SnackbarLiveUiState(
                topBar = snackbarLiveTopBar(),
                plainTrigger = plainSnackbarTrigger(),
                actionTrigger = actionSnackbarTrigger(),
                sheetTrigger = sheetSnackbarTrigger(),
            ),
        )
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SnackbarLiveLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `closes an open sheet session`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(sheet = SheetSession.Open(model = snackbarSheet()))
            val action = OnSnackbarSheetDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sheet shouldBe SheetSession.Closed
        }
    }
}
