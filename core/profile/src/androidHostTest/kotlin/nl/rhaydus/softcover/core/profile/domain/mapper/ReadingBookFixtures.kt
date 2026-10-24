package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead

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
    reads: List<UserBookRead> = listOfNotNull(finishedAt?.let { finishedRead(
        id = id,
        finishedAt = it,
    ) },),
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
        lastReadDate = null,
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
