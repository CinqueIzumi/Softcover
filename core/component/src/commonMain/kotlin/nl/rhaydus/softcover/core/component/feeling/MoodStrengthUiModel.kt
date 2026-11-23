package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.runtime.Immutable

/**
 * One slider row of a mood sheet. A null [strength] means the reader has not slid it yet: the row shows
 * [unsetLabel] instead of a number and the slider draws no fill and no thumb.
 */
@Immutable
data class MoodStrengthUiModel(
    val key: String,
    val label: String,
    val strength: Int?,
    val unsetLabel: String,
)
