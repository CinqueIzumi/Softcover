package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.openChooseListsSession
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.CreateList
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnChooseListsSheetDismissedActionTest {
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
        fun `closes the session and sends CreateListRequestedEvent when a create-list was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                chooseListsSheet = openChooseListsSession(
                    closing = true,
                    pending = CreateList,
                ),
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnChooseListsSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
            stateFlow.value.isChooseListsSheetRequested shouldBe false
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<LibraryEvent.CreateListRequestedEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `closes the session and sends no event when no create-list was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(chooseListsSheet = openChooseListsSession(closing = true))

            // ----- Act -----
            OnChooseListsSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sends no event when the session was already closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(chooseListsSheet = SheetSession.Closed)

            // ----- Act -----
            OnChooseListsSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
