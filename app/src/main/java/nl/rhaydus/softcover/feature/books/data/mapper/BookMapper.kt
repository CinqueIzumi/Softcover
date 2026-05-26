package nl.rhaydus.softcover.feature.books.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.data.database.model.AuthorEntity
import nl.rhaydus.softcover.core.data.database.model.BookAuthorCrossRef
import nl.rhaydus.softcover.core.data.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.data.database.model.BookEntity
import nl.rhaydus.softcover.core.data.database.model.BookFullEntity
import nl.rhaydus.softcover.core.data.database.model.BookSeriesEntity
import nl.rhaydus.softcover.core.data.database.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.core.data.database.model.ReadingJournalEntity
import nl.rhaydus.softcover.core.data.database.model.TagEntity
import nl.rhaydus.softcover.core.data.database.model.UserBookEntity
import nl.rhaydus.softcover.core.data.database.model.UserBookReadEntity
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
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_started_journal.Companion.readingJournalFragment as userBookReadStartedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Status_stopped_journal.Companion.readingJournalFragment as statusStoppedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_finished_journal.Companion.readingJournalFragment as userBookReadFinishedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read.Companion.userBookReadFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

private fun String?.toLocalDateOrNull(): LocalDate? {
    val raw = this?.trim().orEmpty()

    if (raw.isEmpty()) return null

    return try {
        LocalDate.parse(raw)
    } catch (_: DateTimeParseException) {
        null
    }
}

// region DTO -> UI mappers
fun EditionFragment.toBookEdition(
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
    format = edition_format ?: "",
    bookId = book_id,
    owned = false,
)

fun EditionDetailFragment.toBookEdition(): BookEdition {
    val authors = contributions.mapNotNull { contribution ->
        val author = contribution.author ?: return@mapNotNull null

        Author(name = author.name, id = author.id)
    }
    return (this as EditionFragment).toBookEdition(authors = authors)
}

fun ReadingJournalFragment.toReadingJournal(): ReadingJournal = ReadingJournal(
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
        reviewHasSpoilers = review_has_spoilers,
        reviewedAt = reviewed_at,
        updatedAt = updated_at,
        createdAt = created_at,
        journals = journals
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

    Author(name = author.name, id = author.id)
}

private fun BookListFragment.bookSeries(): BookSeries? =
    book_series.firstOrNull()?.bookSeriesFragment()?.toBookSeries()

private fun BookListFragment.positionsInSeries(): List<Double> {
    val first = book_series.firstOrNull()?.bookSeriesFragment() ?: return emptyList()

    return parsePositionDetails(details = first.details, fallback = first.position)
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
    taggable_counts.mapNotNull { count ->
        val tag = count.tag ?: return@mapNotNull null

        Tag(
            id = tag.id.toInt(),
            name = tag.tag,
        )
    }

fun UserBookFragment.toBook(): Book? {
    val listFragment = book.bookListFragment() ?: return null

    val bookAuthors = listFragment.authors()
    val selectedEdition = edition?.editionFragment()
        ?.toBookEdition(authors = bookAuthors)
        ?.copy(bookId = listFragment.id)

    val editions = listOfNotNull(selectedEdition)

    if (editions.isEmpty()) return null

    val userBookReadFragment = user_book_reads.firstOrNull()?.userBookReadFragment()

    return Book(
        id = listFragment.id,
        canonicalId = listFragment.canonicalIdOrNull(),
        title = listFragment.title ?: "",
        editions = editions,
        defaultEdition = null,
        rating = listFragment.roundedRating(),
        description = listFragment.description ?: "",
        releaseYear = listFragment.release_year ?: -1,
        releaseDate = listFragment.release_date.toLocalDateOrNull(),
        coverUrl = listFragment.image?.url ?: "",
        authors = bookAuthors,
        usersCount = listFragment.users_count ?: 0,
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

    if (editions.isEmpty()) return null

    return Book(
        id = listFragment.id,
        canonicalId = listFragment.canonicalIdOrNull(),
        title = listFragment.title ?: "",
        editions = editions,
        defaultEdition = defaultEdition,
        rating = listFragment.roundedRating(),
        description = description ?: "",
        releaseYear = listFragment.release_year ?: -1,
        releaseDate = listFragment.release_date.toLocalDateOrNull(),
        coverUrl = listFragment.image?.url ?: "",
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
        amountOfBooks = series.primary_books_count ?: 0
    )
}
// endregion

// region UI -> Entity mappers
fun BookSeries.toEntity(): BookSeriesEntity = BookSeriesEntity(
    id = id,
    name = name,
    amountOfBooks = amountOfBooks,
)

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    rating = rating,
    description = description,
    releaseYear = releaseYear,
    releaseDate = releaseDate?.toString(),
    coverUrl = coverUrl,
    defaultEditionId = defaultEdition?.id,
    usersCount = usersCount,
    ratingsCount = ratingsCount,
    positionsInSeries = positionsInSeries.joinToString(separator = ","),
    isCompilation = isCompilation,
    seriesId = bookSeries?.id,
)

fun UserBookRead.toEntity(userBookId: Int): UserBookReadEntity = UserBookReadEntity(
    id = id,
    currentPage = currentPage,
    currentSeconds = currentSeconds,
    progress = progress,
    startedAt = startedAt,
    finishedAt = finishedAt,
    userBookId = userBookId
)

fun UserBook.toEntity(bookId: Int): UserBookEntity = UserBookEntity(
    id = id,
    statusCode = status.code,
    dateAdded = dateAdded,
    createdAt = createdAt,
    privacySettingId = privacySettingId,
    reviewHasSpoilers = reviewHasSpoilers,
    editionId = editionId,
    lastReadDate = lastReadDate,
    rating = rating,
    referrerUserId = referrerUserId,
    reviewedAt = reviewedAt,
    updatedAt = updatedAt,
    bookId = bookId,
)

fun ReadingJournal.toEntity(userBookId: Int): ReadingJournalEntity = ReadingJournalEntity(
    event = event ?: "",
    updatedAt = updatedAt,
    userBookId = userBookId
)

fun BookEdition.toEntity(): BookEditionEntity = BookEditionEntity(
    id = id,
    canonicalId = canonicalId,
    bookId = bookId,
    publisher = publisher,
    title = title,
    url = url,
    localImagePath = localImagePath,
    isbn10 = isbn10,
    isbn13 = isbn13,
    pages = pages,
    audioSeconds = audioSeconds,
    releaseYear = releaseYear,
    releaseDate = releaseDate?.toString(),
    format = format,
)

fun Author.toEntity(): AuthorEntity = AuthorEntity(name = name, id = id)

fun Book.toBookAuthorRefs(authorIdsByName: Map<String, Int>): List<BookAuthorCrossRef> =
    authors.map { BookAuthorCrossRef(bookId = id, authorId = authorIdsByName.getValue(it.name)) }

fun Book.toEditionAuthorRefs(authorIdsByName: Map<String, Int>): List<EditionAuthorCrossRef> =
    editions.flatMap { it.toEditionAuthorRefs(authorIdsByName) }

fun BookEdition.toEditionAuthorRefs(authorIdsByName: Map<String, Int>): List<EditionAuthorCrossRef> =
    authors.map {
        EditionAuthorCrossRef(
            editionId = id,
            authorId = authorIdsByName.getValue(it.name)
        )
    }

// endregion

// region Entity -> UI mappers
fun AuthorEntity.toModel(): Author = Author(name = name, id = id)

fun TagEntity.toModel(): Tag = Tag(id = id, name = name)

fun BookEditionEntity.toModel(
    authors: List<AuthorEntity>,
    owned: Boolean,
): BookEdition = BookEdition(
    id = id,
    canonicalId = canonicalId,
    publisher = publisher,
    title = title,
    url = url,
    localImagePath = localImagePath,
    isbn10 = isbn10,
    isbn13 = isbn13,
    pages = pages,
    audioSeconds = audioSeconds,
    releaseYear = releaseYear,
    releaseDate = releaseDate.toLocalDateOrNull(),
    authors = authors.map { it.toModel() },
    format = format,
    bookId = bookId,
    owned = owned,
)

fun UserBookReadEntity.toModel(): UserBookRead = UserBookRead(
    id = id,
    currentPage = currentPage,
    currentSeconds = currentSeconds,
    progress = progress ?: 0f,
    startedAt = startedAt,
    finishedAt = finishedAt,
)

fun UserBookEntity.toModel(journals: List<ReadingJournal>): UserBook = UserBook(
    id = id,
    status = BookStatus.getFromCode(statusCode),
    dateAdded = dateAdded,
    createdAt = createdAt,
    privacySettingId = privacySettingId,
    reviewHasSpoilers = reviewHasSpoilers,
    editionId = editionId,
    lastReadDate = lastReadDate,
    rating = rating,
    referrerUserId = referrerUserId,
    reviewedAt = reviewedAt,
    updatedAt = updatedAt,
    journals = journals,
)

fun ReadingJournalEntity.toModel(): ReadingJournal = ReadingJournal(
    updatedAt = updatedAt,
    event = event,
)

fun BookFullEntity.toModel(): Book {
    val uiEditions = editions.map { editionWithAuthors ->
        val editionAuthors = editionWithAuthors.authors.ifEmpty { bookAuthors }
        editionWithAuthors.edition.edition.toModel(
            authors = editionAuthors,
            owned = editionWithAuthors.edition.isOwned,
        )
    }

    val defaultEdition = book.defaultEditionId?.let { id ->
        uiEditions.firstOrNull { it.id == id }
    }

    val journals = userBookWithJournals?.journals?.map { it.toModel() } ?: emptyList()

    return Book(
        id = book.id,
        title = book.title,
        editions = uiEditions,
        defaultEdition = defaultEdition,
        rating = book.rating,
        description = book.description,
        releaseYear = book.releaseYear,
        releaseDate = book.releaseDate.toLocalDateOrNull(),
        coverUrl = book.coverUrl,
        authors = bookAuthors.map { it.toModel() },
        usersCount = book.usersCount,
        ratingsCount = book.ratingsCount,
        userBook = userBookWithJournals?.userBook?.toModel(journals = journals),
        userBookRead = userBookWithJournals?.userBookRead?.toModel(),
        positionsInSeries = book.positionsInSeries
            .split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { it.toDoubleOrNull() },
        isCompilation = book.isCompilation,
        tags = tags.map { it.toModel() },
        bookSeries = series?.toModel()
    )
}

fun BookSeriesEntity.toModel(): BookSeries = BookSeries(
    id = id,
    name = name,
    amountOfBooks = amountOfBooks,
)
// endregion
