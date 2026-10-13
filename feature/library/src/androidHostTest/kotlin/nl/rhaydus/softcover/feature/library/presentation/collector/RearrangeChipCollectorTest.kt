package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RearrangeChipCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.defaultDispatcher
            } returns testDispatcher
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `a tab present in displayBooksByTab but absent from booksByTab gets no chip`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = RearrangeChipCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = persistentListOf(readingTab),
                sortModeByTab = persistentMapOf(readingTab.id to LibrarySortMode.MANUAL),
                displayBooksByTab = persistentMapOf(readingTab.id to List(2) { mockk<Book>() }.toImmutableList()),
            )

            // ----- Assert -----
            stateFlow.value.rearrangeChipFor(readingTab.id) shouldBe null
            job.cancel()
        }

        @Test
        fun `a ranked custom list qualifies once it has 2 or more editions`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val customListTab = LibraryTab.CustomList(
                listId = 10,
                listName = "Winter reading",
            )
            val rankedList = BookList(
                id = 10,
                name = "Winter reading",
                slug = "winter-reading",
                ranked = true,
                books = emptyList(),
            )
            val collector = RearrangeChipCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = persistentListOf(customListTab),
                sortModeByTab = persistentMapOf(customListTab.id to LibrarySortMode.ORDER),
                editionsByTab = persistentMapOf(customListTab.id to List(2) { mockk<BookEdition>() }.toImmutableList()),
                displayEditionsByTab = persistentMapOf(customListTab.id to List(2) { mockk<BookEdition>() }.toImmutableList()),
                customLists = persistentListOf(rankedList),
            )

            // ----- Assert -----
            stateFlow.value.rearrangeChipFor(customListTab.id)?.label shouldBe "Reorder"
            job.cancel()
        }

        @Test
        fun `unrelated state change that leaves the snapshot equal does not retrigger recompute`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val collector = RearrangeChipCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                stateFlow.value = LibraryUiState(
                    visibleTabs = persistentListOf(readingTab),
                    sortModeByTab = persistentMapOf(readingTab.id to LibrarySortMode.MANUAL),
                    displayBooksByTab = persistentMapOf(readingTab.id to List(2) { mockk<Book>() }.toImmutableList()),
                )
                val chipByTabAfterFirstEmit = stateFlow.value.rearrangeChipByTab

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

                // ----- Assert -----
                (stateFlow.value.rearrangeChipByTab === chipByTabAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
