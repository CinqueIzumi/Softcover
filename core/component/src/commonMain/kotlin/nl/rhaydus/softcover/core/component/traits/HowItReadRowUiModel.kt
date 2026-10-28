package nl.rhaydus.softcover.core.component.traits

import androidx.compose.runtime.Immutable

/** One answered question in [HowItReadBlockUiModel.Filled]: a [label] and the reader's [value] for it. */
@Immutable
sealed interface HowItReadRowUiModel {
    val label: String
    val value: String

    /** An answer that is only words. */
    data class Text(
        override val label: String,
        override val value: String,
    ) : HowItReadRowUiModel

    /**
     * An answer on a spectrum, drawn as a track with a dot at [position], 0f at the [startLabel] end and 1f
     * at the [endLabel] end. [centreLabel] names the midpoint.
     */
    data class Scale(
        override val label: String,
        override val value: String,
        val position: Float,
        val startLabel: String,
        val centreLabel: String,
        val endLabel: String,
    ) : HowItReadRowUiModel
}
