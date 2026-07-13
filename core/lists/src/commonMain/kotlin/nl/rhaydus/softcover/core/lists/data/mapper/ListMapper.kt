package nl.rhaydus.softcover.core.lists.data.mapper

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import nl.rhaydus.softcover.fragment.ListFragment.List_book.Companion.listBookFragment

internal fun ListFragment.toBookList(): BookList {
    val listBooks = list_books.mapNotNull { it.listBookFragment()?.toListBook() }

    return BookList(
        id = id,
        name = name,
        slug = slug.orEmpty(),
        ranked = ranked == true,
        books = listBooks,
        signature = buildListSignature(
            updatedAt = updated_at,
            count = list_books_aggregate.aggregate?.count ?: 0,
            maxListBookUpdatedAt = list_books_aggregate.aggregate?.max?.updated_at,
        ),
    )
}

/**
 * Folds a list's freshness inputs into one comparable string. Includes the list's own `updated_at`
 * (metadata edits), its book [count] (adds/removes), and the newest `list_books.updated_at`
 * ([maxListBookUpdatedAt], for edits/reorders) so any content change advances the signature even
 * when the list's own `updated_at` does not. Built identically here and from the lightweight
 * signature query so the two always compare equal for an unchanged list.
 */
internal fun buildListSignature(
    updatedAt: String?,
    count: Int,
    maxListBookUpdatedAt: String?,
): String = "${updatedAt.orEmpty()}|$count|${maxListBookUpdatedAt.orEmpty()}"

internal fun ListBookFragment.toListBook(): ListBook? {
    val editionId = edition_id ?: return null

    return ListBook(
        listBookId = id,
        listId = list_id,
        bookId = book_id,
        editionId = editionId,
        position = position,
        addedAt = created_at,
    )
}
