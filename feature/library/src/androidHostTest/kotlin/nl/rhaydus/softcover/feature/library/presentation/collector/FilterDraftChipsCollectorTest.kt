package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
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
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.HardcoverTagKey
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTagOption
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSectionDraft
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
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
        fun `editing a section's search recomputes which tags its rows list`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = UserTagKey(
                            category = TagCategory.MOOD,
                            name = "cozy",
                        ),
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                    LibraryTagOption(
                        key = UserTagKey(
                            category = TagCategory.GENRE,
                            name = "fantasy",
                        ),
                        category = TagCategory.GENRE,
                        count = 1,
                    ),
                ),
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
                filterOptionsByTab = persistentMapOf("all" to options),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                filterDraft = stateFlow.value.filterDraft?.copy(
                    yourTagsSection = TagSectionDraft(search = "fan"),
                ),
            )

            // ----- Assert -----
            val yours = stateFlow.value.filterSheet.selection?.yourTags.shouldNotBeNull()
            yours.tagChips.map { it.label } shouldBe listOf("fantasy · 1")
            job.cancel()
        }

        @Test
        fun `the built search field carries each source's own search text from the draft`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val options = LibraryFilterOptions(
                    yourTags = persistentListOf(
                        LibraryTagOption(
                            key = UserTagKey(
                                category = TagCategory.MOOD,
                                name = "cozy",
                            ),
                            category = TagCategory.MOOD,
                            count = 2,
                        ),
                    ),
                    hardcoverTags = persistentListOf(
                        LibraryTagOption(
                            key = HardcoverTagKey(
                                id = 1,
                                name = "magic",
                            ),
                            category = TagCategory.GENRE,
                            count = 3,
                        ),
                    ),
                )
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
                        yourTagsSection = TagSectionDraft(search = "coz"),
                    ),
                    filterOptionsByTab = persistentMapOf("all" to options),
                )

                // ----- Assert -----
                val selection = stateFlow.value.filterSheet.selection
                selection?.yourTags?.searchField?.text shouldBe "coz"
                selection?.hardcoverTags?.searchField?.text shouldBe ""
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
