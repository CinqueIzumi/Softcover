package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
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

private val SHELVES_LABEL = SectionHeaderUiModel.Label(eyebrow = "Shelves")
private val LISTS_LABEL = SectionHeaderUiModel.Label(eyebrow = "Lists")
private val ALL_TAB = LibraryTab.All
private val READING_TAB = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)
private val LIST_TAB = LibraryTab.CustomList(
    listId = 1,
    listName = "Favourites",
)

private val SidebarRowUiModel<String>.selected: Boolean
    get() = (variant as? SidebarRowVariant.Destination)?.selected == true

private val SidebarRowUiModel<String>.longPressable: Boolean
    get() = (variant as? SidebarRowVariant.Destination)?.longPressable == true

class ShelfSidebarSectionsCollectorTest {
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
    inner class ShelfSidebarSectionsForFun {
        @Test
        fun `shelves-only tabs give one section`() {
            // ----- Act -----
            val sections = shelfSidebarSectionsFor(
                visibleTabs = listOf(ALL_TAB, READING_TAB),
                selectedTabId = ALL_TAB.id,
                shelvesSidebarLabel = SHELVES_LABEL,
                listsSidebarLabel = LISTS_LABEL,
            )

            // ----- Assert -----
            sections.size shouldBe 1
            sections.single().label shouldBe SHELVES_LABEL
            sections.single().rows.map { it.key } shouldBe listOf(ALL_TAB.id, READING_TAB.id)
        }

        @Test
        fun `shelves plus custom lists give two sections, with lists second`() {
            // ----- Act -----
            val sections = shelfSidebarSectionsFor(
                visibleTabs = listOf(ALL_TAB, LIST_TAB),
                selectedTabId = ALL_TAB.id,
                shelvesSidebarLabel = SHELVES_LABEL,
                listsSidebarLabel = LISTS_LABEL,
            )

            // ----- Assert -----
            sections.size shouldBe 2
            sections[0].label shouldBe SHELVES_LABEL
            sections[0].rows.map { it.key } shouldBe listOf(ALL_TAB.id)
            sections[1].label shouldBe LISTS_LABEL
            sections[1].rows.map { it.key } shouldBe listOf(LIST_TAB.id)
        }

        @Test
        fun `selected follows selectedTabId`() {
            // ----- Act -----
            val sections = shelfSidebarSectionsFor(
                visibleTabs = listOf(ALL_TAB, READING_TAB, LIST_TAB),
                selectedTabId = READING_TAB.id,
                shelvesSidebarLabel = SHELVES_LABEL,
                listsSidebarLabel = LISTS_LABEL,
            )

            // ----- Assert -----
            val allRows = sections.flatMap { it.rows }
            allRows.single { it.selected }.key shouldBe READING_TAB.id
        }

        @Test
        fun `every row is long-pressable`() {
            // ----- Act -----
            val sections = shelfSidebarSectionsFor(
                visibleTabs = listOf(ALL_TAB, READING_TAB, LIST_TAB),
                selectedTabId = ALL_TAB.id,
                shelvesSidebarLabel = SHELVES_LABEL,
                listsSidebarLabel = LISTS_LABEL,
            )

            // ----- Assert -----
            sections.flatMap { it.rows }.all { it.longPressable } shouldBe true
        }

        @Test
        fun `no custom lists means no lists section at all`() {
            // ----- Act -----
            val sections = shelfSidebarSectionsFor(
                visibleTabs = listOf(ALL_TAB),
                selectedTabId = ALL_TAB.id,
                shelvesSidebarLabel = SHELVES_LABEL,
                listsSidebarLabel = LISTS_LABEL,
            )

            // ----- Assert -----
            sections.none { it.label == LISTS_LABEL } shouldBe true
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets shelfSidebarSections from the current visibleTabs and selectedTabId on launch`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(ALL_TAB, LIST_TAB),
                    selectedTabId = LIST_TAB.id,
                )
                val collector = ShelfSidebarSectionsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.shelfSidebarSections.size shouldBe 2
                stateFlow.value.shelfSidebarSections.flatMap { it.rows }
                    .single { it.selected }.key shouldBe LIST_TAB.id
                job.cancel()
            }

        @Test
        fun `a change to visibleTabs recomputes shelfSidebarSections`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(ALL_TAB),
                selectedTabId = ALL_TAB.id,
            )
            val collector = ShelfSidebarSectionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(visibleTabs = listOf(ALL_TAB, LIST_TAB))

            // ----- Assert -----
            stateFlow.value.shelfSidebarSections.size shouldBe 2
            job.cancel()
        }

        @Test
        fun `a change to selectedTabId moves the selection to the new tab`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(ALL_TAB, READING_TAB),
                selectedTabId = ALL_TAB.id,
            )
            val collector = ShelfSidebarSectionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedTabId = READING_TAB.id)

            // ----- Assert -----
            stateFlow.value.shelfSidebarSections.flatMap { it.rows }
                .single { it.selected }.key shouldBe READING_TAB.id
            job.cancel()
        }

        @Test
        fun `a state change unrelated to visibleTabs or selectedTabId does not re-emit`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(ALL_TAB),
                selectedTabId = ALL_TAB.id,
            )
            val collector = ShelfSidebarSectionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val sectionsAfterFirstEmit = stateFlow.value.shelfSidebarSections

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

            // ----- Assert -----
            (stateFlow.value.shelfSidebarSections === sectionsAfterFirstEmit) shouldBe true
            job.cancel()
        }
    }
}
