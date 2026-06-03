package nl.rhaydus.softcover.core.database.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromJson
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.database.model.AuthorEntity
import nl.rhaydus.softcover.core.database.model.BookAuthorCrossRef
import nl.rhaydus.softcover.core.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.BookFullEntity
import nl.rhaydus.softcover.core.database.model.BookSeriesEntity
import nl.rhaydus.softcover.core.database.model.BookTagFull
import nl.rhaydus.softcover.core.database.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.core.database.model.ReadingJournalEntity
import nl.rhaydus.softcover.core.database.model.UserBookEntity
import nl.rhaydus.softcover.core.database.model.UserBookReadEntity
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

private fun String?.toLocalDateOrNull(): LocalDate? {
    val raw = this?.trim().orEmpty()

    if (raw.isEmpty()) return null

    return try {
        LocalDate.parse(raw)
    } catch (_: IllegalArgumentException) {
        null
    }
}

// region UI -> Entity mappers
internal fun BookSeries.toEntity(): BookSeriesEntity = BookSeriesEntity(
    id = id,
    name = name,
    amountOfBooks = amountOfBooks,
)

internal fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    rating = rating,
    headline = headline,
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

internal fun UserBookRead.toEntity(userBookId: Int): UserBookReadEntity = UserBookReadEntity(
    id = id,
    currentPage = currentPage,
    currentSeconds = currentSeconds,
    progress = progress,
    startedAt = startedAt,
    finishedAt = finishedAt,
    userBookId = userBookId,
)

internal fun UserBook.toEntity(bookId: Int): UserBookEntity = UserBookEntity(
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
    reviewSlateJson = reviewDocument?.toJson(),
    reviewedAt = reviewedAt,
    updatedAt = updatedAt,
    bookId = bookId,
)

internal fun ReadingJournal.toEntity(userBookId: Int): ReadingJournalEntity = ReadingJournalEntity(
    event = event ?: "",
    updatedAt = updatedAt,
    userBookId = userBookId,
)

internal fun BookEdition.toEntity(): BookEditionEntity = BookEditionEntity(
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
    readingFormatId = readingFormat?.id,
)

internal fun Author.toEntity(): AuthorEntity = AuthorEntity(
    name = name,
    id = id,
)

internal fun Book.toBookAuthorRefs(authorIdsByName: Map<String, Int>): List<BookAuthorCrossRef> =
    authors.map { BookAuthorCrossRef(
        bookId = id,
        authorId = authorIdsByName.getValue(it.name),
    ) }

internal fun Book.toEditionAuthorRefs(authorIdsByName: Map<String, Int>): List<EditionAuthorCrossRef> =
    editions.flatMap { it.toEditionAuthorRefs(authorIdsByName) }

internal fun BookEdition.toEditionAuthorRefs(authorIdsByName: Map<String, Int>): List<EditionAuthorCrossRef> =
    authors.map {
        EditionAuthorCrossRef(
            editionId = id,
            authorId = authorIdsByName.getValue(it.name),
        )
    }
// endregion
// region Entity -> UI mappers
fun AuthorEntity.toModel(): Author = Author(
    name = name,
    id = id,
)

fun BookTagFull.toModel(): Tag = Tag(
    id = tag.id,
    name = tag.name,
    category = TagCategory.fromName(tag.category),
    count = crossRef.count,
)

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
    readingFormat = ReadingFormat.fromId(readingFormatId),
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
    reviewDocument = reviewDocumentFromJson(json = reviewSlateJson),
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
        headline = book.headline,
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
        bookSeries = series?.toModel(),
    )
}

fun BookSeriesEntity.toModel(): BookSeries = BookSeries(
    id = id,
    name = name,
    amountOfBooks = amountOfBooks,
)
// endregion
