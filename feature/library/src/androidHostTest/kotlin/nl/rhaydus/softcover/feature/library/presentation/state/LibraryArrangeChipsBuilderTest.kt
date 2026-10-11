package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result.layoutChips.chips.map { it.key } shouldBe listOf("layout:GRID_TWO", "layout:GRID_THREE", "layout:LIST")
            result.layoutChips.chips.map { it.selected } shouldBe listOf(false, true, false)
        }

        @Test
        fun `sort header names the sort row after the draft's resolved tab label`() {
            // ----- Arrange -----
            val customListTab = LibraryTab.CustomList(
                listId = 10,
                listName = "Winter reading",
            )
            val draft = LibraryArrangeDraft(
                tabId = customListTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(customListTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result.sortHeader shouldBe SectionHeaderUiModel.Label(eyebrow = "Sort by · Winter reading")
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
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            val titleChip = result.sortChips.chips.single { it.key == "sort:TITLE" }
            titleChip.selected shouldBe true
            titleChip.trailing shouldBe ChipTrailing.Icon(
                icon = SoftcoverIcon.ArrowDropUp,
                description = "Ascending",
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
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            val titleChip = result.sortChips.chips.single { it.key == "sort:TITLE" }
            titleChip.selected shouldBe true
            titleChip.trailing shouldBe ChipTrailing.Icon(
                icon = SoftcoverIcon.ArrowDropDown,
                description = "Descending",
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
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            val manualChip = result.sortChips.chips.single { it.key == "sort:MANUAL" }
            manualChip.selected shouldBe true
            manualChip.trailing shouldBe null
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
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            val authorChip = result.sortChips.chips.single { it.key == "sort:AUTHOR" }
            authorChip.selected shouldBe false
            authorChip.trailing shouldBe null
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
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(customListTab),
                customLists = persistentListOf(rankedList),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result.sortChips.chips.first().key shouldBe "sort:ORDER"
            result.sortChips.chips.first().selected shouldBe true
        }

        @Test
        fun `a draft tabId absent from visibleTabs yields the shared Empty result`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = "missing-tab",
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result shouldBe LibraryArrangeChips.Empty
        }

        @Test
        fun `an empty visibleTabs list yields the shared Empty result`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = emptyList(),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result shouldBe LibraryArrangeChips.Empty
        }
    }

    @Nested
    inner class ChipPayloads {
        @Test
        fun `every layout chip key resolves to its LibraryLayoutChip`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result.layoutChips.chips.map { result.layoutChips[it.key] } shouldBe LibraryLayoutChip.entries
        }

        @Test
        fun `every sort chip key resolves to its LibrarySortMode`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result.sortChips.chips.forEach { result.sortChips[it.key]?.arrangeChipKey shouldBe it.key }
        }

        @Test
        fun `a key no chip carries resolves to no payload in either set`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            val result = buildLibraryArrangeChips(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            result.layoutChips["layout:BOGUS"] shouldBe null
            result.sortChips["sort:BOGUS"] shouldBe null
        }
    }
}
