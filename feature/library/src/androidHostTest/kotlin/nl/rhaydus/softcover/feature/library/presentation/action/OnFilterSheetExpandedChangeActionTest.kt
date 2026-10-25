package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
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
import nl.rhaydus.softcover.feature.library.presentation.state.TagSectionDraft
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
        fun `opening seeds the draft and sheet from the selected tab`() = runTest {
            // ----- Arrange -----
            val selectedTabId = "list-42"
            val committedFilters = LibraryFilters(formats = persistentSetOf("ebook"))
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                selectedTabId = selectedTabId,
                filtersByTab = persistentMapOf(selectedTabId to committedFilters),
                tabStatsByTab = persistentMapOf(
                    selectedTabId to LibraryTabStats(
                        itemCount = 11,
                        totalPages = 0,
                    ),
                ),
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
            stateFlow.value.filterSheet.scaffold shouldBe filterSheetScaffoldFor(
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
            stateFlow.value.filterSheet.scaffold shouldBe filterSheetScaffoldFor(
                resultCount = 0,
                filtersActive = false,
            )
        }

        @Test
        fun `opening starts with empty tag sections even when an earlier draft had a search`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(
                    tabId = "all",
                    hardcoverTagsSection = TagSectionDraft(search = "cosy"),
                ),
            )

            // ----- Act -----
            OnFilterSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.hardcoverTagsSection shouldBe TagSectionDraft()
        }

        @Test
        fun `closing discards the draft and its tag section state and keeps the existing filterSheet`() = runTest {
            // ----- Arrange -----
            val existingSheet = filterSheetScaffoldFor(
                resultCount = 7,
                filtersActive = true,
            )
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                isFilterSheetExpanded = true,
                filterSheet = initial.filterSheet.copy(
                    scaffold = existingSheet,
                ),
                filterDraft = LibraryFilterDraft(
                    tabId = "list-42",
                    filters = LibraryFilters(formats = persistentSetOf("ebook")),
                    yourTagsSection = TagSectionDraft(search = "kotlin"),
                ),
            )

            // ----- Act -----
            OnFilterSheetExpandedChangeAction(expanded = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe false
            stateFlow.value.filterDraft shouldBe null
            stateFlow.value.filterSheet.scaffold shouldBe existingSheet
        }
    }
}
