package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable

/**
 * The "In progress" section's readout: the percent, its bar and the two ends of the summary row under it.
 */
@Immutable
internal data class InProgressUiModel(
    val percent: String,
    val fraction: Float,
    val summaryStart: String,
    val summaryEnd: String,
)
