package nl.rhaydus.softcover.feature.session.presentation.state

import androidx.compose.runtime.Immutable

/**
 * The page editor on Focus mode.
 *
 * A book without a page count has no field to type into: [isEditable] is false and [caption] reads
 * the stored percentage instead.
 */
@Immutable
internal data class FocusPageEditorUiModel(
    val caption: String,
    val fraction: Float,
    val isEditable: Boolean,
)
