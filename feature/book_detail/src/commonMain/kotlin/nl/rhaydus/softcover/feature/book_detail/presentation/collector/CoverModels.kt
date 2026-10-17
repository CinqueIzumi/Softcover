package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey

internal data class CoverModels(
    val hero: CoverUiModel,
    val backdrop: CoverUiModel,
    val fullScreen: CoverUiModel,
    val heroTitleTransitionKey: BookTransitionKey,
    val heroRatingTransitionKey: BookTransitionKey,
)
