package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ArrangeSheetSnapshotTest {
    private fun draft(layout: LibraryGridLayout) = LibraryArrangeDraft(
        tabId = LibraryTab.All.id,
        sortMode = LibrarySortMode.TITLE,
        sortDirection = SortDirection.ASCENDING,
        gridLayout = layout,
    )

    private fun list(
        id: Int,
        ranked: Boolean,
    ): BookList = mockk<BookList>().also {
        every {
            it.id
        } returns id
        every {
            it.ranked
        } returns ranked
    }

    @Nested
    inner class TitlesToggle {
        @Test
        fun `is null without an arrange draft`() {
            // ----- Arrange -----
            val state = LibraryUiState(arrangeDraft = null)

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.titlesToggle.shouldBeNull()
        }

        @Test
        fun `is null for a list layout`() {
            // ----- Arrange -----
            val state = LibraryUiState(arrangeDraft = draft(LibraryGridLayout.LIST_COMPACT))

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.titlesToggle.shouldBeNull()
        }

        @Test
        fun `is checked for a grid layout that shows titles`() {
            // ----- Arrange -----
            val state = LibraryUiState(arrangeDraft = draft(LibraryGridLayout.GRID_TWO_COLUMNS))

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.titlesToggle.shouldNotBeNull().toggle.checked shouldBe true
        }

        @Test
        fun `is unchecked for a cover-only grid layout`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                arrangeDraft = draft(LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY),
            )

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.titlesToggle.shouldNotBeNull().toggle.checked shouldBe false
        }
    }

    @Nested
    inner class MakeOrdered {
        @Test
        fun `is non-null for the selected unranked list and carries its id`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                selectedTabId = LibraryTab.CustomList.idFor(listId = 7),
                customLists = listOf(
                    list(
                        id = 7,
                        ranked = false,
                    ),
                ),
            )

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.makeOrdered.shouldNotBeNull().listId shouldBe 7
        }

        @Test
        fun `is null for a ranked list`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                selectedTabId = LibraryTab.CustomList.idFor(listId = 7),
                customLists = listOf(
                    list(
                        id = 7,
                        ranked = true,
                    ),
                ),
            )

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.makeOrdered.shouldBeNull()
        }

        @Test
        fun `is null when the selected tab is not a custom list`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                selectedTabId = LibraryTab.All.id,
                customLists = listOf(
                    list(
                        id = 7,
                        ranked = false,
                    ),
                ),
            )

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.makeOrdered.shouldBeNull()
        }

        @Test
        fun `ignores an unranked list that is not the selected one`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                selectedTabId = LibraryTab.CustomList.idFor(listId = 8),
                customLists = listOf(
                    list(
                        id = 7,
                        ranked = false,
                    ),
                    list(
                        id = 8,
                        ranked = true,
                    ),
                ),
            )

            // ----- Act -----
            val snapshot = ArrangeSheetSnapshot.from(state = state)

            // ----- Assert -----
            snapshot.makeOrdered.shouldBeNull()
        }
    }
}
