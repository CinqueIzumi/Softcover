package nl.rhaydus.softcover.feature.reading.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class FeaturedBookUiModel(
    val id: Int,
    val title: String,
    val seriesText: String?,
    val authors: String?,
    val isAudiobook: Boolean,
)
