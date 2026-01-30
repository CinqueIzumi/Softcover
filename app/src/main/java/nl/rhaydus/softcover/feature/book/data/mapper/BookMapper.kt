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

fun BookFragment.toBook(
    userBookFragment: UserBookFragment? = null,
    userBookReadFragment: UserBookReadFragment? = null,
): Book {
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
        status = userBookFragment?.status_id?.let { BookStatus.getFromCode(it) } ?: BookStatus.None,
        authors = contributions.mapNotNull { contribution ->
            val author = contribution.author ?: return@mapNotNull null
            val id = author.id

            Author(
                name = author.name,
                id = id,
            )
        },
        currentPage = userBookReadFragment?.let { it.progress_pages ?: 0 },
        progress = userBookReadFragment?.let { it.progress?.toFloat() ?: 0f },
        editionId = userBookReadFragment?.edition?.editionFragment?.id,
        startedAt = userBookReadFragment?.started_at,
        finishedAt = userBookReadFragment?.finished_at,
        userBookReadId = userBookReadFragment?.id,
        userBookId = userBookFragment?.id,
        defaultEdition = default_physical_edition?.editionFragment?.toBookEdition()
    )
}