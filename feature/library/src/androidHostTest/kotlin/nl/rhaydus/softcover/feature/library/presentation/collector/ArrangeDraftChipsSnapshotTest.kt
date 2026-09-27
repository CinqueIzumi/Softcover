package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ArrangeDraftChipsSnapshotTest {
    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)

    @Nested
    inner class Compute {
        @Test
        fun `compute returns empty layout and sort chip lists while the draft is null (sheet closed)`() {
            // ----- Arrange -----
            val snapshot = ArrangeDraftChipsSnapshot(
                draft = null,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Act -----
            val (layoutChips, sortChips) = snapshot.compute()

            // ----- Assert -----
            layoutChips shouldBe emptyList()
            sortChips shouldBe emptyList()
        }

        @Test
        fun `compute delegates to buildLibraryArrangeChips once the draft is present`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = readingTab.id,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            val snapshot = ArrangeDraftChipsSnapshot(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Act -----
            val (layoutChips, sortChips) = snapshot.compute()

            // ----- Assert -----
            val layoutChip = layoutChips.first { it.key == "layout:GRID_TWO" }
            layoutChip.tone shouldBe ChipTone.Choice
            layoutChip.selected shouldBe true
            val titleChip = sortChips.first { it.key == "sort:TITLE" }
            titleChip.tone shouldBe ChipTone.Choice
            titleChip.selected shouldBe true
            titleChip.trailing shouldBe ChipTrailing.Icon(
                icon = SoftcoverIcon.ArrowDropUp,
                description = "Ascending",
            )
        }

        @Test
        fun `compute returns empty chip lists when the draft's tabId matches no visible tab`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = "missing-tab",
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            val snapshot = ArrangeDraftChipsSnapshot(
                draft = draft,
                visibleTabs = listOf(readingTab),
                customLists = emptyList(),
                booksByTab = emptyMap(),
                deadlines = emptyMap(),
            )

            // ----- Act -----
            val (layoutChips, sortChips) = snapshot.compute()

            // ----- Assert -----
            layoutChips shouldBe emptyList()
            sortChips shouldBe emptyList()
        }
    }
}
