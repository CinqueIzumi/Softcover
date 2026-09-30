package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.arrangeSheetScaffoldFor
import nl.rhaydus.softcover.feature.library.presentation.state.bulkRemoveSheetScaffoldFor
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

    @Nested
    inner class ArrangeSheetCollectorOnLaunch {
        @Test
        fun `result count follows the selected tab's display books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launch {
                ArrangeSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = LibraryTab.All.id,
                booksByTab = mapOf(LibraryTab.All.id to List(5) { mockk<Book>() }),
                displayBooksByTab = mapOf(LibraryTab.All.id to List(2) { mockk<Book>() }),
                tabStatsByTab = mapOf(LibraryTab.All.id to LibraryTabStats(
                    itemCount = 5,
                    totalPages = 0,
                ),),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 2)
            job.cancel()
        }

        @Test
        fun `custom list result count follows the display editions`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = "list-3"
            val job = launch {
                ArrangeSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = tabId,
                editionsByTab = mapOf(tabId to List(4) { mockk<BookEdition>() }),
                displayEditionsByTab = mapOf(tabId to List(3) { mockk<BookEdition>() }),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 3)
            job.cancel()
        }

        @Test
        fun `falls back to the tab stats before the display list has landed`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launch {
                ArrangeSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectedTabId = LibraryTab.All.id,
                tabStatsByTab = mapOf(LibraryTab.All.id to LibraryTabStats(
                    itemCount = 9,
                    totalPages = 0,
                ),),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 9)
            job.cancel()
        }

        @Test
        fun `falls back to zero when there are no stats either`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launch {
                ArrangeSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState()

            // ----- Assert -----
            stateFlow.value.arrangeSheet shouldBe arrangeSheetScaffoldFor(resultCount = 0)
            job.cancel()
        }
    }

    @Nested
    inner class BulkRemoveSheetCollectorOnLaunch {
        @Test
        fun `maps the selection size and the in-flight flag`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launch {
                BulkRemoveSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

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
            job.cancel()
        }

        @Test
        fun `re-maps when the in-flight flag clears`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launch {
                BulkRemoveSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                bulkActionInProgress = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bulkActionInProgress = false)

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe bulkRemoveSheetScaffoldFor(
                bookCount = 1,
                inProgress = false,
            )
            job.cancel()
        }
    }

    @Nested
    inner class ShelvesSheetCollectorOnLaunch {
        @Test
        fun `maps visible tabs, selection and stats into rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launch {
                ShelvesSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val tabs = listOf(LibraryTab.All, LibraryTab.CustomList(
                listId = 2,
                listName = "Faves",
            ),)

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = tabs,
                selectedTabId = "list-2",
                tabStatsByTab = mapOf("list-2" to LibraryTabStats(
                    itemCount = 6,
                    totalPages = 0,
                ),),
            )

            // ----- Assert -----
            stateFlow.value.shelvesSheetRows shouldBe shelvesSheetRowsFor(
                visibleTabs = tabs,
                selectedTabId = "list-2",
                tabStatsByTab = mapOf("list-2" to LibraryTabStats(
                    itemCount = 6,
                    totalPages = 0,
                ),),
            )
            stateFlow.value.shelvesSheetRows.map { it.selected } shouldBe listOf(false, true)
            job.cancel()
        }
    }
}
