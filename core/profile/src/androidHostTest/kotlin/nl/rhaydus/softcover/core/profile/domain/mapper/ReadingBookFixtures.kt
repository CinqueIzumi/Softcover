package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DatePrecision
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import kotlin.time.Instant

/** A book whose finish date is [finishedAt] (an ISO date), or undated when it is null and no extra reads are given. */
internal fun readBook(
    id: Int,
    finishedAt: String? = null,
    rating: Double? = null,
    pages: Int? = null,
    editions: List<BookEdition> = emptyList(),
    editionId: Int? = null,
    authors: List<Author> = emptyList(),
    tags: List<Tag> = emptyList(),
    lastReadDate: String? = null,
    reads: List<UserBookRead> = listOfNotNull(
        finishedAt?.let {
            finishedRead(
                id = id,
                finishedAt = it,
            )
        },
    ),
): Book = Book(
    id = id,
    title = "Book $id",
    editions = editions,
    defaultEdition = null,
    rating = 0.0,
    description = "",
    releaseYear = 2000,
    pages = pages,
    coverUrl = "",
    authors = authors,
    usersCount = 0,
    ratingsCount = 0,
    bookSeries = null,
    positionsInSeries = emptyList(),
    isCompilation = false,
    tags = tags,
    userBook = UserBook(
        id = id,
        status = BookStatus.Read,
        dateAdded = "2020-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = editionId,
        lastReadDate = lastReadDate,
        rating = rating,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
        reads = reads,
    ),
)

internal fun finishedRead(
    id: Int,
    finishedAt: String,
): UserBookRead = UserBookRead(
    id = id,
    currentPage = null,
    currentSeconds = null,
    progress = 1f,
    startedAt = null,
    finishedAt = finishedAt,
    finishedAtPrecision = null,
    editionPages = null,
)

internal fun datedRead(
    id: Int,
    startedAt: String?,
    finishedAt: String?,
    precision: DatePrecision? = DatePrecision.DAY,
    editionPages: Int? = null,
): UserBookRead = UserBookRead(
    id = id,
    currentPage = null,
    currentSeconds = null,
    progress = 1f,
    startedAt = startedAt,
    finishedAt = finishedAt,
    finishedAtPrecision = precision,
    editionPages = editionPages,
)

internal fun edition(
    id: Int,
    pages: Int?,
): BookEdition = BookEdition(
    id = id,
    canonicalId = null,
    bookId = 0,
    publisher = null,
    title = null,
    url = null,
    localImagePath = null,
    isbn10 = null,
    isbn13 = null,
    pages = pages,
    audioSeconds = null,
    authors = emptyList(),
    releaseYear = 2000,
    format = "",
    owned = false,
)

/**
 * A Did not finish book stopped at [stoppedAt] (a naive-UTC journal timestamp, or none when null) whose
 * newest read is [read].
 */
internal fun dnfBook(
    id: Int,
    stoppedAt: String? = "2026-03-12T12:00:00.000000",
    pages: Int? = null,
    editions: List<BookEdition> = emptyList(),
    editionId: Int? = null,
    tags: List<Tag> = emptyList(),
    lastReadDate: String? = null,
    read: UserBookRead? = unfinishedRead(id = id),
): Book {
    val base = readBook(
        id = id,
        pages = pages,
        editions = editions,
        editionId = editionId,
        tags = tags,
        lastReadDate = lastReadDate,
        reads = listOfNotNull(read),
    )

    return base.copy(
        userBook = base.userBook?.copy(
            status = BookStatus.DidNotFinish,
            journals = listOfNotNull(
                stoppedAt?.let {
                    ReadingJournal(
                        updatedAt = it,
                        event = "status_stopped",
                    )
                },
            ),
        ),
    )
}

internal fun unfinishedRead(
    id: Int,
    currentPage: Int? = null,
    progress: Float = 0f,
    editionPages: Int? = null,
): UserBookRead = UserBookRead(
    id = id,
    currentPage = currentPage,
    currentSeconds = null,
    progress = progress,
    startedAt = null,
    finishedAt = null,
    finishedAtPrecision = null,
    editionPages = editionPages,
)

internal fun progressUpdate(
    bookId: Int,
    at: String,
    pagesRead: Int,
    pages: Int? = null,
): ProgressUpdate = ProgressUpdate(
    bookId = bookId,
    at = Instant.parse(at),
    pages = pages,
    pagesRead = pagesRead,
)
