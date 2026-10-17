package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.openChooseListsSession
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.CreateList
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.selectionCountOf
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
        fun `leaves selection mode and clears the selection and the in-flight lists`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                isBulkMoveMenuExpanded = true,
                listsBeingMutated = persistentSetOf(5),
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
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `withdraws the bulk sheet requests so the collectors hide them`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                isBulkRemoveSheetRequested = true,
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnExitSelectionModeAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.isChooseListsSheetRequested shouldBe false
        }

        @Test
        fun `keeps a create-list request recorded on the chooser`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                chooseListsSheet = openChooseListsSession(pending = CreateList),
            )

            // ----- Act -----
            OnExitSelectionModeAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet.dismissed() shouldBe CreateList
        }

        @Test
        fun `leaves closed bulk surfaces closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
            )

            // ----- Act -----
            OnExitSelectionModeAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe SheetSession.Closed
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
        }


        @Test
        fun `exiting resets the selection count to zero`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2, 3),
                selectionCount = selectionCountOf(count = 3),
            )

            // ----- Act -----
            OnExitSelectionModeAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 0)
        }
    }
}
