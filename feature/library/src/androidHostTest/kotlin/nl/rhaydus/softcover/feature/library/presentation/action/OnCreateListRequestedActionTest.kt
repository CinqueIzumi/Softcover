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
import nl.rhaydus.softcover.feature.library.presentation.openChooseListsSession
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.CreateList
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnCreateListRequestedActionTest {
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
        fun `asks the open sheet to close and records the create-list request without sending an event`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                chooseListsSheet = openChooseListsSession(),
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnCreateListRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet.isClosing shouldBe true
            stateFlow.value.isChooseListsSheetRequested shouldBe false
            stateFlow.value.chooseListsSheet.dismissed() shouldBe CreateList
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `keeps the sheet model while it hides`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(chooseListsSheet = openChooseListsSession())

            // ----- Act -----
            OnCreateListRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe openChooseListsSession(
                closing = true,
                pending = CreateList,
            )
        }

        @Test
        fun `stays closed and records nothing when the sheet is not open`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(chooseListsSheet = SheetSession.Closed)

            // ----- Act -----
            OnCreateListRequestedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
        }
    }
}
