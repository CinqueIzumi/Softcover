package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/** Trending's card: no byline, and a rating line only for a rated book. */
internal fun Book.toTrendingCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
): BookCardUiModel = toStackedCard(
    cover = cover,
    coverBadge = coverBadge,
    text = BookCardContent(
        title = title,
        supportingLines = if (rating == 0.0) {
            persistentListOf()
        } else {
            persistentListOf(
                BookCardLine(
                    parts = persistentListOf(
                        BookCardLinePart.Rating(
                            label = formatDecimalNumber(
                                rating,
                                fractionDigits = 1,
                            ),
                        ),
                    ),
                ),
            )
        },
    ),
    menuLabel = null,
)

/** The "Because you read" card: the author is the byline. */
internal fun Book.toBecauseYouReadCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
): BookCardUiModel = toStackedCard(
    cover = cover,
    coverBadge = coverBadge,
    text = BookCardContent(
        title = title,
        byline = authorString.takeIf { it.isNotBlank() }?.let { "By $it" },
    ),
    menuLabel = null,
)

/** An "Up next in your series" card, released or not; the overflow button opens the dismiss sheet. */
internal fun Book.toUpNextCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
): BookCardUiModel = toStackedCard(
    cover = cover,
    coverBadge = coverBadge,
    text = BookCardContent(
        title = title,
        eyebrow = bookSeries?.let { BookCardEyebrow(text = it.name) },
        supportingLines = positionInSeriesDisplay
            ?.let { position ->
                persistentListOf(BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "Book #$position"))))
            }
            ?: persistentListOf(),
    ),
    menuLabel = "More options",
)

private fun Book.toStackedCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
    text: BookCardContent,
    menuLabel: String?,
): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = id),
    variant = BookCardVariant.Stacked(
        cover = cover,
        text = text,
        menuLabel = menuLabel,
    ),
    decorations = BookCardDecorations(coverBadge = coverBadge),
)
