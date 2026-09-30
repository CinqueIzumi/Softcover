package nl.rhaydus.softcover.core.presentation.model

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTabTest {
    @Nested
    inner class IdFor {
        @Test
        fun `ids never collide across tab kinds`() {
            // ----- Arrange -----
            val statuses = UserBookStatus.entries
            val listId = 1

            // ----- Act -----
            val ids = statuses.map { LibraryTab.Status.idFor(status = it) } +
                LibraryTab.CustomList.idFor(listId = listId) +
                LibraryTab.All.id

            // ----- Assert -----
            ids.toSet().size shouldBe ids.size
        }
    }

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
        fun `returns the default sort mode for the All tab id`() {
            // ----- Arrange -----
            val allTabId = LibraryTab.All.id

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = allTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }

        @Test
        fun `returns the default sort mode for a non-Read status tab id`() {
            // ----- Arrange -----
            val wantToReadTabId = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ).id

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = wantToReadTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }

        @Test
        fun `returns the default sort mode for a custom-list tab id`() {
            // ----- Arrange -----
            val customListTabId = LibraryTab.CustomList.idFor(listId = 99)

            // ----- Act -----
            val result = LibraryTab.defaultSortMode(tabId = customListTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }
    }
}
