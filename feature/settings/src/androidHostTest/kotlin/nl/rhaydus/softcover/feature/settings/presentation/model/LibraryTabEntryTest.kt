package nl.rhaydus.softcover.feature.settings.presentation.model

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTabEntryTest {
    @Nested
    inner class Id {
        @Test
        fun `All entry id equals the All tab id`() {
            // ----- Arrange -----
            val entry = LibraryTabEntry.All(totalCount = 3)

            // ----- Act -----
            val id = entry.id
            val allId = LibraryTabEntry.ALL_ID

            // ----- Assert -----
            id shouldBe LibraryTab.All.id
            allId shouldBe LibraryTab.All.id
        }

        @Test
        fun `Status entry id equals the matching Status tab id for every status`() {
            // ----- Arrange -----
            val entries = UserBookStatus.entries.map { status ->
                LibraryTabEntry.Status(
                    status = status,
                    statusLabel = "label",
                    statusCount = 1,
                )
            }
            val expectedIds = UserBookStatus.entries.map { LibraryTab.Status.of(it).id }

            // ----- Act -----
            val ids = entries.map { it.id }

            // ----- Assert -----
            ids shouldBe expectedIds
        }

        @Test
        fun `CustomList entry id equals the matching CustomList tab id`() {
            // ----- Arrange -----
            val entry = LibraryTabEntry.CustomList(
                listId = 42,
                listName = "Favourites",
                listCount = 5,
            )
            val tab = LibraryTab.CustomList(
                listId = 42,
                listName = "Favourites",
            )

            // ----- Act -----
            val id = entry.id

            // ----- Assert -----
            id shouldBe tab.id
        }
    }
}
