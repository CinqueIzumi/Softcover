package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable

/** One read-through in the "Your reads" section. */
@Immutable
internal data class ReadThroughRowUiModel(
    val key: Int,
    val title: String,
    val supporting: String,
    val trailing: String,
)
