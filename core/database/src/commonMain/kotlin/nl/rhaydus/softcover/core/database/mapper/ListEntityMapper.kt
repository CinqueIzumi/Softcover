package nl.rhaydus.softcover.core.database.mapper

import nl.rhaydus.softcover.core.database.model.BookEditionWithAuthors
import nl.rhaydus.softcover.core.database.model.BookListEntity
import nl.rhaydus.softcover.core.database.model.BookListWithBooks
import nl.rhaydus.softcover.core.database.model.ListBookEntity
import nl.rhaydus.softcover.core.database.model.ListBookFull
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting

private const val OWNED_LIST_SLUG: String = "owned"

internal fun BookList.toEntity(): BookListEntity = BookListEntity(
    id = id,
    name = name,
    slug = slug,
    ranked = ranked,
    privacySettingId = privacy.code,
    signature = signature,
)

internal fun ListBook.toEntity(): ListBookEntity = ListBookEntity(
    listId = listId,
    bookId = bookId,
    editionId = editionId,
    listBookId = listBookId,
    position = position,
    addedAt = addedAt,
)

private fun BookEditionWithAuthors.toDomainEdition(): BookEdition = edition.edition.toModel(
    authors = authors,
    owned = edition.isOwned,
)

fun ListBookFull.toModel(isOwnedList: Boolean): ListBook {
    val cachedBook = book

    val userBookEdition = if (isOwnedList) {
        null
    } else {
        cachedBook?.userBookWithJournals?.userBook?.editionId?.let { id ->
            cachedBook.editions.firstOrNull { it.edition.edition.id == id }
        }
    }

    val rowEdition = edition?.toDomainEdition()

    return ListBook(
        listBookId = listBook.listBookId,
        listId = listBook.listId,
        bookId = listBook.bookId,
        editionId = listBook.editionId,
        position = listBook.position,
        addedAt = listBook.addedAt,
        book = book?.toModel(),
        edition = rowEdition,
        shownEdition = userBookEdition?.toDomainEdition() ?: rowEdition,
    )
}

fun BookListWithBooks.toModel(): BookList {
    val isOwnedList = bookList.slug == OWNED_LIST_SLUG

    // Positioned books come first in ascending position order (the manual order the user
    // dragged into place); unpositioned books fall back to date-added DESC, then listBookId
    // DESC so newcomers slot at the tail of the unpositioned tail rather than mid-list.
    val ordered = listBooks
        .filter { it.book != null && it.edition != null }
        .sortedWith(
            compareBy<ListBookFull> { it.listBook.position == null }
                .thenBy(nullsLast()) { it.listBook.position }
                .then(
                    compareBy<ListBookFull, String?>(nullsLast(reverseOrder())) { it.listBook.addedAt }
                        .thenByDescending { it.listBook.listBookId },
                ),
        )

    return BookList(
        id = bookList.id,
        name = bookList.name,
        slug = bookList.slug,
        ranked = bookList.ranked,
        privacy = PrivacySetting.entries.firstOrNull { it.code == bookList.privacySettingId }
            ?: PrivacySetting.PUBLIC,
        books = ordered.map { it.toModel(isOwnedList = isOwnedList) },
        signature = bookList.signature,
    )
}
