package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.filterSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFilterSheetExpandedChangeActionTest {
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
        fun `sets isFilterSheetExpanded to true when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isFilterSheetExpanded = false)
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe true
        }

        @Test
        fun `sets isFilterSheetExpanded to false when expanded is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isFilterSheetExpanded = true)
            val action = OnFilterSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe false
        }

        @Test
        fun `preserves other state fields when updating isFilterSheetExpanded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isFilterSheetExpanded = false,
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isFilterSheetExpanded shouldBe true
        }

        @Test
        fun `opening seeds filterDraft from the selected tab's committed filters`() = runTest {
            // ----- Arrange -----
            val selectedTabId = "list-42"
            val committedFilters = LibraryFilters(formats = setOf("ebook"))
            stateFlow.value = LibraryUiState(
                selectedTabId = selectedTabId,
                filtersByTab = mapOf(selectedTabId to committedFilters),
                filterDraft = null,
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe LibraryFilterDraft(
                tabId = selectedTabId,
                filters = committedFilters,
            )
        }

        @Test
        fun `closing clears filterDraft, discarding any in-sheet edits`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isFilterSheetExpanded = true,
                filterDraft = LibraryFilterDraft(
                    tabId = "list-42",
                    filters = LibraryFilters(formats = setOf("ebook")),
                    tagSearch = "kotlin",
                ),
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe null
        }

        @Test
        fun `opening clears any stale tag search text`() = runTest {
            // ----- Arrange -----
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                tagSearchField = initial.tagSearchField.copy(text = "stale"),
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.tagSearchField.text shouldBe ""
        }

        @Test
        fun `closing clears the tag search text`() = runTest {
            // ----- Arrange -----
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                tagSearchField = initial.tagSearchField.copy(text = "stale"),
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.tagSearchField.text shouldBe ""
        }

        @Test
        fun `opening seeds filterSheet from the tab's item count and committed filters`() = runTest {
            // ----- Arrange -----
            val tabId = "list-42"
            stateFlow.value = LibraryUiState(
                selectedTabId = tabId,
                filtersByTab = mapOf(tabId to LibraryFilters(formats = setOf("ebook"))),
                tabStatsByTab = mapOf(
                    tabId to LibraryTabStats(
                        itemCount = 11,
                        totalPages = 0,
                    ),
                ),
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterSheet shouldBe filterSheetScaffoldFor(
                resultCount = 11,
                filtersActive = true,
            )
        }

        @Test
        fun `opening with no committed filters seeds an inactive Clear all`() = runTest {
            // ----- Arrange -----
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterSheet shouldBe filterSheetScaffoldFor(
                resultCount = 0,
                filtersActive = false,
            )
        }

        @Test
        fun `closing keeps the existing filterSheet`() = runTest {
            // ----- Arrange -----
            val existingSheet = filterSheetScaffoldFor(
                resultCount = 7,
                filtersActive = true,
            )
            stateFlow.value = LibraryUiState(
                isFilterSheetExpanded = true,
                filterSheet = existingSheet,
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterSheet shouldBe existingSheet
        }
    }
}
