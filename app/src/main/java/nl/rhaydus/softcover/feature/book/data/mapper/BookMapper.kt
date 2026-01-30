package nl.rhaydus.softcover.feature.book.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.fragment.BookFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import kotlin.math.roundToInt

private fun EditionFragment.toBookEdition(): BookEdition {
    return BookEdition(
        id = id,
        title = title,
        url = image?.url,
        publisher = publisher?.name,
        pages = pages,
        authors = contributions.mapNotNull { contribution ->
            val author = contribution.author ?: return@mapNotNull null
            val id = author.id

            Author(
                name = author.name,
                id = id,
            )
        },
        isbn10 = isbn_10,
        releaseYear = release_year ?: -1,
    )
}

fun BookFragment.toBookWithOptionals(): Book {
    val baseBook = this.toBook()

    val userBooks = user_books.firstOrNull() ?: return baseBook
    val userBooksFragment = userBooks.userBookFragment

    val userBook = userBooksFragment.appendBookWithStatus(book = baseBook)

    val userBookReadFragment =
        userBooks.user_book_reads.firstOrNull()?.userBookReadFragment ?: return userBook

    return userBookReadFragment.appendBookWithOptionals(book = userBook)
}

private fun UserBookFragment.appendBookWithStatus(book: Book): Book {
    return book.copy(
        status = BookStatus.getFromCode(code = status_id),
        userBookId = id,
    )
}

private fun UserBookReadFragment.appendBookWithOptionals(book: Book): Book {
    return book.copy(
        currentPage = progress_pages ?: 0,
        progress = progress?.toFloat() ?: 0f,
        editionId = edition?.editionFragment?.id,
        userBookReadId = id,
        startedAt = started_at,
        finishedAt = finished_at,
    )
}

private fun BookFragment.toBook(): Book {
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
        coverUrl = image?.url ?: "",
        status = BookStatus.None,
        authors = contributions.mapNotNull { contribution ->
            val author = contribution.author ?: return@mapNotNull null
            val id = author.id

            Author(
                name = author.name,
                id = id,
            )
        },
        currentPage = null,
        progress = null,
        editionId = null,
        userBookId = null,
        startedAt = null,
        finishedAt = null,
        userBookReadId = null,
        defaultEdition = default_physical_edition?.editionFragment?.toBookEdition()
    )
}