package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnArrangeSheetExpandedChangeActionTest {
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
        fun `sets isArrangeSheetExpanded to true when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isArrangeSheetExpanded = false)
            val action = OnArrangeSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isArrangeSheetExpanded shouldBe true
        }

        @Test
        fun `sets isArrangeSheetExpanded to false when expanded is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isArrangeSheetExpanded = true)
            val action = OnArrangeSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isArrangeSheetExpanded shouldBe false
        }

        @Test
        fun `preserves other state fields when updating isArrangeSheetExpanded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isArrangeSheetExpanded = false,
            )
            val action = OnArrangeSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isArrangeSheetExpanded shouldBe true
        }

        @Test
        fun `opening seeds arrangeDraft from the selected tab's committed sort and layout`() = runTest {
            // ----- Arrange -----
            val selectedTabId = "list-42"
            stateFlow.value = LibraryUiState(
                selectedTabId = selectedTabId,
                sortModeByTab = mapOf(selectedTabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(selectedTabId to SortDirection.ASCENDING),
                gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS,
                arrangeDraft = null,
            )
            val action = OnArrangeSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe LibraryArrangeDraft(
                tabId = selectedTabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS,
            )
        }

        @Test
        fun `closing clears arrangeDraft, discarding any in-sheet edits`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isArrangeSheetExpanded = true,
                arrangeDraft = LibraryArrangeDraft(
                    tabId = "list-42",
                    sortMode = LibrarySortMode.RATING,
                    sortDirection = SortDirection.DESCENDING,
                    gridLayout = LibraryGridLayout.LIST_LARGE,
                ),
            )
            val action = OnArrangeSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe null
        }
    }
}
