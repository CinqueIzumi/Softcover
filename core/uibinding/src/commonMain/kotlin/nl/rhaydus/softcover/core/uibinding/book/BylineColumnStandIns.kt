package nl.rhaydus.softcover.core.uibinding.book

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant

/**
 * [count] loading stand-ins shaped like [toBylineColumnCard]'s cards, under distinct negative book ids.
 * [hasMenu] matches the real cards' so the overflow button doesn't pop in when they land.
 */
fun bylineColumnStandIns(
    count: Int,
    hasMenu: Boolean = false,
): ImmutableList<BookCardUiModel> = List(count) { index ->
    BookCardUiModel(
        key = BookCardKey.Book(bookId = -(index + 1)),
        variant = BookCardVariant.Column(
            cover = BookCardCover(
                model = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.Raised,
                    isLoading = true,
                ),
            ),
            text = BookCardContent(
                title = "The Name of the Wind",
                byline = "By Patrick Rothfuss",
            ),
            hasMenu = hasMenu,
        ),
        isLoading = true,
    )
}.toImmutableList()
