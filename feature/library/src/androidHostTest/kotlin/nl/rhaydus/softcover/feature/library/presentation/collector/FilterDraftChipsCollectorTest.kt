package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

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
        fun `filterSheetSelection stays null while the sheet is closed`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = FilterDraftChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState()

            // ----- Assert -----
            stateFlow.value.filterSheetSelection shouldBe null
            job.cancel()
        }

        @Test
        fun `opening the sheet with a draft populates filterSheetSelection for the matching tab`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val chips = LibraryFilterChips(
                    formatChips = listOf(ChipUiModel(
                        key = "format:ebook",
                        label = "ebook",
                    ),),
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
                    filterDraft = LibraryFilterDraft(tabId = "all"),
                    filterChipsByTab = mapOf("all" to chips),
                    booksByTab = mapOf("all" to List(2) { mockk() }),
                )

                // ----- Assert -----
                stateFlow.value.filterSheetSelection?.formatChips?.map { it.key } shouldBe listOf("format:ebook")
                stateFlow.value.filterSheetSelection?.resultCount shouldBe 2
                job.cancel()
            }

        @Test
        fun `editing the draft's tagSearch recomputes which tag chips remain`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val chips = LibraryFilterChips(
                tagChips = listOf(
                    ChipUiModel(
                        key = "tag:1",
                        label = "Fiction",
                    ),
                    ChipUiModel(
                        key = "tag:2",
                        label = "Sci-Fi",
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
                filterChipsByTab = mapOf("all" to chips),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                filterDraft = stateFlow.value.filterDraft?.copy(tagSearch = "fic"),
            )

            // ----- Assert -----
            stateFlow.value.filterSheetSelection?.tagChips?.map { it.key } shouldBe listOf("tag:1")
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
                val selectionAfterFirstEmit = stateFlow.value.filterSheetSelection

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

                // ----- Assert -----
                (stateFlow.value.filterSheetSelection === selectionAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
