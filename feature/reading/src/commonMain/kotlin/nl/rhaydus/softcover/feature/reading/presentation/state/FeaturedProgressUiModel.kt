package nl.rhaydus.softcover.feature.reading.presentation.state

import androidx.compose.runtime.Immutable

/**
 * The featured card's progress readout.
 *
 * @property pageLead The position, shown at full strength ("Page 348" or the elapsed audiobook time).
 * @property pageTail The total, shown dimmed after [pageLead] (" of 512"); null when it is unknown.
 */
@Immutable
internal data class FeaturedProgressUiModel(
    val percent: String,
    val pageLead: String,
    val pageTail: String?,
    val fraction: Float,
    val isLoading: Boolean = false,
) {
    companion object {
        /** Stand-in figures sized like a real readout, drawn masked while the featured book loads. */
        val Loading = FeaturedProgressUiModel(
            percent = "50%",
            pageLead = "Page 331",
            pageTail = " of 662",
            fraction = 0.5f,
            isLoading = true,
        )
    }
}
