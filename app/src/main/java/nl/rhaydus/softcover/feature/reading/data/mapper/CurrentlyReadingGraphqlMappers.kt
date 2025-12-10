package nl.rhaydus.softcover.feature.reading.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment

fun EditionFragment.toBookEdition(): BookEdition {
    return BookEdition(
        id = id,
        title = title,
        url = image?.url,
        publisher = publisher?.name,
        pages = pages,
        authors = contributions.map { contribution ->
            Author(name = contribution.author?.name ?: "")
        },
        isbn10 = isbn_10,
    )
}

fun UserBookFragment.toBook(): Book {
    val book = book

    return Book(
        id = book.id,
        title = book.title ?: "",
        editions = book.editions.map { userBookEdition ->
            userBookEdition.editionFragment.toBookEdition()
        }
    )
}

fun UserBookReadFragment.toBookWithProgress(): BookWithProgress? {
    val userBookFragment = user_book?.userBookFragment ?: return null
    val editionId = edition?.editionFragment?.id ?: return null
    val book = userBookFragment.toBook()

    return BookWithProgress(
        book = book,
        currentPage = progress_pages ?: 0,
        progress = progress?.toFloat() ?: 0f,
        editionId = editionId,
        userBookReadId = id,
        startedAt = started_at,
        finishedAt = finished_at,
        userBookId = userBookFragment.id
    )
}