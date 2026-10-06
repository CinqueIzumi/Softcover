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
        fun `opening seeds the draft and sheet from the selected tab and clears stale tag search text`() = runTest {
            // ----- Arrange -----
            val selectedTabId = "list-42"
            val committedFilters = LibraryFilters(formats = setOf("ebook"))
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                selectedTabId = selectedTabId,
                filtersByTab = mapOf(selectedTabId to committedFilters),
                tabStatsByTab = mapOf(
                    selectedTabId to LibraryTabStats(
                        itemCount = 11,
                        totalPages = 0,
                    ),
                ),
                tagSearchField = initial.tagSearchField.copy(text = "stale"),
            )

            // ----- Act -----
            OnFilterSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe true
            stateFlow.value.filterDraft shouldBe LibraryFilterDraft(
                tabId = selectedTabId,
                filters = committedFilters,
            )
            stateFlow.value.filterSheet shouldBe filterSheetScaffoldFor(
                resultCount = 11,
                filtersActive = true,
            )
            stateFlow.value.tagSearchField.text shouldBe ""
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
        fun `closing discards the draft and tag search text and keeps the existing filterSheet`() = runTest {
            // ----- Arrange -----
            val existingSheet = filterSheetScaffoldFor(
                resultCount = 7,
                filtersActive = true,
            )
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                isFilterSheetExpanded = true,
                filterSheet = existingSheet,
                filterDraft = LibraryFilterDraft(
                    tabId = "list-42",
                    filters = LibraryFilters(formats = setOf("ebook")),
                    tagSearch = "kotlin",
                ),
                tagSearchField = initial.tagSearchField.copy(text = "kotlin"),
            )

            // ----- Act -----
            OnFilterSheetExpandedChangeAction(expanded = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe false
            stateFlow.value.filterDraft shouldBe null
            stateFlow.value.tagSearchField.text shouldBe ""
            stateFlow.value.filterSheet shouldBe existingSheet
        }
    }
}
