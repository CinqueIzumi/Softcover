package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
        fun `rearrangeChipByTab gets an entry for a tab that qualifies for rearranging`() = runTest(testDispatcher) {
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
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                booksByTab = mapOf(readingTab.id to List(2) { mockk() }),
                displayBooksByTab = mapOf(readingTab.id to List(2) { mockk() }),
            )

            // ----- Assert -----
            stateFlow.value.rearrangeChipFor(readingTab.id)?.label shouldBe "Reorder"
            job.cancel()
        }

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
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                displayBooksByTab = mapOf(readingTab.id to List(2) { mockk() }),
            )

            // ----- Assert -----
            stateFlow.value.rearrangeChipFor(readingTab.id) shouldBe null
            job.cancel()
        }

        @Test
        fun `a tab that does not qualify has no entry in rearrangeChipByTab`() = runTest(testDispatcher) {
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
                visibleTabs = listOf(readingTab),
                sortModeByTab = mapOf(readingTab.id to LibrarySortMode.TITLE),
                displayBooksByTab = mapOf(readingTab.id to List(2) { mockk() }),
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
                visibleTabs = listOf(customListTab),
                sortModeByTab = mapOf(customListTab.id to LibrarySortMode.ORDER),
                editionsByTab = mapOf(customListTab.id to List(2) { mockk() }),
                displayEditionsByTab = mapOf(customListTab.id to List(2) { mockk() }),
                customLists = listOf(rankedList),
            )

            // ----- Assert -----
            stateFlow.value.rearrangeChipFor(customListTab.id)?.label shouldBe "Reorder"
            job.cancel()
        }

        @Test
        fun `isRearranging replaces every visible tab's chip label with Done`() = runTest(testDispatcher) {
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
                visibleTabs = listOf(LibraryTab.All, readingTab),
                isRearranging = true,
            )

            // ----- Assert -----
            stateFlow.value.rearrangeChipFor(LibraryTab.All.id)?.label shouldBe "Done"
            stateFlow.value.rearrangeChipFor(readingTab.id)?.label shouldBe "Done"
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
                    visibleTabs = listOf(readingTab),
                    sortModeByTab = mapOf(readingTab.id to LibrarySortMode.MANUAL),
                    displayBooksByTab = mapOf(readingTab.id to List(2) { mockk() }),
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
