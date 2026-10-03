package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.uibinding.book.ratingPart

internal val BECAUSE_YOU_READ_HEADER_STAND_IN = SectionHeaderUiModel.Section(
    eyebrow = "Because you read",
    headline = "Fantasy",
)

internal val TRENDING_STAND_IN_CARD = standInCard(
    bookId = -1,
    coverVariant = CoverVariant.ExploreRail,
    text = BookCardContent(
        title = "The Name of the Wind",
        supportingLines = persistentListOf(
            BookCardLine(parts = persistentListOf(ratingPart(rating = 4.5))),
        ),
    ),
    menuLabel = null,
)

internal val BECAUSE_YOU_READ_STAND_IN_CARD = standInCard(
    bookId = -2,
    coverVariant = CoverVariant.ExploreRail,
    text = BookCardContent(
        title = "The Name of the Wind",
        byline = "By Patrick Rothfuss",
    ),
    menuLabel = null,
)

internal val UP_NEXT_STAND_IN_CARD = standInCard(
    bookId = -3,
    coverVariant = CoverVariant.ExploreSeriesCard,
    text = BookCardContent(
        title = "The Wise Man's Fear",
        eyebrow = BookCardEyebrow(text = "The Kingkiller Chronicle"),
        supportingLines = persistentListOf(
            BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "Book #2"))),
        ),
    ),
    menuLabel = "More options",
)

internal val SEARCH_RESULT_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -4),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Standard,
        lead = BookCardLead.Cover(
            cover = BookCardCover(
                model = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.ExploreSearchRow,
                    isLoading = true,
                ),
            ),
        ),
        text = BookCardContent(
            title = "The Name of the Wind",
            eyebrow = BookCardEyebrow(text = "The Kingkiller Chronicle"),
            byline = "By Patrick Rothfuss",
            supportingLines = persistentListOf(
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "2007 · 1.2k readers"))),
            ),
        ),
        trailing = BookCardTrailing.Bookmark(
            saved = false,
            contentDescription = "",
        ),
    ),
    isLoading = true,
)

internal val HIDDEN_BOOK_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Book(bookId = -5),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.Cover(
            cover = BookCardCover(
                model = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.HiddenBookRow,
                    isLoading = true,
                ),
            ),
        ),
        text = BookCardContent(
            title = "The Name of the Wind",
            byline = "By Patrick Rothfuss",
        ),
        trailing = BookCardTrailing.Restore(label = "Restore"),
    ),
    isLoading = true,
)

internal val HIDDEN_SERIES_STAND_IN_CARD = BookCardUiModel(
    key = BookCardKey.Series(seriesId = -6),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.SeriesStack(
            front = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = CoverVariant.HiddenSeriesStack,
                isLoading = true,
            ),
        ),
        text = BookCardContent(
            title = "The Kingkiller Chronicle",
            supportingLines = persistentListOf(
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "Patrick Rothfuss · 3 books"))),
            ),
        ),
        trailing = BookCardTrailing.Restore(label = "Restore"),
    ),
    isLoading = true,
)

private fun standInCard(
    bookId: Int,
    coverVariant: CoverVariant,
    text: BookCardContent,
    menuLabel: String?,
) = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = CoverUiModel(
                source = null,
                coverlessTitle = null,
                variant = coverVariant,
                isLoading = true,
            ),
        ),
        text = text,
        menuLabel = menuLabel,
    ),
    isLoading = true,
)
