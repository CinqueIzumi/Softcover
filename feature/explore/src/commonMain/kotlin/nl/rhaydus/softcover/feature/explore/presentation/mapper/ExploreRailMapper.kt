package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.ExploreRail

internal fun ExploreRail.toRailModel(set: BookCardSet<Book>): BookCardRailUiModel<Book> = BookCardRailUiModel(
    set = set,
    cardWidth = railCardWidth(rail = this),
    staggerKey = staggerKey,
    canLoadMore = false,
)
