package nl.rhaydus.softcover.feature.reading.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
internal data class TrendingTileUiModel(
    val title: String,
    val author: String?,
    val cover: CoverUiModel?,
)
