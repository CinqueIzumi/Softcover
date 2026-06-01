package nl.rhaydus.softcover.core.lists.data.mapper

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import nl.rhaydus.softcover.fragment.ListFragment.List_book.Companion.listBookFragment

fun ListFragment.toBookList(): BookList {
    val listBooks = list_books.mapNotNull { it.listBookFragment()?.toListBook() }

    return BookList(
        id = id,
        name = name,
        slug = slug ?: "",
        ranked = ranked == true,
        books = listBooks,
    )
}

fun ListBookFragment.toListBook(): ListBook? {
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
