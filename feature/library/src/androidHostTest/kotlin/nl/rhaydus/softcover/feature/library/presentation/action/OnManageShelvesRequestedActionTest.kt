package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
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

class OnManageShelvesRequestedActionTest {
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
        fun `asks the open sheet to close and records the manage-shelves request without sending an event`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                shelvesSheet = openShelvesSession(),
                isShelvesSheetRequested = true,
            )

            // ----- Act -----
            OnManageShelvesRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheet.isClosing shouldBe true
            stateFlow.value.isShelvesSheetRequested shouldBe false
            stateFlow.value.shelvesSheet.dismissed() shouldBe ManageShelves
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `keeps the sheet model while it hides`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(shelvesSheet = openShelvesSession())

            // ----- Act -----
            OnManageShelvesRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheet shouldBe openShelvesSession(
                closing = true,
                pending = ManageShelves,
            )
        }

        @Test
        fun `stays closed and records nothing when the sheet is not open`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(shelvesSheet = SheetSession.Closed)

            // ----- Act -----
            OnManageShelvesRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheet shouldBe SheetSession.Closed
            stateFlow.value.shelvesSheet.dismissed() shouldBe null
        }
    }
}
