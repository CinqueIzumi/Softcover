package nl.rhaydus.softcover.feature.reading.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead

private const val ALSO_READING_STAND_IN_COUNT = 2

/** Also the number of want-to-read books the empty screen shows as "Pick up next" tiles. */
internal const val PICK_UP_NEXT_TILE_COUNT = 3

private val ALSO_READING_STAND_IN_TEMPLATE = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Standard,
        lead = BookCardLead.Cover(
            cover = BookCardCover(
                model = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.Tile,
                    isLoading = true,
                ),
            ),
        ),
        text = BookCardContent(
            title = "The Wise Man's Fear",
            eyebrow = BookCardEyebrow(text = "THE KINGKILLER CHRONICLE"),
            byline = "By Patrick Rothfuss",
        ),
        trailing = BookCardTrailing.SetProgress.labelled(enabled = false),
        contentHorizontalInset = Spacing.xl,
        contentVerticalInset = Spacing.md,
    ),
    decorations = BookCardDecorations(
        progress = BookCardProgress.Value(
            fraction = 0.5f,
            label = "50%",
        ),
    ),
    isLoading = true,
)

internal val ALSO_READING_STAND_IN_CARDS: ImmutableList<BookCardUiModel> =
    List(ALSO_READING_STAND_IN_COUNT) { index ->
        ALSO_READING_STAND_IN_TEMPLATE.copy(key = BookCardKey.Book(bookId = -(index + 1)))
    }.toImmutableList()

private val PICK_UP_NEXT_STAND_IN_TEMPLATE = BookCardUiModel(
    key = BookCardKey.Book(bookId = -1),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = CoverVariant.Tile,
                isLoading = true,
            ),
        ),
        text = BookCardContent(title = "The Name of the Wind"),
    ),
    isLoading = true,
)

internal val PICK_UP_NEXT_STAND_IN_CARDS: ImmutableList<BookCardUiModel> =
    List(PICK_UP_NEXT_TILE_COUNT) { index ->
        PICK_UP_NEXT_STAND_IN_TEMPLATE.copy(key = BookCardKey.Book(bookId = -(index + 1)))
    }.toImmutableList()

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
    userBook = UserBook(
        id = -1,
        status = BookStatus.Reading,
        dateAdded = "",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
        reads = listOf(
            UserBookRead(
                id = -1,
                currentPage = 331,
                currentSeconds = null,
                progress = 50f,
                startedAt = null,
                finishedAt = null,
                finishedAtPrecision = null,
                editionPages = null,
            ),
        ),
    ),
)
