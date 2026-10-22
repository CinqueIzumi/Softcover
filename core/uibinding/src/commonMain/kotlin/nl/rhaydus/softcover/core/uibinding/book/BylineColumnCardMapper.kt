package nl.rhaydus.softcover.core.uibinding.book

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * A column card of the cover, the title and the "By X" byline: the card of every "more books" rail.
 */
fun Book.toBylineColumnCard(
    cover: CoverUiModel?,
    coverBadge: CoverOverlayUiModel?,
    transitionSurface: String? = null,
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
        text = BookCardContent(
            title = title,
            titleTransitionKey = transitionKey(
                element = BookTransitionElement.Title,
                surface = transitionSurface,
            ),
            byline = byline(author = authorString),
        ),
        hasMenu = hasMenu,
    ),
)
