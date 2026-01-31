package nl.rhaydus.softcover.feature.book.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.fragment.BookFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
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

fun BookFragment.toBook(
    userBookFragment: UserBookFragment? = null,
): Book {
    val rating = ((rating ?: 0.0) * 10).roundToInt() / 10.0
    val userBookReadFragment = userBookFragment?.user_book_reads?.firstOrNull()?.userBookReadFragment

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
        userStatus = userBookFragment?.status_id?.let { BookStatus.getFromCode(it) }
            ?: BookStatus.None,
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
        userEditionId = userBookFragment?.edition_id,
        startedAt = userBookReadFragment?.started_at,
        finishedAt = userBookReadFragment?.finished_at,
        userBookReadId = userBookReadFragment?.id,
        userBookId = userBookFragment?.id,
        defaultEdition = default_physical_edition?.editionFragment?.toBookEdition(),
        userLastReadDate = userBookFragment?.last_read_date,
        userDateAdded = userBookFragment?.date_added,
        userPrivacySettingId = userBookFragment?.privacy_setting_id,
        userRating = userBookFragment?.rating,
        userReferrerUserId = userBookFragment?.referrer_user_id,
        userReviewHasSpoilers = userBookFragment?.review_has_spoilers,
        userReviewedAt = userBookFragment?.reviewed_at,
        userUpdatedAt = userBookFragment?.updated_at,
    )
}