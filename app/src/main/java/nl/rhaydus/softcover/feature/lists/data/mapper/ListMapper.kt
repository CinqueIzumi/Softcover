package nl.rhaydus.softcover.feature.lists.data.mapper

import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.data.mapper.toModel
import nl.rhaydus.softcover.core.data.database.model.BookListEntity
import nl.rhaydus.softcover.core.data.database.model.BookListWithBooks
import nl.rhaydus.softcover.core.data.database.model.ListBookEntity
import nl.rhaydus.softcover.core.data.database.model.ListBookFull
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import nl.rhaydus.softcover.fragment.ListFragment.List_book.Companion.listBookFragment

private const val OWNED_LIST_SLUG: String = "owned"

fun ListFragment.toBookList(): BookList {
    val listBooks = list_books.mapNotNull { it.listBookFragment()?.toListBook() }

    return BookList(
        id = id,
        name = name,
        slug = slug ?: "",
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
        addedAt = created_at,
    )
}

fun BookList.toEntity(): BookListEntity = BookListEntity(
    id = id,
    name = name,
    slug = slug,
)

fun ListBook.toEntity(): ListBookEntity = ListBookEntity(
    listId = listId,
    bookId = bookId,
    editionId = editionId,
    listBookId = listBookId,
    addedAt = addedAt,
)

fun ListBookFull.toModel(isOwnedList: Boolean): ListBook {
    val cachedBook = book

    val preferredEdition = if (isOwnedList) {
        null
    } else {
        val preferredEditionId = cachedBook?.userBookWithJournals?.userBook?.editionId
            ?: cachedBook?.book?.defaultEditionId

        preferredEditionId?.let { id ->
            cachedBook?.editions?.firstOrNull { it.edition.edition.id == id }
        }
    }

    val resolvedEdition = preferredEdition ?: edition

    return ListBook(
        listBookId = listBook.listBookId,
        listId = listBook.listId,
        bookId = listBook.bookId,
        editionId = resolvedEdition?.edition?.edition?.id ?: listBook.editionId,
        addedAt = listBook.addedAt,
        book = book?.toModel(),
        edition = resolvedEdition?.let { editionWithAuthors ->
            editionWithAuthors.edition.edition.toModel(
                authors = editionWithAuthors.authors,
                owned = editionWithAuthors.edition.isOwned,
            )
        },
    )
}

fun BookListWithBooks.toModel(): BookList {
    val isOwnedList = bookList.slug == OWNED_LIST_SLUG

    return BookList(
        id = bookList.id,
        name = bookList.name,
        slug = bookList.slug,
        books = listBooks
            .filter { it.book != null && it.edition != null }
            .sortedWith(
                compareBy<ListBookFull, String?>(nullsLast(reverseOrder())) { it.listBook.addedAt }
                    .thenByDescending { it.listBook.listBookId },
            )
            .map { it.toModel(isOwnedList = isOwnedList) },
    )
}
