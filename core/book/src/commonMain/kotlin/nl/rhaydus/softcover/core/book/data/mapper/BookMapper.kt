package nl.rhaydus.softcover.core.book.data.mapper

import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromSlate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.fragment.BookDetailFragment
import nl.rhaydus.softcover.fragment.BookDetailFragment.Default_cover_edition.Companion.editionFragment
import nl.rhaydus.softcover.fragment.BookListFragment
import nl.rhaydus.softcover.fragment.BookListFragment.Book_series.Companion.bookSeriesFragment
import nl.rhaydus.softcover.fragment.BookSeriesFragment
import nl.rhaydus.softcover.fragment.EditionDetailFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Book.Companion.bookListFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Edition.Companion.editionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Progress_updated_journal.Companion.readingJournalFragment as progressUpdatedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Status_stopped_journal.Companion.readingJournalFragment as statusStoppedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read.Companion.userBookReadFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_finished_journal.Companion.readingJournalFragment as userBookReadFinishedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_started_journal.Companion.readingJournalFragment as userBookReadStartedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment

private fun String?.toLocalDateOrNull(): LocalDate? {
    val raw = this?.trim().orEmpty()

    if (raw.isEmpty()) return null

    return try {
        LocalDate.parse(raw)
    } catch (_: IllegalArgumentException) {
        null
    }
}

// region DTO -> UI mappers
internal fun EditionFragment.toBookEdition(
    authors: List<Author> = emptyList(),
): BookEdition = BookEdition(
    id = id,
    canonicalId = canonical_id,
    title = title,
    url = image?.url ?: fallbackImages.firstOrNull()?.url,
    localImagePath = null,
    publisher = publisher?.name,
    pages = pages,
    audioSeconds = audio_seconds,
    authors = authors,
    isbn10 = isbn_10,
    isbn13 = isbn_13,
    releaseYear = release_year ?: -1,
    releaseDate = release_date.toLocalDateOrNull(),
    format = edition_format.orEmpty(),
    readingFormat = ReadingFormat.fromId(reading_format_id),
    bookId = book_id,
    owned = false,
)

internal fun EditionDetailFragment.toBookEdition(): BookEdition {
    val authors = contributions.mapNotNull { contribution ->
        val author = contribution.author ?: return@mapNotNull null

        Author(
            name = author.name,
            id = author.id,
        )
    }
    return (this as EditionFragment).toBookEdition(authors = authors)
}

internal fun ReadingJournalFragment.toReadingJournal(): ReadingJournal = ReadingJournal(
    updatedAt = updated_at,
    event = event,
)

private fun UserBookFragment.toUserBook(): UserBook {
    val journals = buildList {
        progress_updated_journal.mapNotNullTo(this) {
            it.progressUpdatedJournalFragment()?.toReadingJournal()
        }
        user_book_read_started_journal.mapNotNullTo(this) {
            it.userBookReadStartedJournalFragment()?.toReadingJournal()
        }
        user_book_read_finished_journal.mapNotNullTo(this) {
            it.userBookReadFinishedJournalFragment()?.toReadingJournal()
        }
        status_stopped_journal.mapNotNullTo(this) {
            it.statusStoppedJournalFragment()?.toReadingJournal()
        }
    }

    return UserBook(
        id = id,
        status = BookStatus.getFromCode(code = status_id),
        editionId = edition_id,
        lastReadDate = last_read_date,
        dateAdded = date_added,
        privacySettingId = privacy_setting_id,
        rating = rating,
        referrerUserId = referrer_user_id,
        reviewDocument = reviewDocumentFromSlate(slate = review_slate).takeUnless { it.isBlank() },
        reviewHasSpoilers = review_has_spoilers,
        reviewedAt = reviewed_at,
        updatedAt = updated_at,
        createdAt = created_at,
        journals = journals,
    )
}

private fun UserBookReadFragment.toUserBookRead(): UserBookRead = UserBookRead(
    currentPage = progress_pages,
    currentSeconds = progress_seconds,
    progress = progress?.toFloat() ?: 0f,
    id = id,
    startedAt = started_at,
    finishedAt = finished_at,
)

private fun BookListFragment.authors(): List<Author> = contributions.mapNotNull { contribution ->
    val author = contribution.author ?: return@mapNotNull null

    Author(
        name = author.name,
        id = author.id,
    )
}

private fun BookListFragment.bookSeries(): BookSeries? =
    book_series.firstOrNull()?.bookSeriesFragment()?.toBookSeries()

private fun BookListFragment.positionsInSeries(): List<Double> {
    val first = book_series.firstOrNull()?.bookSeriesFragment() ?: return emptyList()

    return parsePositionDetails(
        details = first.details,
        fallback = first.position,
    )
}

private fun parsePositionDetails(
    details: String?,
    fallback: Double?,
): List<Double> {
    val parsed = details?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { raw ->
            val rangeParts = raw.split("-").map { it.trim() }

            when (rangeParts.size) {
                1 -> rangeParts[0].toDoubleOrNull()?.let { listOf(it) }
                2 -> {
                    val start = rangeParts[0].toDoubleOrNull()
                    val end = rangeParts[1].toDoubleOrNull()

                    if (start == null || end == null || end < start) {
                        null
                    } else if (start % 1.0 == 0.0 && end % 1.0 == 0.0) {
                        (start.toInt()..end.toInt()).map { it.toDouble() }
                    } else {
                        listOf(start, end)
                    }
                }
                else -> null
            }
        }

    return parsed ?: listOfNotNull(fallback)
}

private fun BookListFragment.canonicalIdOrNull(): Int? {
    val canonicalId = canonical?.id ?: return null

    return canonicalId.takeIf { it != id }
}

private fun BookListFragment.roundedRating(): Double =
    ((rating ?: 0.0) * 10).roundToInt() / 10.0

private fun BookListFragment.tags(): List<Tag> =
    taggable_counts.mapNotNull { taggableCount ->
        val tag = taggableCount.tag ?: return@mapNotNull null

        Tag(
            id = tag.id.toInt(),
            name = tag.tag,
            category = TagCategory.fromApiString(tag.tag_category.category),
            count = taggableCount.count,
        )
    }

internal fun UserBookFragment.toBook(): Book? {
    val listFragment = book.bookListFragment() ?: return null

    val bookAuthors = listFragment.authors()
    val selectedEdition = edition?.editionFragment()
        ?.toBookEdition(authors = bookAuthors)
        ?.copy(bookId = listFragment.id)

    val editions = listOfNotNull(selectedEdition)

    val userBookReadFragment = user_book_reads.firstOrNull()?.userBookReadFragment()

    return Book(
        id = listFragment.id,
        canonicalId = listFragment.canonicalIdOrNull(),
        title = listFragment.title.orEmpty(),
        editions = editions,
        defaultEdition = null,
        rating = listFragment.roundedRating(),
        headline = listFragment.headline.orEmpty(),
        description = listFragment.description.orEmpty(),
        releaseYear = listFragment.release_year ?: -1,
        releaseDate = listFragment.release_date.toLocalDateOrNull(),
        coverUrl = listFragment.image?.url.orEmpty(),
        authors = bookAuthors,
        usersCount = listFragment.users_count,
        ratingsCount = listFragment.ratings_count,
        bookSeries = listFragment.bookSeries(),
        positionsInSeries = listFragment.positionsInSeries(),
        isCompilation = listFragment.compilation,
        tags = listFragment.tags(),
        userBook = toUserBook(),
        userBookRead = userBookReadFragment?.toUserBookRead(),
    )
}

fun BookDetailFragment.toBook(): Book? {
    val listFragment: BookListFragment = this
    val bookAuthors = listFragment.authors()
    val defaultEdition = default_cover_edition?.editionFragment()
        ?.toBookEdition(authors = bookAuthors)
        ?.copy(bookId = listFragment.id)
    val editions = listOfNotNull(defaultEdition)

    return Book(
        id = listFragment.id,
        canonicalId = listFragment.canonicalIdOrNull(),
        title = listFragment.title.orEmpty(),
        editions = editions,
        defaultEdition = defaultEdition,
        rating = listFragment.roundedRating(),
        headline = listFragment.headline.orEmpty(),
        description = description.orEmpty(),
        releaseYear = listFragment.release_year ?: -1,
        releaseDate = listFragment.release_date.toLocalDateOrNull(),
        coverUrl = listFragment.image?.url.orEmpty(),
        authors = bookAuthors,
        usersCount = users_count,
        ratingsCount = listFragment.ratings_count,
        bookSeries = listFragment.bookSeries(),
        positionsInSeries = listFragment.positionsInSeries(),
        isCompilation = listFragment.compilation,
        tags = listFragment.tags(),
        userBook = null,
        userBookRead = null,
    )
}

private fun BookSeriesFragment.toBookSeries(): BookSeries? {
    val series = series ?: return null

    return BookSeries(
        id = series.id,
        name = series.name,
        amountOfBooks = series.primary_books_count ?: 0,
    )
}
// endregion
