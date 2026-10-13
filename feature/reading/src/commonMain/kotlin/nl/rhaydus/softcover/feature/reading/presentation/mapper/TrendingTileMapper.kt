package nl.rhaydus.softcover.feature.reading.presentation.mapper

import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.TrendingTileUiModel

internal fun Book.toTrendingTileUiModel(): TrendingTileUiModel = TrendingTileUiModel(
    title = title,
    author = authors.firstOrNull()?.name,
    cover = toCoverUiModel(
        variant = CoverVariant.Tile,
        shareTransition = true,
    ),
)
