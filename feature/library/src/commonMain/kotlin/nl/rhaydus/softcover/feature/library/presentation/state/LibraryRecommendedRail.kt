package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.domain.model.Book

internal data class LibraryRecommendedRail(
    val rail: BookCardRailUiModel<Book>,
    val placement: RecommendedRailPlacement,
)
