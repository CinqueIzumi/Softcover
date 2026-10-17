package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
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

class OnShelvesSheetExpandedChangeActionTest {
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
        fun `requests the sheet without building its model or sending an event when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState()

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShelvesSheetRequested shouldBe true
            stateFlow.value.shelvesSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `leaves a closing session for the collector to replace when expanded is true`() = runTest {
            // ----- Arrange -----
            val closing = openShelvesSession(
                closing = true,
                pending = ManageShelves,
            )
            stateFlow.value = LibraryUiState(shelvesSheet = closing)

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShelvesSheetRequested shouldBe true
            stateFlow.value.shelvesSheet shouldBe closing
        }

        @Test
        fun `closes the session and withdraws the request when expanded is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                shelvesSheet = openShelvesSession(),
                isShelvesSheetRequested = true,
            )

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheet shouldBe SheetSession.Closed
            stateFlow.value.isShelvesSheetRequested shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
