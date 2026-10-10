package nl.rhaydus.softcover.core.uibinding.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipSize
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsRowUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * Maps the reader's custom lists onto the choose-lists sheet for a **single** book.
 *
 * The receiver is the reader's lists in the order to offer them; the built-in "Owned" list is dropped
 * here, since it is not one the reader files books onto.
 */
fun List<BookList>.toChooseListsUiModel(
    bookId: Int,
    bookTitle: String,
    cover: CoverUiModel,
    listsBeingMutated: Set<Int>,
): ChooseListsUiModel = ChooseListsUiModel(
    rows = rowsFor(
        bookIds = setOf(bookId),
        listsBeingMutated = listsBeingMutated,
        isBulk = false,
    ),
    scaffold = chooseListsScaffold(
        name = bookTitle,
        description = "Tap a list to file it here — tap a filled one to take it off. Saved as you go.",
        trailing = SheetHeaderTrailing.Jacket(cover = cover),
    ),
)

/**
 * Maps the reader's custom lists onto the choose-lists sheet for a **bulk selection**, where each
 * row's membership is tri-state across [bookIds] and the captions count the selection.
 *
 * [covers] are the selected books that actually resolved a cover, for the header stack, capped at
 * [SheetHeaderTrailing.JacketStack.MAX_COVERS]. **An empty list is a legitimate value** — the header
 * then draws one upright coverless jacket, so padding it up to one here would tilt that placeholder as
 * though it were a stack.
 */
fun List<BookList>.toBulkChooseListsUiModel(
    bookIds: Set<Int>,
    covers: List<CoverUiModel>,
    listsBeingMutated: Set<Int>,
): ChooseListsUiModel {
    val name = if (bookIds.size == 1) "1 book" else "${bookIds.size} books"
    val takesOff = if (bookIds.size == 1) "it" else "all ${bookIds.size}"
    val stackCovers = covers.take(SheetHeaderTrailing.JacketStack.MAX_COVERS)

    return ChooseListsUiModel(
        rows = rowsFor(
            bookIds = bookIds,
            listsBeingMutated = listsBeingMutated,
            isBulk = true,
        ),
        scaffold = chooseListsScaffold(
            name = name,
            description = "A part-filled list adds the rest first — then a second tap takes $takesOff off.",
            trailing = if (stackCovers.isEmpty()) {
                SheetHeaderTrailing.Jacket(
                    cover = CoverUiModel(
                        source = null,
                        coverlessTitle = name,
                        variant = CoverVariant.StackedJacket,
                    ),
                )
            } else {
                SheetHeaderTrailing.JacketStack(covers = stackCovers.toImmutableList())
            },
        ),
    )
}

private fun chooseListsScaffold(
    name: String,
    description: String,
    trailing: SheetHeaderTrailing,
): SheetScaffoldUiModel = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = "Lists",
            headline = persistentListOf(
                HeaderTextSegment.Plain(text = "Shelve "),
                HeaderTextSegment.Emphasised(text = name),
            ),
            description = persistentListOf(HeaderTextSegment.Plain(text = description)),
        ),
        trailing = trailing,
    ),
    layout = SheetLayout.PinnedLazy,
)

private fun List<BookList>.rowsFor(
    bookIds: Set<Int>,
    listsBeingMutated: Set<Int>,
    isBulk: Boolean,
): ImmutableList<ChooseListsRowUiModel> = filter { it.isOwned.not() }.map { list ->
    val listBookIds = list.books.mapTo(mutableSetOf()) { it.bookId }
    val matchingCount = bookIds.count { it in listBookIds }

    // Order matters: an empty selection has `matchingCount == bookIds.size == 0`, so the `ALL` arm
    // would claim it if it came first. The pre-migration helper guarded this with an early return.
    val membership = when {
        matchingCount == 0 -> ListMembership.NONE
        matchingCount == bookIds.size -> ListMembership.ALL
        else -> ListMembership.PARTIAL
    }

    ChooseListsRowUiModel(
        listId = list.id,
        name = list.name,
        caption = captionFor(
            bookCount = list.books.size,
            membership = membership,
            matchingCount = matchingCount,
            totalCount = bookIds.size,
            isBulk = isBulk,
        ),
        chip = membershipChipFor(
            listId = list.id,
            membership = membership,
            label = membershipChipLabelFor(
                membership = membership,
                matchingCount = matchingCount,
                totalCount = bookIds.size,
                isBulk = isBulk,
            ),
        ),
        membership = membership,
        isPending = list.id in listsBeingMutated,
    )
}.toImmutableList()

/** The chip's look is a function of membership alone; it is decorative, so the row's toggle announces state. */
private fun membershipChipFor(
    listId: Int,
    membership: ListMembership,
    label: String,
): ChipUiModel = ChipUiModel(
    key = listId.toString(),
    label = label,
    tone = when (membership) {
        ListMembership.ALL -> ChipTone.Container
        ListMembership.PARTIAL -> ChipTone.Filled
        ListMembership.NONE -> ChipTone.Outlined
    },
    leading = when (membership) {
        ListMembership.ALL -> null
        ListMembership.PARTIAL,
        ListMembership.NONE -> ChipLeading.Icon(icon = SoftcoverIcon.Add)
    },
    trailing = when (membership) {
        ListMembership.ALL -> ChipTrailing.Icon(icon = SoftcoverIcon.Close)
        ListMembership.PARTIAL,
        ListMembership.NONE -> null
    },
    interaction = ChipInteraction.Inert,
    size = ChipSize.Compact,
)

/**
 * "12 BOOKS" ("1 BOOK" for one) on its own for a single book; in bulk it appends the membership phrase:
 * `ALL` → "ALL {totalCount} HERE", `PARTIAL` → "{matchingCount} OF {totalCount} HERE", `NONE` → "NONE YET".
 * For example "12 BOOKS · 3 OF 5 HERE".
 */
private fun captionFor(
    bookCount: Int,
    membership: ListMembership,
    matchingCount: Int,
    totalCount: Int,
    isBulk: Boolean,
): String {
    val booksCount = if (bookCount == 1) "1 BOOK" else "$bookCount BOOKS"

    if (isBulk.not()) return booksCount

    val phrase = when (membership) {
        ListMembership.ALL -> "ALL $totalCount HERE"
        ListMembership.PARTIAL -> "$matchingCount OF $totalCount HERE"
        ListMembership.NONE -> "NONE YET"
    }

    return "$booksCount · $phrase"
}

/**
 * The trailing control's label. `PARTIAL` only ever arises across two or more books (one book is either
 * on a list or not), so its label needs no singular form.
 */
private fun membershipChipLabelFor(
    membership: ListMembership,
    matchingCount: Int,
    totalCount: Int,
    isBulk: Boolean,
): String {
    val isPlural = isBulk && totalCount > 1
    return when (membership) {
        ListMembership.ALL -> if (isPlural) "On all $totalCount" else "On the list"
        ListMembership.PARTIAL -> "Add the other ${totalCount - matchingCount}"
        ListMembership.NONE -> if (isPlural) "Add all $totalCount" else "Add"
    }
}
