package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
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
        fun `hides the sheet, clears the flag and sends CreateListRequestedEvent when a create-list was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isBulkAddToListSheetShown = true,
                isCreateListPending = true,
            )

            // ----- Act -----
            OnChooseListsSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkAddToListSheetShown shouldBe false
            stateFlow.value.isCreateListPending shouldBe false
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<LibraryEvent.CreateListRequestedEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `hides the sheet and sends no event when no create-list was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isBulkAddToListSheetShown = true,
                isCreateListPending = false,
            )

            // ----- Act -----
            OnChooseListsSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkAddToListSheetShown shouldBe false
            stateFlow.value.isCreateListPending shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
