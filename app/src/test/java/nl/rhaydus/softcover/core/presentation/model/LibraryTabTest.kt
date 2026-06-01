package nl.rhaydus.softcover.core.presentation.model

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTabTest {

    @Nested
    inner class DefaultSortMode {

        @Test
        fun `returns DATE_FINISHED for the Read tab id`() {
            // ----- Arrange -----
            val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = readTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.DATE_FINISHED
        }

        @Test
        fun `returns DATE_ADDED for the All tab id`() {
            // ----- Arrange -----
            val allTabId = LibraryTab.All.id

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = allTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }

        @Test
        fun `returns DATE_ADDED for a non-Read status tab id`() {
            // ----- Arrange -----
            val wantToReadTabId = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ).id

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = wantToReadTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }

        @Test
        fun `returns DATE_ADDED for a custom-list tab id`() {
            // ----- Arrange -----
            val customListTabId = "list-99"

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = customListTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }
    }
}
