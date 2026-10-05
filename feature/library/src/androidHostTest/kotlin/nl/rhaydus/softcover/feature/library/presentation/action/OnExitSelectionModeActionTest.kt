package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
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

class OnExitSelectionModeActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `leaves selection mode, clears the selection and dismisses every bulk surface`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = setOf(1, 2),
                isBulkMoveMenuExpanded = true,
                isBulkRemoveSheetShown = true,
                isBulkAddToListSheetShown = true,
                listsBeingMutated = setOf(5),
            )

            // ----- Act -----
            OnExitSelectionModeAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.isBulkMoveMenuExpanded shouldBe false
            stateFlow.value.isBulkRemoveSheetShown shouldBe false
            stateFlow.value.isBulkAddToListSheetShown shouldBe false
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }
    }
}
