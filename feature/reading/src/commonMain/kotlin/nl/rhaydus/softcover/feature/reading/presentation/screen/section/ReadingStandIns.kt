package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead

internal const val ALSO_READING_STAND_IN_COUNT = 2

internal val ALSO_READING_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Standard,
        lead = BookCardLead.Cover(
            model = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = CoverVariant.ReadingRowThumb,
                isLoading = true,
            ),
        ),
        text = BookCardContent(
            title = "The Wise Man's Fear",
            eyebrow = BookCardEyebrow(text = "THE KINGKILLER CHRONICLE"),
            byline = "By Patrick Rothfuss",
        ),
        trailing = BookCardTrailing.SetProgress(label = "Set progress"),
    ),
    decorations = BookCardDecorations(
        progress = BookCardProgress.Value(
            fraction = 0.5f,
            label = "50%",
        ),
    ),
)

internal val PICK_UP_NEXT_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -2),
    variant = BookCardVariant.Column(
        cover = CoverUiModel(
            source = null,
            coverlessTitle = null,
            variant = CoverVariant.ReadingPickUpNextTile,
            isLoading = true,
        ),
        text = BookCardContent(title = "The Name of the Wind"),
        menuLabel = null,
    ),
)

private val FEATURED_STAND_IN_EDITION = BookEdition(
    id = -1,
    canonicalId = null,
    bookId = -1,
    publisher = null,
    title = null,
    url = null,
    localImagePath = null,
    isbn10 = null,
    isbn13 = null,
    pages = 662,
    audioSeconds = null,
    authors = listOf(
        Author(
            id = -1,
            name = "Patrick Rothfuss",
        ),
    ),
    releaseYear = 2007,
    format = "Hardcover",
    readingFormat = ReadingFormat.Physical,
    owned = false,
)

internal val FEATURED_STAND_IN_BOOK = Book(
    id = -1,
    title = "The Name of the Wind",
    editions = listOf(FEATURED_STAND_IN_EDITION),
    defaultEdition = FEATURED_STAND_IN_EDITION,
    rating = 0.0,
    description = "",
    releaseYear = 2007,
    coverUrl = "",
    authors = FEATURED_STAND_IN_EDITION.authors,
    usersCount = 0,
    ratingsCount = 0,
    bookSeries = BookSeries(
        id = -1,
        name = "The Kingkiller Chronicle",
        amountOfBooks = 3,
    ),
    positionsInSeries = listOf(1.0),
    isCompilation = false,
    userBook = null,
    userBookRead = UserBookRead(
        id = -1,
        currentPage = 331,
        currentSeconds = null,
        progress = 50f,
        startedAt = null,
        finishedAt = null,
    ),
)
