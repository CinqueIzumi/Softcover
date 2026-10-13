package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnTabSelectedActionTest {
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
        fun `switches tab and collapses selection mode and rearranging`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                isRearranging = true,
                listsBeingMutated = persistentSetOf(5),
            )

            // ----- Act -----
            OnTabSelectedAction(tabId = "want-to-read").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedTabId shouldBe "want-to-read"
            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.isRearranging shouldBe false
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `closes the bulk move menu and withdraws the bulk sheet requests`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isBulkMoveMenuExpanded = true,
                isBulkRemoveSheetRequested = true,
                isChooseListsSheetRequested = true,
            )

            // ----- Act -----
            OnTabSelectedAction(tabId = "want-to-read").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkMoveMenuExpanded shouldBe false
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.isChooseListsSheetRequested shouldBe false
        }

        @Test
        fun `leaves the bulk sheet sessions closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState()

            // ----- Act -----
            OnTabSelectedAction(tabId = "want-to-read").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe SheetSession.Closed
            stateFlow.value.chooseListsSheet shouldBe SheetSession.Closed
        }
    }
}
