package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.openShelvesSession
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.ManageShelves
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShelvesSheetCloseRequestedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var eventChannel: Channel<LibraryEvent>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `withdraws the sheet request, cancels a pending pick and sends no event`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                shelvesSheet = openShelvesSession(),
                isShelvesSheetRequested = true,
                isShelvesSheetPickPending = true,
            )

            // ----- Act -----
            OnShelvesSheetCloseRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShelvesSheetRequested shouldBe false
            stateFlow.value.isShelvesSheetPickPending shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `leaves the session for the collector to hide and keeps a manage-shelves request`() = runTest {
            // ----- Arrange -----
            val open = openShelvesSession(pending = ManageShelves)
            stateFlow.value = LibraryUiState(
                shelvesSheet = open,
                isShelvesSheetRequested = true,
            )

            // ----- Act -----
            OnShelvesSheetCloseRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheet shouldBe open
            stateFlow.value.shelvesSheet.dismissed() shouldBe ManageShelves
        }
    }
}
