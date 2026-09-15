package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A number that can change in place, and how it should read.
 *
 * One `Double` rather than the Int/Float overload pair this replaced — the tween always ran on a
 * float anyway, and [format] is what decides whether the reader ever sees a fraction.
 */
@Immutable
data class StatNumberUiModel(
    val value: Double,
    val format: StatNumberFormat = StatNumberFormat.Grouped,
) {
    companion object : UiModelPreviews<StatNumberUiModel> {
        override val previews: ImmutableList<StatNumberUiModel> = persistentListOf(
            StatNumberUiModel(value = 12_481.0),
            StatNumberUiModel(
                value = 30.0,
                format = StatNumberFormat.Plain,
            ),
            StatNumberUiModel(
                value = 4.23,
                format = StatNumberFormat.Decimal(fractionDigits = 1),
            ),
        )
    }
}
