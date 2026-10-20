package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.uibinding.book.bylineColumnStandIns

private const val SIMILAR_BOOKS_STAND_IN_COUNT = 3

internal val SIMILAR_BOOKS_STAND_IN_CARDS: ImmutableList<BookCardUiModel> =
    bylineColumnStandIns(count = SIMILAR_BOOKS_STAND_IN_COUNT)
