package nl.rhaydus.softcover.feature.reading.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.fragment.BookFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import kotlin.math.roundToInt

// TODO: Does this not really belong in the book feature, rather than currently reading?
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
        releaseYear = release_year ?: -1,
    )
}

fun BookFragment.toBook(): Book {
    val rating = ((rating ?: 0.0) * 10).roundToInt() / 10.0

    return Book(
        id = id,
        title = title ?: "",
        editions = editions.map { userBookEdition ->
            userBookEdition.editionFragment.toBookEdition()
        },
        description = description ?: "",
        rating = rating,
        releaseYear = release_year ?: -1,
        pages = pages ?: -1,
        coverUrl = image?.url ?: "",
        status = BookStatus.None,
        authors = contributions.map { contribution ->
            Author(name = contribution.author?.name ?: "")
        },
        currentPage = null,
        progress = null,
        editionId = null,
        userBookId = null,
        startedAt = null,
        finishedAt = null,
        userBookReadId = null,
    )
}

fun UserBookFragment.toBook(): Book {
    return this.book
        .bookFragment
        .toBook()
        .copy(status = BookStatus.getFromCode(code = this.status_id))
}

fun UserBookReadFragment.toBook(): Book {
    val userBookFragment = user_book
        ?.userBookFragment
        ?: throw Exception("user book fragment not found")

    val book = userBookFragment.toBook()

    return book.copy(
        currentPage = progress_pages,
        progress = progress?.toFloat(),
        editionId = edition?.editionFragment?.id,
        userBookId = userBookFragment.id,
        userBookReadId = id,
        startedAt = started_at,
        finishedAt = finished_at,
    )
}