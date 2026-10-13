package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel

internal data class ReadingAtlasUiModel(
    val hero: StatTileUiModel,
    val volumes: StatTileUiModel,
    val rating: StatTileUiModel,
    val streak: StatTileUiModel,
)
