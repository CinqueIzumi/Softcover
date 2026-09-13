package nl.rhaydus.softcover.core.uibinding.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.lists.ChooseListsRowUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsVariant
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.domain.model.BookList

/** How many covers the bulk header's rotated stack can show. */
private const val MAX_STACKED_COVERS: Int = 3

/**
 * Maps the reader's custom lists onto the choose-lists sheet for a **single** book.
 *
 * The receiver is exactly the lists to offer, in the order to offer them — filtering (e.g. dropping
 * the built-in "Owned" list) is the host's call, not the mapping's.
 *
 * Promoted straight to `:core:uibinding` rather than starting feature-local (R6): it landed with two
 * consumers, `book_detail` here and `library`'s bulk selection below.
 */
fun List<BookList>.toChooseListsUiModel(
    bookId: Int,
    bookTitle: String,
    listsBeingMutated: Set<Int>,
): ChooseListsUiModel = ChooseListsUiModel(
    variant = ChooseListsVariant.SingleBook(name = bookTitle),
    rows = rowsFor(
        bookIds = setOf(bookId),
        listsBeingMutated = listsBeingMutated,
        isBulk = false,
    ),
)

/**
 * Maps the reader's custom lists onto the choose-lists sheet for a **bulk selection**, where each
 * row's membership is tri-state across [bookIds] and the captions count the selection.
 *
 * [coverCount] is how many of the selected books actually resolved a cover for the header stack,
 * capped at three. **Zero is a legitimate value** — the header then draws one upright coverless
 * jacket, so clamping it up to one here would tilt that placeholder as though it were a stack.
 */
fun List<BookList>.toBulkChooseListsUiModel(
    bookIds: Set<Int>,
    coverCount: Int,
    listsBeingMutated: Set<Int>,
): ChooseListsUiModel = ChooseListsUiModel(
    variant = ChooseListsVariant.ManyBooks(
        bookCount = bookIds.size,
        coverCount = coverCount.coerceIn(
            minimumValue = 0,
            maximumValue = MAX_STACKED_COVERS,
        ),
    ),
    rows = rowsFor(
        bookIds = bookIds,
        listsBeingMutated = listsBeingMutated,
        isBulk = true,
    ),
)

private fun List<BookList>.rowsFor(
    bookIds: Set<Int>,
    listsBeingMutated: Set<Int>,
    isBulk: Boolean,
): ImmutableList<ChooseListsRowUiModel> = map { list ->
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
        actionLabel = actionLabelFor(
            membership = membership,
            matchingCount = matchingCount,
            totalCount = bookIds.size,
            isBulk = isBulk,
        ),
        membership = membership,
        isPending = list.id in listsBeingMutated,
    )
}.toImmutableList()

/** "12 BOOKS" on its own for a single book; "12 BOOKS · 3 OF 5 HERE" once a selection is in play. */
private fun captionFor(
    bookCount: Int,
    membership: ListMembership,
    matchingCount: Int,
    totalCount: Int,
    isBulk: Boolean,
): String {
    val booksCount = "$bookCount BOOKS"

    if (isBulk.not()) return booksCount

    val phrase = when (membership) {
        ListMembership.ALL -> "ALL $totalCount HERE"
        ListMembership.PARTIAL -> "$matchingCount OF $totalCount HERE"
        ListMembership.NONE -> "NONE YET"
    }

    return "$booksCount · $phrase"
}

/**
 * The trailing control's label. `PARTIAL` only ever arises in bulk (a single book is either on a
 * list or not), so its label needs no single-book form.
 */
private fun actionLabelFor(
    membership: ListMembership,
    matchingCount: Int,
    totalCount: Int,
    isBulk: Boolean,
): String = when (membership) {
    ListMembership.ALL -> if (isBulk) "On all $totalCount" else "On the list"
    ListMembership.PARTIAL -> "Add the other ${totalCount - matchingCount}"
    ListMembership.NONE -> if (isBulk) "Add all $totalCount" else "Add"
}
