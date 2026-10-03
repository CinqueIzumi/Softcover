package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

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
            BookCardLine(parts = persistentListOf(BookCardLinePart.Rating(label = "4.5"))),
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

private fun standInCard(
    bookId: Int,
    coverVariant: CoverVariant,
    text: BookCardContent,
    menuLabel: String?,
) = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Stacked(
        cover = CoverUiModel(
            source = null,
            coverlessTitle = null,
            variant = coverVariant,
            isLoading = true,
        ),
        text = text,
        menuLabel = menuLabel,
    ),
)
