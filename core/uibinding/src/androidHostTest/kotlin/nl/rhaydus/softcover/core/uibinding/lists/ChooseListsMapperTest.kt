package nl.rhaydus.softcover.core.uibinding.lists

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChooseListsMapperTest {
    private fun coverUiModel(): CoverUiModel = CoverUiModel(
        source = null,
        coverlessTitle = "Cover",
        variant = CoverVariant.StackedJacket,
    )

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
        books = bookIds.map {
            listBook(
                bookId = it,
                listId = id,
            )
        },
    )

    @Nested
    inner class SingleBookMapping {
        @Test
        fun `header shelves the book with the title and a jacket of its cover`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
            )
            val cover = coverUiModel()

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = cover,
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            val header = result.scaffold.header.shouldNotBeNull()
            header.section.headline shouldBe persistentListOf(
                HeaderTextSegment.Plain(text = "Shelve "),
                HeaderTextSegment.Emphasised(text = "Piranesi"),
            )
            header.trailing shouldBe SheetHeaderTrailing.Jacket(cover = cover)
        }

        @Test
        fun `caption is just the book count, with no membership suffix`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = (0 until 12).toList(),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "12 BOOKS"
        }

        @Test
        fun `membership chip label is On the list when the book is a member`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(42),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().chip.label shouldBe "On the list"
        }

        @Test
        fun `membership chip label is Add when the book is not a member`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(99),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().chip.label shouldBe "Add"
        }
    }

    @Nested
    inner class BulkMembershipBoundaries {
        @Test
        fun `membership is NONE when none of the selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(100, 101),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2, 3),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().membership shouldBe ListMembership.NONE
        }

        @Test
        fun `membership is PARTIAL when some of the selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(1, 2, 100),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2, 3),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().membership shouldBe ListMembership.PARTIAL
        }

        @Test
        fun `membership is ALL when every selected book is on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(1, 2, 3, 100),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2, 3),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
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
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = (0 until 12).toList(),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "12 BOOKS · ALL 5 HERE"
        }

        @Test
        fun `caption reads m OF n HERE when some selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(0, 1, 2, 105),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "4 BOOKS · 3 OF 5 HERE"
        }

        @Test
        fun `caption reads NONE YET when no selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = (200 until 231).toList(),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "31 BOOKS · NONE YET"
        }
    }

    @Nested
    inner class BulkMembershipChipLabels {
        @Test
        fun `membership chip label is On all n when every selected book is on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(0, 1, 2, 3, 4),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().chip.label shouldBe "On all 5"
        }

        @Test
        fun `membership chip label is Add the other n when some selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(0, 1, 2),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().chip.label shouldBe "Add the other 2"
        }

        @Test
        fun `membership chip label is Add all n when no selected books are on the list`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(100, 101),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(0, 1, 2, 3, 4),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().chip.label shouldBe "Add all 5"
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
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.map { it.membership } shouldBe listOf(ListMembership.NONE, ListMembership.NONE)
        }
    }

    @Nested
    inner class CoversCapping {
        @Test
        fun `covers above 3 are capped down to the first 3`() {
            // ----- Arrange -----
            val covers = List(5) { coverUiModel() }
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1),
                covers = covers,
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            val trailing = result.scaffold.header
                .shouldNotBeNull()
                .trailing as SheetHeaderTrailing.JacketStack
            trailing.covers.toList() shouldBe covers.take(3)
        }

        @Test
        fun `an empty covers list is preserved rather than padded`() {
            // ----- Arrange -----
            // Empty is legitimate: the header draws one upright coverless jacket for it. Padding it
            // up to one cover would make the header treat that lone jacket as a tilted stack, which
            // is the regression this test guards against.
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
            )

            // ----- Act -----
            val result = lists.toBulkChooseListsUiModel(
                bookIds = setOf(1),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            val trailing = result.scaffold.header
                .shouldNotBeNull()
                .trailing as SheetHeaderTrailing.Jacket
            trailing.cover shouldBe CoverUiModel(
                source = null,
                coverlessTitle = "1 book",
                variant = CoverVariant.StackedJacket,
            )
        }

        @Test
        fun `covers within the cap become a stack of exactly those covers`() {
            // ----- Arrange -----
            val covers = List(2) { coverUiModel() }

            // ----- Act -----
            val result = emptyList<BookList>().toBulkChooseListsUiModel(
                bookIds = setOf(1, 2),
                covers = covers,
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            val trailing = result.scaffold.header
                .shouldNotBeNull()
                .trailing as SheetHeaderTrailing.JacketStack
            trailing.covers.toList() shouldBe covers
        }

        @Test
        fun `bulk header shelves the counted books`() {
            // ----- Arrange -----
            val bookIds = setOf(1, 2, 3)
            val covers = emptyList<CoverUiModel>()
            val listsBeingMutated = emptySet<Int>()

            // ----- Act -----
            val result = emptyList<BookList>().toBulkChooseListsUiModel(
                bookIds = bookIds,
                covers = covers,
                listsBeingMutated = listsBeingMutated,
                previous = null,
            )

            // ----- Assert -----
            val section = result.scaffold.header
                .shouldNotBeNull()
                .section
            section.headline shouldBe persistentListOf(
                HeaderTextSegment.Plain(text = "Shelve "),
                HeaderTextSegment.Emphasised(text = "3 books"),
            )
        }
    }

    @Nested
    inner class BulkCopy {
        private fun descriptionOf(count: Int): String {
            val result = emptyList<BookList>().toBulkChooseListsUiModel(
                bookIds = (1..count).toSet(),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            return result.scaffold.header
                .shouldNotBeNull()
                .section.description
                .shouldNotBeNull()
                .joinToString(separator = "") { it.text }
        }

        @Test
        fun `a single selected book is named as 1 book and removed as it`() {
            // ----- Arrange -----
            val bookIds = setOf(7)
            val covers = emptyList<CoverUiModel>()
            val listsBeingMutated = emptySet<Int>()

            // ----- Act -----
            val result = emptyList<BookList>().toBulkChooseListsUiModel(
                bookIds = bookIds,
                covers = covers,
                listsBeingMutated = listsBeingMutated,
                previous = null,
            )

            // ----- Assert -----
            result.scaffold.header.shouldNotBeNull().section.headlineText shouldBe "Shelve 1 book"
            descriptionOf(count = 1) shouldContain "removes it."
        }

        private fun chipLabelOf(
            selectedCount: Int,
            onListCount: Int,
        ): String {
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = (1..onListCount).toList(),
                ),
            )

            return lists.toBulkChooseListsUiModel(
                bookIds = (1..selectedCount).toSet(),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            ).rows.single().chip.label
        }

        @Test
        fun `a single selected book on the list gets the singular On the list chip`() {
            // ----- Arrange -----
            val selectedCount = 1
            val onListCount = 1

            // ----- Act -----
            val label = chipLabelOf(
                selectedCount = selectedCount,
                onListCount = onListCount,
            )

            // ----- Assert -----
            label shouldBe "On the list"
        }

        @Test
        fun `a single selected book off the list gets the singular Add chip`() {
            // ----- Arrange -----
            val selectedCount = 1
            val onListCount = 0

            // ----- Act -----
            val label = chipLabelOf(
                selectedCount = selectedCount,
                onListCount = onListCount,
            )

            // ----- Assert -----
            label shouldBe "Add"
        }

        @Test
        fun `a selection of 5 books removes all 5, never all three`() {
            // ----- Arrange -----
            val selectedCount = 5

            // ----- Act -----
            val description = descriptionOf(count = selectedCount)

            // ----- Assert -----
            description shouldContain "removes all 5."
            description shouldNotContain "three"
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
                cover = coverUiModel(),
                listsBeingMutated = setOf(2),
                previous = null,
            )

            // ----- Assert -----
            result.rows.map { it.listId to it.isPending } shouldBe listOf(
                1 to false,
                2 to true,
                3 to false,
            )
        }

        @Test
        fun `a pending list keeps its previous single-book row even though the data has since changed`() {
            // ----- Arrange -----
            val before = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
            ).toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )
            val after = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(42, 43),
                ),
            )

            // ----- Act -----
            val result = after.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = setOf(1),
                previous = before,
            )

            // ----- Assert -----
            result.rows.single() shouldBe before.rows.single().copy(isPending = true)
            result.rows.single().membership shouldBe ListMembership.NONE
        }

        @Test
        fun `a pending bulk list keeps its previous row while the write is half done`() {
            // ----- Arrange -----
            val before = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
            ).toBulkChooseListsUiModel(
                bookIds = setOf(1, 2),
                covers = emptyList(),
                listsBeingMutated = emptySet(),
                previous = null,
            )
            val halfWritten = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(1),
                ),
            )

            // ----- Act -----
            val result = halfWritten.toBulkChooseListsUiModel(
                bookIds = setOf(1, 2),
                covers = emptyList(),
                listsBeingMutated = setOf(1),
                previous = before,
            )

            // ----- Assert -----
            result.rows.single() shouldBe before.rows.single().copy(isPending = true)
            result.rows.single().membership shouldBe ListMembership.NONE
            result.rows.single().caption shouldContain "NONE YET"
        }

        @Test
        fun `a pending list with no previous row is mapped fresh from the data`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(42),
                ),
                bookList(
                    id = 2,
                    bookIds = emptyList(),
                ),
            )
            val previous = listOf(
                bookList(
                    id = 2,
                    bookIds = emptyList(),
                ),
            ).toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = setOf(1),
                previous = previous,
            )

            // ----- Assert -----
            val fresh = result.rows.first { it.listId == 1 }
            fresh.isPending shouldBe true
            fresh.membership shouldBe ListMembership.ALL
        }

        @Test
        fun `a list that is no longer pending ignores the previous row and shows its final state`() {
            // ----- Arrange -----
            val before = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ),
            ).toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = setOf(1),
                previous = null,
            )
            val finished = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(42),
                ),
            )

            // ----- Act -----
            val result = finished.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = before,
            )

            // ----- Assert -----
            result.rows.single().isPending shouldBe false
            result.rows.single().membership shouldBe ListMembership.ALL
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
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
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
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows shouldBe emptyList()
        }
    }

    @Nested
    inner class OwnedListFilter {
        @Test
        fun `the built-in Owned list is dropped from the rows`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = emptyList(),
                ).copy(slug = "owned"),
                bookList(
                    id = 2,
                    bookIds = emptyList(),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.map { it.listId } shouldBe listOf(2)
        }
    }

    @Nested
    inner class SingularCaption {
        @Test
        fun `a list holding one book reads 1 BOOK`() {
            // ----- Arrange -----
            val lists = listOf(
                bookList(
                    id = 1,
                    bookIds = listOf(9),
                ),
            )

            // ----- Act -----
            val result = lists.toChooseListsUiModel(
                bookId = 42,
                bookTitle = "Piranesi",
                cover = coverUiModel(),
                listsBeingMutated = emptySet(),
                previous = null,
            )

            // ----- Assert -----
            result.rows.single().caption shouldBe "1 BOOK"
        }
    }

    @Nested
    inner class MembershipChip {
        private fun chipFor(
            listBookIds: List<Int>,
            selection: Set<Int>,
            listId: Int = 7,
        ): ChipUiModel = listOf(
            bookList(
                id = listId,
                bookIds = listBookIds,
            ),
        ).toBulkChooseListsUiModel(
            bookIds = selection,
            covers = emptyList(),
            listsBeingMutated = emptySet(),
            previous = null,
        ).rows.single().chip

        @Test
        fun `ALL is a container chip with a close trailing icon and no leading`() {
            // ----- Arrange -----
            val listBookIds = listOf(1, 2)
            val selection = setOf(1, 2)

            // ----- Act -----
            val chip = chipFor(
                listBookIds = listBookIds,
                selection = selection,
            )

            // ----- Assert -----
            chip.tone shouldBe ChipTone.Container
            chip.leading.shouldBeNull()
            chip.trailing shouldBe ChipTrailing.Icon(icon = SoftcoverIcon.Close)
        }

        @Test
        fun `PARTIAL is a filled chip with an add leading icon and no trailing`() {
            // ----- Arrange -----
            val listBookIds = listOf(1)
            val selection = setOf(1, 2)

            // ----- Act -----
            val chip = chipFor(
                listBookIds = listBookIds,
                selection = selection,
            )

            // ----- Assert -----
            chip.tone shouldBe ChipTone.Filled
            chip.leading shouldBe ChipLeading.Icon(icon = SoftcoverIcon.Add)
            chip.trailing.shouldBeNull()
        }

        @Test
        fun `NONE is an outlined chip with an add leading icon and no trailing`() {
            // ----- Arrange -----
            val listBookIds = emptyList<Int>()
            val selection = setOf(1, 2)

            // ----- Act -----
            val chip = chipFor(
                listBookIds = listBookIds,
                selection = selection,
            )

            // ----- Assert -----
            chip.tone shouldBe ChipTone.Outlined
            chip.leading shouldBe ChipLeading.Icon(icon = SoftcoverIcon.Add)
            chip.trailing.shouldBeNull()
        }

        @Test
        fun `the chip is keyed by its list id`() {
            // ----- Arrange -----
            val listId = 123

            // ----- Act -----
            val chip = chipFor(
                listBookIds = emptyList(),
                selection = setOf(1),
                listId = listId,
            )

            // ----- Assert -----
            chip.key shouldBe "123"
        }

        @Test
        fun `the chip is decorative so it is inert`() {
            // ----- Arrange -----
            val selection = setOf(1)

            // ----- Act -----
            val chip = chipFor(
                listBookIds = listOf(1),
                selection = selection,
            )

            // ----- Assert -----
            chip.interaction shouldBe ChipInteraction.Inert
        }
    }
}
