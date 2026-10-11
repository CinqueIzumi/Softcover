package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
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

class OnBulkAddToListSheetShownActionTest {
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
        fun `requests the chooser without building its model when shown is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(selectedBookIds = setOf(1, 2))

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isChooseListsSheetRequested shouldBe true
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `leaves a closing session for the collector to replace when shown is true`() = runTest {
            // ----- Arrange -----
            val closing = openChooseListsSession(
                closing = true,
                pending = CreateList,
            )
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                chooseListsSheet = closing,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isChooseListsSheetRequested shouldBe true
            stateFlow.value.chooseListsSheet shouldBe closing
        }

        @Test
        fun `closes the session and withdraws the request when shown is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                chooseListsSheet = openChooseListsSession(closing = true),
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
            stateFlow.value.isChooseListsSheetRequested shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
