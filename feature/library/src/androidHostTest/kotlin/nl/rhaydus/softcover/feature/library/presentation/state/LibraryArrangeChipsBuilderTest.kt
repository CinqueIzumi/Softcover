package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.chip.ChipVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

class LibraryArrangeChipsBuilderTest {
    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)

    @Nested
    inner class BuildLibraryArrangeChips {
        @Test
        fun `layout chips mark only the draft's grid layout bucket as selected`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS,
            )

            // ----- Act -----
            val (layoutChips, _) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            layoutChips.map { it.key } shouldBe listOf("layout:GRID_TWO", "layout:GRID_THREE", "layout:LIST")
            layoutChips.map { it.label } shouldBe listOf("Grid · 2", "Grid · 3", "List")
            layoutChips.map { (it.variant as ChipVariant.Choice).selected } shouldBe listOf(false, true, false)
        }

        @Test
        fun `sort chip for the draft's active mode is selected with a trailing direction icon`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (_, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            sortChips.single { it.key == "sort:TITLE" }.variant shouldBe
                ChipVariant.Choice(
                    selected = true,
                    trailingIcon = SoftcoverIcon.ArrowDropUp,
                )
        }

        @Test
        fun `descending direction renders the down-facing trailing icon on the active sort chip`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (_, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            sortChips.single { it.key == "sort:TITLE" }.variant shouldBe
                ChipVariant.Choice(
                    selected = true,
                    trailingIcon = SoftcoverIcon.ArrowDropDown,
                )
        }

        @Test
        fun `a positional sort mode never gets a trailing direction icon even when active`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.MANUAL,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (_, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            sortChips.single { it.key == "sort:MANUAL" }.variant shouldBe ChipVariant.Choice(selected = true)
        }

        @Test
        fun `inactive sort chips are unselected with no trailing icon`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (_, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            sortChips.single { it.key == "sort:AUTHOR" }.variant shouldBe ChipVariant.Choice(selected = false)
        }

        @Test
        fun `sort chips are delegated to librarySortOptions, offering ORDER first for a ranked custom list`() {
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
            val draft = LibraryArrangeDraft(
                tabId = customListTab.id,
                sortMode = LibrarySortMode.ORDER,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (_, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(customListTab),
                customLists = listOf(rankedList),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            sortChips.first().key shouldBe "sort:ORDER"
            sortChips.first().variant shouldBe ChipVariant.Choice(selected = true)
        }

        @Test
        fun `a draft tabId absent from visibleTabs yields no chips`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = "missing-tab",
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (layoutChips, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            layoutChips shouldBe emptyList()
            sortChips shouldBe emptyList()
        }

        @Test
        fun `an empty visibleTabs list yields no chips`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val (layoutChips, sortChips) = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = emptyList(),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            layoutChips shouldBe emptyList()
            sortChips shouldBe emptyList()
        }
    }

    @Nested
    inner class LibraryLayoutChipForKey {
        @Test
        fun `every layout chip key round-trips back to its LibraryLayoutChip`() {
            // ----- Act & Assert -----
            LibraryLayoutChip.entries.forEach { chip ->
                libraryLayoutChipForKey(key = chip.arrangeChipKey) shouldBe chip
            }
        }

        @Test
        fun `an unknown key resolves to null`() {
            // ----- Act -----
            val result = libraryLayoutChipForKey(key = "layout:UNKNOWN")

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class LibrarySortModeForKey {
        @Test
        fun `every sort chip key round-trips back to its LibrarySortMode`() {
            // ----- Act & Assert -----
            LibrarySortMode.entries.forEach { mode ->
                librarySortModeForKey(key = mode.arrangeChipKey) shouldBe mode
            }
        }

        @Test
        fun `an unknown key resolves to null`() {
            // ----- Act -----
            val result = librarySortModeForKey(key = "sort:UNKNOWN")

            // ----- Assert -----
            result shouldBe null
        }
    }
}
