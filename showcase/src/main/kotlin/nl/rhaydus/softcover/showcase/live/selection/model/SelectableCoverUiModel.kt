package nl.rhaydus.softcover.showcase.live.selection.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
internal data class SelectableCoverUiModel(
    val key: String,
    val cover: CoverUiModel,
    val selected: Boolean,
)
