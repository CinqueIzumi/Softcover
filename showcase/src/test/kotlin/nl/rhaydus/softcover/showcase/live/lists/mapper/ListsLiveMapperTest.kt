package nl.rhaydus.softcover.showcase.live.lists.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.lists.ChooseListsRowUiModel
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.showcase.live.lists.model.ListsDemo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsLiveMapperTest {
    private fun row(
        rows: List<ChooseListsRowUiModel>,
        listId: Int,
    ): ChooseListsRowUiModel = rows.firstOrNull { it.listId == listId }.shouldNotBeNull()

    @Nested
    inner class TogglingMembership {
        @Test
        fun `puts a list the book is not on onto it`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.ONE_BOOK)

            // ----- Act -----
            val result = row(
                rows = model.togglingMembership(listId = 2).rows,
                listId = 2,
            )

            // ----- Assert -----
            result.membership shouldBe ListMembership.ALL
            result.chip.label shouldBe "On the list"
        }

        @Test
        fun `takes the book off a list that holds it`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.ONE_BOOK)

            // ----- Act -----
            val result = row(
                rows = model.togglingMembership(listId = 1).rows,
                listId = 1,
            )

            // ----- Assert -----
            result.membership shouldBe ListMembership.NONE
            result.chip.label shouldBe "Add"
        }

        @Test
        fun `adds the rest of the books to a partly filled list`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.SEVERAL_BOOKS)

            // ----- Act -----
            val result = row(
                rows = model.togglingMembership(listId = 1).rows,
                listId = 1,
            )

            // ----- Assert -----
            result.membership shouldBe ListMembership.ALL
            result.chip.label shouldBe "On the list"
        }

        @Test
        fun `returns a list to its first state when toggled twice`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.ONE_BOOK)

            // ----- Act -----
            val result = model
                .togglingMembership(listId = 2)
                .togglingMembership(listId = 2)

            // ----- Assert -----
            row(
                rows = result.rows,
                listId = 2,
            ).membership shouldBe ListMembership.NONE
        }

        @Test
        fun `leaves the other lists as they were`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.ONE_BOOK)

            // ----- Act -----
            val result = model.togglingMembership(listId = 2)

            // ----- Assert -----
            result.rows.filter { it.listId != 2 } shouldContainExactly model.rows.filter { it.listId != 2 }
        }

        @Test
        fun `changes nothing for an unknown list id`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.ONE_BOOK)

            // ----- Act -----
            val result = model.togglingMembership(listId = 999)

            // ----- Assert -----
            result.rows shouldContainExactly model.rows
        }

        @Test
        fun `changes nothing when there are no lists`() {
            // ----- Arrange -----
            val model = listsDemoModel(demo = ListsDemo.NO_LISTS)

            // ----- Act -----
            val result = model.togglingMembership(listId = 1)

            // ----- Assert -----
            result.rows shouldBe emptyList()
        }
    }
}
