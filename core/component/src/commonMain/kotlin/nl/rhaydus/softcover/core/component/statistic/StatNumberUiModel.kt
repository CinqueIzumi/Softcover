package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A number that can change in place, and how it should read.
 *
 * One `Double`, because the tween runs on a float anyway; [format] decides whether the reader ever
 * sees a fraction.
 */
@Immutable
data class StatNumberUiModel(
    val value: Double,
    val format: StatNumberFormat = StatNumberFormat.Grouped,
    val isLoading: Boolean = false,
    val variant: StatNumberVariant = StatNumberVariant.Standard,
) {
    companion object : UiModelPreviews<StatNumberUiModel> {
        override val previews: ImmutableMap<String, StatNumberUiModel> = persistentMapOf(
            "Grouped thousands" to StatNumberUiModel(value = 12_481.0),
            "Plain integer" to StatNumberUiModel(
                value = 30.0,
                format = StatNumberFormat.Plain,
            ),
            "One decimal" to StatNumberUiModel(
                value = 4.23,
                format = StatNumberFormat.Decimal,
            ),
            "Loading" to StatNumberUiModel(
                value = 0.0,
                isLoading = true,
            ),
        )
    }
}
