package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.arrangeSheetScaffoldFor
import nl.rhaydus.softcover.feature.library.presentation.state.bulkRemoveSheetScaffoldFor
import nl.rhaydus.softcover.feature.library.presentation.state.shelvesSheetScaffold
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetScaffoldCollectorsTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun TestScope.launchCollector(collector: LibraryCollector) {
        backgroundScope.launch {
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class ArrangeSheetCollectorOnLaunch {
        @Test
        fun `result count follows the selected tab's display books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = LibraryTab.All.id,
                booksByTab = mapOf(LibraryTab.All.id to List(5) { mockk<Book>() }),
                displayBooksByTab = mapOf(LibraryTab.All.id to List(2) { mockk<Book>() }),
                tabStatsByTab = mapOf(
                    LibraryTab.All.id to LibraryTabStats(
                        itemCount = 5,
                        totalPages = 0,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 2)
        }

        @Test
        fun `custom list result count follows the display editions`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.CustomList.idFor(listId = 3)
            launchCollector(ArrangeSheetCollector())

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = tabId,
                editionsByTab = mapOf(tabId to List(4) { mockk<BookEdition>() }),
                displayEditionsByTab = mapOf(tabId to List(3) { mockk<BookEdition>() }),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 3)
        }

        @Test
        fun `falls back to the tab stats before the display list has landed`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = LibraryTab.All.id,
                tabStatsByTab = mapOf(
                    LibraryTab.All.id to LibraryTabStats(
                        itemCount = 9,
                        totalPages = 0,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 9)
        }

        @Test
        fun `falls back to zero when there are no stats either`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())

            // ----- Act -----
            stateFlow.value = LibraryUiState()

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 0)
        }

        @Test
        fun `publishes an unchecked titles toggle for a cover-only grid draft`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())
            val gridDraft = LibraryArrangeDraft(
                tabId = LibraryTab.All.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            )

            // ----- Act -----
            stateFlow.value = LibraryUiState(arrangeDraft = gridDraft)

            // ----- Assert -----
            stateFlow.value.arrangeTitlesToggle.shouldNotBeNull().checked shouldBe false
        }

        @Test
        fun `clears the titles toggle when the draft switches to a list layout`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())
            val gridDraft = LibraryArrangeDraft(
                tabId = LibraryTab.All.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = gridDraft)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                arrangeDraft = gridDraft.copy(gridLayout = LibraryGridLayout.LIST_LARGE),
            )

            // ----- Assert -----
            stateFlow.value.arrangeTitlesToggle.shouldBeNull()
        }

        @Test
        fun `publishes make ordered for the selected unranked list`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())
            val unranked = BookList(
                id = 3,
                name = "Faves",
                slug = "faves",
                ranked = false,
                books = emptyList(),
            )

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = LibraryTab.CustomList.idFor(listId = 3),
                customLists = listOf(unranked),
            )

            // ----- Assert -----
            stateFlow.value.arrangeMakeOrderedListId shouldBe 3
        }

        @Test
        fun `clears make ordered once the selected list becomes ranked`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ArrangeSheetCollector())
            val unranked = BookList(
                id = 3,
                name = "Faves",
                slug = "faves",
                ranked = false,
                books = emptyList(),
            )
            stateFlow.value = LibraryUiState(
                selectedTabId = LibraryTab.CustomList.idFor(listId = 3),
                customLists = listOf(unranked),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(customLists = listOf(unranked.copy(ranked = true)))

            // ----- Assert -----
            stateFlow.value.arrangeMakeOrderedListId.shouldBeNull()
        }
    }

    @Nested
    inner class BulkRemoveSheetCollectorOnLaunch {
        @Test
        fun `maps the selection size and the in-flight flag`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(BulkRemoveSheetCollector())

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2, 3),
                bulkActionInProgress = true,
            )

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe bulkRemoveSheetScaffoldFor(
                bookCount = 3,
                inProgress = true,
            )
        }

        @Test
        fun `a close request flags the sheet without changing its content`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(BulkRemoveSheetCollector())
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2),
                bulkActionInProgress = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isBulkRemoveCloseRequested = true)

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe bulkRemoveSheetScaffoldFor(
                bookCount = 2,
                inProgress = true,
            ).copy(closeRequested = true)
        }

        @Test
        fun `emptying the selection after a close request keeps the headline frozen`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(BulkRemoveSheetCollector())
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2, 3),
                bulkActionInProgress = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                selectedBookIds = emptySet(),
                bulkActionInProgress = false,
                isBulkRemoveCloseRequested = true,
            )

            // ----- Assert -----
            val sheet = stateFlow.value.bulkRemoveSheet
            sheet.closeRequested shouldBe true
            sheet.header.section.headlineText shouldBe "Remove 3 books?"
        }

        @Test
        fun `clearing the close request rebuilds the sheet from the current state`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(BulkRemoveSheetCollector())
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2, 3),
                isBulkRemoveCloseRequested = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                selectedBookIds = setOf(1),
                isBulkRemoveCloseRequested = false,
            )

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe bulkRemoveSheetScaffoldFor(
                bookCount = 1,
                inProgress = false,
            )
        }
    }

    @Nested
    inner class ShelvesSheetCollectorOnLaunch {
        @Test
        fun `maps visible tabs, selection and stats into rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ShelvesSheetCollector())
            val tabs = listOf(
                LibraryTab.All,
                LibraryTab.CustomList(
                    listId = 2,
                    listName = "Faves",
                ),
            )

            val tabStatsByTab = mapOf(
                LibraryTab.CustomList.idFor(listId = 2) to LibraryTabStats(
                    itemCount = 6,
                    totalPages = 0,
                ),
            )

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = tabs,
                selectedTabId = LibraryTab.CustomList.idFor(listId = 2),
                tabStatsByTab = tabStatsByTab,
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheetGroup.rows shouldBe shelvesSheetRowsFor(
                visibleTabs = tabs,
                selectedTabId = LibraryTab.CustomList.idFor(listId = 2),
                tabStatsByTab = tabStatsByTab,
            )
            stateFlow.value.shelvesSheetGroup.rows.map { it.selected } shouldBe listOf(false, true)
        }

        @Test
        fun `a close request flags the sheet scaffold without changing the rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ShelvesSheetCollector())
            val rowsBefore = stateFlow.value.shelvesSheetGroup.rows

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isShelvesSheetCloseRequested = true)

            // ----- Assert -----
            stateFlow.value.shelvesSheet shouldBe shelvesSheetScaffold().copy(closeRequested = true)
            stateFlow.value.shelvesSheetGroup.rows shouldBe rowsBefore
        }

        @Test
        fun `clearing the close request unflags the sheet scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ShelvesSheetCollector())
            stateFlow.value = stateFlow.value.copy(isShelvesSheetCloseRequested = true)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isShelvesSheetCloseRequested = false)

            // ----- Assert -----
            stateFlow.value.shelvesSheet shouldBe shelvesSheetScaffold()
        }

        @Test
        fun `rows do not dismiss the sheet themselves so the host can run the pick first`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector(ShelvesSheetCollector())

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedTabId = LibraryTab.All.id)

            // ----- Assert -----
            stateFlow.value.shelvesSheetGroup.dismissesSheet shouldBe false
        }
    }
}
