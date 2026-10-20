package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.book.toBylineColumnCard
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSimilarBookClickAction

/** The Similar books rail of [sourceBookId]; each card's payload opens its book. */
internal fun List<Book>.toSimilarBooksRail(
    sourceBookId: Int,
    isAppending: Boolean,
    canLoadMore: Boolean,
): BookCardRailUiModel<BookDetailAction> {
    val surface = similarBooksTransitionSurface(sourceBookId = sourceBookId)

    return BookCardRailUiModel(
        set = map { book ->
            val card = book.toBylineColumnCard(
                cover = book.toCoverUiModel(
                    variant = CoverVariant.Raised,
                    shareTransition = true,
                    transitionSurface = surface,
                ),
                coverBadge = null,
                transitionSurface = surface,
            )

            card to OnSimilarBookClickAction(bookId = book.id)
        }.toBookCardSet(),
        cardWidth = similarBooksCardWidth(),
        staggerKey = surface,
        isAppending = isAppending,
        canLoadMore = canLoadMore,
    )
}
