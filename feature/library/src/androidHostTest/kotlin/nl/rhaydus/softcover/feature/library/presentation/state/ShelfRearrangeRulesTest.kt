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
            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH),
                sortMode = LibrarySortMode.MANUAL,
                selectionMode = false,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when the sort mode is not manual`() {
            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = LibraryTab.Status.of(UserBookStatus.READ),
                sortMode = LibrarySortMode.TITLE,
                selectionMode = false,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null in selection mode`() {
            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = LibraryTab.Status.of(UserBookStatus.READ),
                sortMode = LibrarySortMode.MANUAL,
                selectionMode = true,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when not rearranging`() {
            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = LibraryTab.Status.of(UserBookStatus.READ),
                sortMode = LibrarySortMode.MANUAL,
                selectionMode = false,
                isRearranging = false,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null for the All tab`() {
            // ----- Act -----
            val result = bookReorderStatusFor(
                tab = LibraryTab.All,
                sortMode = LibrarySortMode.MANUAL,
                selectionMode = false,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class IsEditionListRearranging {
        @Test
        fun `is true for a ranked list on order sort while rearranging`() {
            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = LibrarySortMode.ORDER,
                isRanked = true,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when the sort mode is not order`() {
            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = LibrarySortMode.TITLE,
                isRanked = true,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false for an unranked list`() {
            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = LibrarySortMode.ORDER,
                isRanked = false,
                isRearranging = true,
            )

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when not rearranging`() {
            // ----- Act -----
            val result = isEditionListRearranging(
                sortMode = LibrarySortMode.ORDER,
                isRanked = true,
                isRearranging = false,
            )

            // ----- Assert -----
            result shouldBe false
        }
    }
}
