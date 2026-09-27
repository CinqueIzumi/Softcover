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
import nl.rhaydus.softcover.core.component.chip.ChipVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class ArrangeDraftChipsCollectorTest {
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
        fun `arrangeLayoutChips and arrangeSortChips stay empty while the sheet is closed`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = ArrangeDraftChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(visibleTabs = listOf(readingTab))

            // ----- Assert -----
            stateFlow.value.arrangeLayoutChips shouldBe emptyList()
            stateFlow.value.arrangeSortChips shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `opening the sheet with a draft populates the layout and sort chips for the matching tab`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val collector = ArrangeDraftChipsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readingTab),
                    arrangeDraft = LibraryArrangeDraft(
                        tabId = readingTab.id,
                        sortMode = LibrarySortMode.MANUAL,
                        sortDirection = SortDirection.ASCENDING,
                        gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
                    ),
                )

                // ----- Assert -----
                stateFlow.value.arrangeLayoutChips.first { it.key == "layout:GRID_TWO" }.variant shouldBe
                    ChipVariant.Choice(selected = true)
                stateFlow.value.arrangeSortChips.first { it.key == "sort:MANUAL" }.variant shouldBe
                    ChipVariant.Choice(selected = true)
                job.cancel()
            }

        @Test
        fun `editing the draft's sort mode recomputes which sort chip is selected`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = ArrangeDraftChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(readingTab),
                arrangeDraft = LibraryArrangeDraft(
                    tabId = readingTab.id,
                    sortMode = LibrarySortMode.MANUAL,
                    sortDirection = SortDirection.ASCENDING,
                    gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
                ),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                arrangeDraft = stateFlow.value.arrangeDraft?.copy(sortMode = LibrarySortMode.TITLE),
            )

            // ----- Assert -----
            stateFlow.value.arrangeSortChips.first { it.key == "sort:MANUAL" }.variant shouldBe
                ChipVariant.Choice(selected = false)
            stateFlow.value.arrangeSortChips.first { it.key == "sort:TITLE" }.variant shouldBe
                ChipVariant.Choice(
                    selected = true,
                    trailingIcon = SoftcoverIcon.ArrowDropUp,
                )
            job.cancel()
        }

        @Test
        fun `unrelated state change that leaves the snapshot equal does not retrigger recompute`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val collector = ArrangeDraftChipsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readingTab),
                    arrangeDraft = LibraryArrangeDraft(
                        tabId = readingTab.id,
                        sortMode = LibrarySortMode.MANUAL,
                        sortDirection = SortDirection.ASCENDING,
                        gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
                    ),
                )
                val layoutChipsAfterFirstEmit = stateFlow.value.arrangeLayoutChips
                val sortChipsAfterFirstEmit = stateFlow.value.arrangeSortChips

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

                // ----- Assert -----
                (stateFlow.value.arrangeLayoutChips === layoutChipsAfterFirstEmit) shouldBe true
                (stateFlow.value.arrangeSortChips === sortChipsAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
