package nl.rhaydus.softcover.showcase.live.selection.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class SelectableRowUiModel(
    val key: String,
    val title: String,
    val selected: Boolean,
)
