package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.book.bookStatsLine
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.ratingPart
import nl.rhaydus.softcover.core.uibinding.book.seriesEyebrow
import nl.rhaydus.softcover.core.uibinding.book.transitionKey

/** Trending's card: no byline, and a rating line only for a rated book. */
internal fun Book.toTrendingCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
    transitionSurface: String? = null,
): BookCardUiModel = toColumnCard(
    cover = cover,
    coverBadge = coverBadge,
    text = BookCardContent(
        title = title,
        titleTransitionKey = transitionKey(
            element = BookTransitionElement.Title,
            surface = transitionSurface,
        ),
        supportingLines = if (rating == 0.0) {
            persistentListOf()
        } else {
            persistentListOf(
                persistentListOf(
                    ratingPart(
                        rating = rating,
                        transitionKey = transitionKey(
                            element = BookTransitionElement.Rating,
                            surface = transitionSurface,
                        ),
                    ),
                ),
            )
        },
    ),
)

/** The "Because you read" card: the author is the byline. */
internal fun Book.toBecauseYouReadCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
    transitionSurface: String? = null,
): BookCardUiModel = toColumnCard(
    cover = cover,
    coverBadge = coverBadge,
    text = BookCardContent(
        title = title,
        titleTransitionKey = transitionKey(
            element = BookTransitionElement.Title,
            surface = transitionSurface,
        ),
        byline = byline(author = authorString),
    ),
)

/** An "Up next in your series" card, released or not; the overflow button opens the dismiss sheet. */
internal fun Book.toUpNextCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
    transitionSurface: String? = null,
): BookCardUiModel = toColumnCard(
    cover = cover,
    coverBadge = coverBadge,
    text = BookCardContent(
        title = title,
        titleTransitionKey = transitionKey(
            element = BookTransitionElement.Title,
            surface = transitionSurface,
        ),
        eyebrow = seriesEyebrow(seriesText = bookSeries?.name),
        supportingLines = positionInSeriesDisplay
            ?.let { position ->
                persistentListOf(persistentListOf(BookCardLinePart.Text(text = "Book #$position")))
            }
            ?: persistentListOf(),
    ),
    hasMenu = true,
)

internal fun Book.toSearchResultCard(
    cover: CoverUiModel,
    transitionSurface: String? = null,
): BookCardUiModel {
    val saved = userBook != null

    return BookCardUiModel(
        key = BookCardKey.Book(bookId = id),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Standard,
            lead = BookCardLead.Cover(cover = BookCardCover(model = cover)),
            text = BookCardContent(
                title = title,
                titleTransitionKey = transitionKey(
                    element = BookTransitionElement.Title,
                    surface = transitionSurface,
                ),
                eyebrow = seriesEyebrow(seriesText = seriesText),
                byline = byline(author = authorString),
                supportingLines = listOfNotNull(
                    bookStatsLine(
                        releaseYear = releaseYear,
                        usersCount = usersCount,
                        rating = rating,
                        ratingTransitionKey = transitionKey(
                            element = BookTransitionElement.Rating,
                            surface = transitionSurface,
                        ),
                    ),
                ).toImmutableList(),
            ),
            trailing = BookCardTrailing.Bookmark.labelled(
                saved = saved,
                contentDescription = if (saved) "Remove from library" else "Add to library",
            ),
        ),
    )
}

private fun Book.toColumnCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
    text: BookCardContent,
    hasMenu: Boolean = false,
): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = id),
    variant = BookCardVariant.Column(
        cover = cover?.let {
            BookCardCover(
                model = it,
                badge = coverBadge,
            )
        },
        text = text,
        hasMenu = hasMenu,
    ),
)
