package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryEmptyStateMapperTest {
    @Nested
    inner class ToEmptyStateUiModel {
        @Test
        fun `did not finish tab gets copy distinct from the empty shelf copy`() {
            // ----- Arrange -----
            val dnfTab = LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH)
            val readTab = LibraryTab.Status.of(UserBookStatus.READ)

            // ----- Act -----
            val dnfResult = dnfTab.toEmptyStateUiModel()
            val readResult = readTab.toEmptyStateUiModel()

            // ----- Assert -----
            dnfResult.title shouldNotBe readResult.title
            dnfResult.body.orEmpty() shouldNotContain dnfTab.label
        }

        @Test
        fun `other status tab interpolates its label into the body`() {
            // ----- Arrange -----
            val tab = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ)

            // ----- Act -----
            val result = tab.toEmptyStateUiModel()

            // ----- Assert -----
            result.body.orEmpty() shouldContain "Want to Read"
        }

        @Test
        fun `custom list tab interpolates the list name into the body`() {
            // ----- Arrange -----
            val tab = LibraryTab.CustomList(
                listId = 7,
                listName = "Cosy mysteries",
            )

            // ----- Act -----
            val result = tab.toEmptyStateUiModel()

            // ----- Assert -----
            result.body.orEmpty() shouldContain "Cosy mysteries"
        }

        @Test
        fun `every non did not finish tab shares the same title`() {
            // ----- Arrange -----
            val tabs = listOf(
                LibraryTab.All,
                LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING),
                LibraryTab.CustomList(
                    listId = 1,
                    listName = "Favourites",
                ),
            )

            // ----- Act -----
            val titles = tabs.map { it.toEmptyStateUiModel().title }.toSet()

            // ----- Assert -----
            titles.size shouldBe 1
        }
    }
}
