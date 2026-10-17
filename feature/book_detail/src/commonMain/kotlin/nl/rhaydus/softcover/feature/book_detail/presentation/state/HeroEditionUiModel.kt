package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable

/** [length] is null when the edition has no page count or audio length. */
@Immutable
internal data class HeroEditionUiModel(
    val authors: String,
    val length: String?,
    val isAudiobook: Boolean,
)
