package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ArrangeDraftChipsSnapshotTest {
    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)

    @Nested
    inner class Compute {
        @Test
        fun `compute returns the shared Empty result while the draft is null (sheet closed)`() {
            // ----- Arrange -----
            val snapshot = ArrangeDraftChipsSnapshot(
                draft = null,
                visibleTabs = persistentListOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Act -----
            val result = snapshot.compute()

            // ----- Assert -----
            result shouldBe LibraryArrangeChips.Empty
        }

        @Test
        fun `compute returns the shared Empty result when the draft's tabId matches no visible tab`() {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = "missing-tab",
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            val snapshot = ArrangeDraftChipsSnapshot(
                draft = draft,
                visibleTabs = persistentListOf(readingTab),
                customLists = persistentListOf(),
                booksByTab = persistentMapOf(),
                deadlines = persistentMapOf(),
            )

            // ----- Act -----
            val result = snapshot.compute()

            // ----- Assert -----
            result shouldBe LibraryArrangeChips.Empty
        }
    }
}
