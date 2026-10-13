package nl.rhaydus.softcover.feature.reading.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant

/**
 * The featured card's progress readout.
 *
 * @property pageLead The position, shown at full strength ("Page 348" or the elapsed audiobook time).
 * @property pageTail The total, shown dimmed after [pageLead] (" of 512"); null when it is unknown.
 */
@Immutable
internal data class FeaturedProgressUiModel(
    val percent: StatNumberUiModel,
    val pageLead: StatNumberUiModel,
    val pageTail: String?,
    val fraction: Float,
    val isLoading: Boolean = false,
) {
    companion object {
        /** Stand-in figures sized like a real readout, drawn masked while the featured book loads. */
        val Loading = FeaturedProgressUiModel(
            percent = StatNumberUiModel(
                value = 50.0,
                format = StatNumberFormat.Plain,
                isLoading = true,
                variant = StatNumberVariant.Headline,
                suffix = StatNumberSuffix.Fixed("%"),
            ),
            pageLead = StatNumberUiModel(
                value = 331.0,
                format = StatNumberFormat.Plain,
                isLoading = true,
                variant = StatNumberVariant.BodyStrong,
                prefix = "Page ",
            ),
            pageTail = " of 662",
            fraction = 0.5f,
            isLoading = true,
        )
    }
}
