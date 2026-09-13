package nl.rhaydus.softcover.core.uibinding.lists

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.lists.ChooseListsVariant
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook

class ChooseListsMapperTest {
    private fun listBook(
        bookId: Int,
        listId: Int,
    ): ListBook = ListBook(
        listBookId = bookId,
        listId = listId,
        bookId = bookId,
        editionId = 1,
    )

    private fun bookList(
        id: Int,
        bookIds: List<Int>,
    ): BookList = BookList(
        id = id,
        name = "List $id",
        slug = "list-$id",
        books = bookIds.map { listBook(
            bookId = it,
            listId = id,
        ) },
    )

    @Nested
    inner class SingleBookMapping {
        @Test
        fun `subject is SingleBook with the given title`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = emptyList(),
            ),)

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.variant shouldBe ChooseListsVariant.SingleBook(name = "Piranesi")
        }

        @Test
        fun `caption is just the book count, with no membership suffix`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = (0 until 12).toList(),
            ),)

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "12 BOOKS"
        }

        @Test
        fun `actionLabel is On the list when the book is a member`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(42),
            ),)

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().actionLabel shouldBe "On the list"
        }

        @Test
        fun `actionLabel is Add when the book is not a member`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(99),
            ),)

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().actionLabel shouldBe "Add"
        }
    }

    @Nested
    inner class BulkMembershipBoundaries {
        @Test
        fun `membership is NONE when none of the selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(100, 101),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2, 3),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().membership shouldBe ListMembership.NONE
        }

        @Test
        fun `membership is PARTIAL when some of the selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(1, 2, 100),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2, 3),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().membership shouldBe ListMembership.PARTIAL
        }

        @Test
        fun `membership is ALL when every selected book is on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(1, 2, 3, 100),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2, 3),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().membership shouldBe ListMembership.ALL
        }
    }

    @Nested
    inner class BulkCaptions {
        @Test
        fun `caption reads ALL n HERE when every selected book is on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = (0 until 12).toList(),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "12 BOOKS · ALL 5 HERE"
        }

        @Test
        fun `caption reads m OF n HERE when some selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(0, 1, 2, 105),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "4 BOOKS · 3 OF 5 HERE"
        }

        @Test
        fun `caption reads NONE YET when no selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = (200 until 231).toList(),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "31 BOOKS · NONE YET"
        }
    }

    @Nested
    inner class BulkActionLabels {
        @Test
        fun `actionLabel is On all n when every selected book is on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(0, 1, 2, 3, 4),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().actionLabel shouldBe "On all 5"
        }

        @Test
        fun `actionLabel is Add the other n when some selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(0, 1, 2),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().actionLabel shouldBe "Add the other 2"
        }

        @Test
        fun `actionLabel is Add all n when no selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = listOf(100, 101),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.single().actionLabel shouldBe "Add all 5"
        }
    }

    @Nested
    inner class EmptySelection {
        @Test
        fun `every row is NONE when bookIds is empty`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(1, 2, 3),
                ),
                bookList(
                    id = 2,
                    bookIds = emptyList(),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = emptySet(),
                coverCount = 3,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.map { it.membership } shouldBe listOf(ListMembership.NONE, ListMembership.NONE)
        }
    }

    @Nested
    inner class CoverCountClamping {
        @Test
        fun `coverCount of 0 is preserved rather than clamped up`() {
            // ----- Arrange -----
            // Zero is legitimate: the header draws one upright coverless jacket for it. Clamping it
            // up to 1 would make the header treat that lone jacket as a tilted stack, which is the
            // regression this test guards against.
            val lists = listOf(bookList(
                id = 1,
                bookIds = emptyList(),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1),
                coverCount = 0,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            (result.variant as ChooseListsVariant.ManyBooks).coverCount shouldBe 0
        }

        @Test
        fun `negative coverCount is clamped up to 0`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = emptyList(),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1),
                coverCount = -1,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            (result.variant as ChooseListsVariant.ManyBooks).coverCount shouldBe 0
        }

        @Test
        fun `coverCount above 3 is clamped down to 3`() {
            // ----- Arrange -----
            val lists = listOf(bookList(
                id = 1,
                bookIds = emptyList(),
            ),)

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1),
                coverCount = 5,
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            (result.variant as ChooseListsVariant.ManyBooks).coverCount shouldBe 3
        }
    }

    @Nested
    inner class PendingMutations {
        @Test
        fun `isPending is true exactly for lists in listsBeingMutated`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
                bookList(
                    id = 2,
                    bookIds = emptyList(),
                ),
                bookList(
                    id = 3,
                    bookIds = emptyList(),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = setOf(2),
            )

            // ----- Assert -----
            result.rows.map { it.listId to it.isPending } shouldBe listOf(
                1 to false,
                2 to true,
                3 to false,
            )
        }
    }

    @Nested
    inner class RowOrder {
        @Test
        fun `rows are in the same order as the receiver`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 3,
                    bookIds = emptyList(),
                ),
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
                bookList(
                    id = 2,
                    bookIds = emptyList(),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows.map { it.listId } shouldBe listOf(3, 1, 2)
        }

        @Test
        fun `rows is empty for an empty receiver`() {
            // ----- Arrange -----
            val lists = emptyList<BookList>()

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                listsBeingMutated = emptySet(),
            )

            // ----- Assert -----
            result.rows shouldBe emptyList()
        }
    }
}
