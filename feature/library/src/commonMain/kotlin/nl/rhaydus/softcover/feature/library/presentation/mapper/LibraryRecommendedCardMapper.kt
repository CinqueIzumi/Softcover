package nl.rhaydus.softcover.feature.library.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.core.uibinding.book.transitionKey
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

private const val RECOMMENDED_STAND_IN_COUNT = 4

internal fun Book.toRecommendedCard(): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = id),
    variant = BookCardVariant.Column(
        cover = BookCardCover(
            model = toCoverUiModel(
                variant = CoverVariant.Shelf,
                shareTransition = true,
            ),
        ),
        text = BookCardContent(
            title = title,
            byline = byline(author = authorString),
            titleTransitionKey = transitionKey(element = BookTransitionElement.Title),
        ),
        hasMenu = true,
        bookmark = BookCardTrailing.Bookmark.labelled(
            saved = false,
            contentDescription = "Add to Want to read",
        ),
    ),
)

internal val RECOMMENDED_STAND_IN_CARDS: ImmutableList<BookCardUiModel> = List(RECOMMENDED_STAND_IN_COUNT) { index ->
    BookCardUiModel(
        key = BookCardKey.Book(bookId = -(index + 1)),
        variant = BookCardVariant.Column(
            cover = BookCardCover(
                model = CoverUiModel(
                    source = null,
                    coverlessTitle = null,
                    variant = CoverVariant.Shelf,
                    isLoading = true,
                ),
            ),
            text = BookCardContent(
                title = "The Name of the Wind",
                byline = "By Patrick Rothfuss",
            ),
            hasMenu = true,
        ),
        isLoading = true,
    )
}.toImmutableList()
