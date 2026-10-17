package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShelfRearrangeRulesTest {
    @Nested
    inner class BookReorderStatusFor {
        @Test
        fun `returns the status of a Status tab on manual sort while rearranging`() {
            // ----- Arrange -----
            val tab = LibraryTab.Status.of(UserBookStatus.READ)

            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = tab,
                sortMode = LibrarySortMode.MANUAL,
                selectionMode = false,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe UserBookStatus.READ
        }

        @Test
        fun `returns null for the Did Not Finish tab`() {
            // ----- Arrange -----
            val tab = LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH)
            val sortMode = LibrarySortMode.MANUAL
            val selectionMode = false
            val isRearranging = true

            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = tab,
                sortMode = sortMode,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when the sort mode is not manual`() {
            // ----- Arrange -----
            val tab = LibraryTab.Status.of(UserBookStatus.READ)
            val sortMode = LibrarySortMode.TITLE
            val selectionMode = false
            val isRearranging = true

            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = tab,
                sortMode = sortMode,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null in selection mode`() {
            // ----- Arrange -----
            val tab = LibraryTab.Status.of(UserBookStatus.READ)
            val sortMode = LibrarySortMode.MANUAL
            val selectionMode = true
            val isRearranging = true

            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = tab,
                sortMode = sortMode,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when not rearranging`() {
            // ----- Arrange -----
            val tab = LibraryTab.Status.of(UserBookStatus.READ)
            val sortMode = LibrarySortMode.MANUAL
            val selectionMode = false
            val isRearranging = false

            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = tab,
                sortMode = sortMode,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null for the All tab`() {
            // ----- Arrange -----
            val tab = LibraryTab.All
            val sortMode = LibrarySortMode.MANUAL
            val selectionMode = false
            val isRearranging = true

            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = tab,
                sortMode = sortMode,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class IsEditionListRearranging {
        @Test
        fun `is true for a ranked list on order sort while rearranging`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.ORDER
            val isRanked = true
            val isRearranging = true

            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = sortMode,
                isRanked = isRanked,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when the sort mode is not order`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.TITLE
            val isRanked = true
            val isRearranging = true

            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = sortMode,
                isRanked = isRanked,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false for an unranked list`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.ORDER
            val isRanked = false
            val isRearranging = true

            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = sortMode,
                isRanked = isRanked,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when not rearranging`() {
            // ----- Arrange -----
            val sortMode = LibrarySortMode.ORDER
            val isRanked = true
            val isRearranging = false

            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = sortMode,
                isRanked = isRanked,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            result shouldBe false
        }
    }
}
