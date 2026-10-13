package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.filterSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FilterDraftChipsCollectorTest {
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
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.defaultDispatcher
            } returns testDispatcher
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `filterSheet footer follows the selection's result count and clear-all state`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val collector = FilterDraftChipsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    filterDraft = LibraryFilterDraft(
                        tabId = "all",
                        filters = LibraryFilters(formats = persistentSetOf("ebook")),
                    ),
                    booksByTab = persistentMapOf("all" to persistentListOf()),
                )

                // ----- Assert -----
                stateFlow.value.filterSheet.scaffold shouldBe filterSheetScaffoldFor(
                    resultCount = 0,
                    filtersActive = true,
                )
                job.cancel()
            }

        @Test
        fun `filterSheet is left untouched while the sheet is closed`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val seeded = filterSheetScaffoldFor(
                resultCount = 8,
                filtersActive = true,
            )
            stateFlow.value = LibraryUiState(
                filterSheet = LibraryUiState().filterSheet.copy(scaffold = seeded),
            )
            val collector = FilterDraftChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchQuery = "x")

            // ----- Assert -----
            stateFlow.value.filterSheet.scaffold shouldBe seeded
            job.cancel()
        }

        @Test
        fun `editing the draft's tagSearch recomputes which tag chips remain`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tagFiction = Tag(
                id = 1,
                name = "Fiction",
                category = TagCategory.GENRE,
            )
            val tagScifi = Tag(
                id = 2,
                name = "Sci-Fi",
                category = TagCategory.GENRE,
            )
            val chips = LibraryFilterChips(
                tagChips = listOf(
                    ChipUiModel(
                        key = "tag:1",
                        label = "Fiction",
                    ) to LibraryFilterValue.Tag(tag = tagFiction),
                    ChipUiModel(
                        key = "tag:2",
                        label = "Sci-Fi",
                    ) to LibraryFilterValue.Tag(tag = tagScifi),
                ).toChipSet(),
            )
            val collector = FilterDraftChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(tabId = "all"),
                filterChipsByTab = persistentMapOf("all" to chips),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                filterDraft = stateFlow.value.filterDraft?.copy(tagSearch = "fic"),
            )

            // ----- Assert -----
            stateFlow.value.filterSheet.selection?.tagChips?.map { it.key } shouldBe listOf("tag:1")
            job.cancel()
        }

        @Test
        fun `unrelated state change that leaves the snapshot equal does not retrigger recompute`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val collector = FilterDraftChipsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value = LibraryUiState(
                    filterDraft = LibraryFilterDraft(tabId = "all"),
                )
                val selectionAfterFirstEmit = stateFlow.value.filterSheet.selection

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

                // ----- Assert -----
                (stateFlow.value.filterSheet.selection === selectionAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
