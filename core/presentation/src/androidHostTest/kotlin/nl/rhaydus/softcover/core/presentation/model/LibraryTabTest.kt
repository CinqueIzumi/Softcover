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
        fun `returns the default sort mode for every other tab id`() {
            // ----- Arrange -----
            val otherTabIds = listOf(
                LibraryTab.All.id,
                LibraryTab.Status.of(UserBookStatus.WANT_TO_READ).id,
                LibraryTab.CustomList.idFor(listId = 99),
            )

            // ----- Act -----
            val results = otherTabIds.map { LibraryTab.defaultSortMode(tabId = it) }

            // ----- Assert -----
            results.toSet() shouldBe setOf(LibrarySortMode.Default)
        }
    }
}
