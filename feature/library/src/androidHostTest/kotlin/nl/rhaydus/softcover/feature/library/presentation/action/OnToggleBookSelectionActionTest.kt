package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.selectionCountOf
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnToggleBookSelectionActionTest {
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
        fun `selects an unselected book and keeps selection mode`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 2).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookIds shouldBe setOf(1, 2)
            stateFlow.value.selectionMode shouldBe true
        }

        @Test
        fun `deselects a selected book while others remain selected`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 1).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookIds shouldBe setOf(2)
            stateFlow.value.selectionMode shouldBe true
        }

        @Test
        fun `deselecting the last book leaves selection mode and clears the in-flight lists`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                isBulkMoveMenuExpanded = true,
                listsBeingMutated = persistentSetOf(5),
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 1).execute(
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
        fun `deselecting the last book withdraws the bulk sheet requests`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                isBulkRemoveSheetRequested = true,
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 1).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.isChooseListsSheetRequested shouldBe false
        }

        @Test
        fun `deselecting one of several books keeps the bulk sheet requests`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                isBulkRemoveSheetRequested = true,
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 1).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveSheetRequested shouldBe true
            stateFlow.value.isChooseListsSheetRequested shouldBe true
        }


        @Test
        fun `selecting a book updates the selection count with the set`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 2).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 2)
        }

        @Test
        fun `deselecting the last book resets the selection count to zero`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                selectionCount = selectionCountOf(count = 1),
            )

            // ----- Act -----
            OnToggleBookSelectionAction(bookId = 1).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 0)
        }
    }
}
