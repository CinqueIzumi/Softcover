package nl.rhaydus.softcover.feature.book.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
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

fun UserBookFragment.toBook(): Book {
    return book.bookFragment.toBook(userBookFragment = this)
}

private fun UserBookFragment?.toUserBook(): UserBook? {
    if (this == null) return null
    return UserBook(
        id = id,
        status = BookStatus.getFromCode(code = status_id),
        editionId = edition_id,
        lastReadDate = last_read_date,
        dateAdded = date_added,
        privacySettingId = privacy_setting_id,
        rating = rating,
        referrerUserId = referrer_user_id,
        reviewHasSpoilers = review_has_spoilers,
        reviewedAt = reviewed_at,
        updatedAt = updated_at,
    )
}

private fun UserBookReadFragment?.toUserBookRead(): UserBookRead? {
    if (this == null) return null

    return UserBookRead(
        currentPage = progress_pages,
        progress = progress?.toFloat(),
        id = id,
        startedAt = started_at,
        finishedAt = finished_at,
    )
}

fun BookFragment.toBook(
    userBookFragment: UserBookFragment? = null,
): Book {
    val rating = ((rating ?: 0.0) * 10).roundToInt() / 10.0
    val userBookReadFragment =
        userBookFragment?.user_book_reads?.firstOrNull()?.userBookReadFragment

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
        authors = contributions.mapNotNull { contribution ->
            val author = contribution.author ?: return@mapNotNull null
            val id = author.id

            Author(
                name = author.name,
                id = id,
            )
        },
        defaultEdition = default_physical_edition?.editionFragment?.toBookEdition(),
        userBook = userBookFragment.toUserBook(),
        userBookRead = userBookReadFragment.toUserBookRead(),
        usersCount = users_count,
    )
}