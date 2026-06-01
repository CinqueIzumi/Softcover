package nl.rhaydus.softcover.core.book

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead

val baseAuthor = Author(
    name = "Caitlin Starling",
    id = 1,
)

val baseEdition = BookEdition(
    id = 1,
    canonicalId = null,
    title = "Last to leave the Room",
    publisher = "",
    isbn10 = "",
    isbn13 = "",
    pages = 100,
    audioSeconds = null,
    url = "",
    localImagePath = null,
    releaseYear = 2023,
    authors = listOf(baseAuthor),
    format = "Hardcover",
    readingFormat = ReadingFormat.Physical,
    bookId = 1,
    owned = false,
)

val baseBook = Book(
    id = 1,
    title = "Last to leave the Room",
    rating = 3.7,
    releaseYear = 2023,
    headline = "A sinking city, a door that shouldn't exist, and a copy of yourself that passes every test.",
    description = "Last to Leave the Room is a new novel...",
    editions = listOf(baseEdition),
    coverUrl = "",
    authors = listOf(baseAuthor),
    defaultEdition = baseEdition,
    usersCount = 20,
    ratingsCount = 1200,
    userBook = UserBook(
        id = 1,
        status = BookStatus.None,
        dateAdded = "",
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        createdAt = null,
        journals = emptyList(),
    ),
    userBookRead = UserBookRead(
        id = 1,
        currentPage = null,
        currentSeconds = null,
        progress = 0f,
        startedAt = null,
        finishedAt = null,
    ),
    bookSeries = null,
    positionsInSeries = emptyList(),
    isCompilation = false,
)
