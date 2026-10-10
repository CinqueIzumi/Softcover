package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A number that can change in place, and how it should read.
 *
 * One `Double`, because the spring runs on a float anyway; [format] decides whether the reader ever
 * sees a fraction. [prefix] and [suffix] sit around the number in the same text, so a phrase such as
 * "Page 331" reads as one line where only the digits move; they carry their own spaces.
 */
@Immutable
data class StatNumberUiModel(
    val value: Double,
    val format: StatNumberFormat = StatNumberFormat.Grouped,
    val isLoading: Boolean = false,
    val variant: StatNumberVariant = StatNumberVariant.Standard,
    val prefix: String = "",
    val suffix: StatNumberSuffix? = null,
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
            "Fixed suffix" to StatNumberUiModel(
                value = 42.0,
                format = StatNumberFormat.Plain,
                variant = StatNumberVariant.Headline,
                suffix = StatNumberSuffix.Fixed("%"),
            ),
            "Plural suffix" to StatNumberUiModel(
                value = 3.0,
                format = StatNumberFormat.Plain,
                variant = StatNumberVariant.Body,
                suffix = StatNumberSuffix.Plural(
                    one = " day running",
                    other = " days running",
                ),
            ),
            "Prefix" to StatNumberUiModel(
                value = 331.0,
                variant = StatNumberVariant.BodyStrong,
                prefix = "Page ",
            ),
            "Duration" to StatNumberUiModel(
                value = 5_400.0,
                format = StatNumberFormat.Duration,
                variant = StatNumberVariant.Hero,
            ),
            "Loading with affixes" to StatNumberUiModel(
                value = 0.0,
                isLoading = true,
                variant = StatNumberVariant.BodySmall,
                prefix = "Page ",
                suffix = StatNumberSuffix.Fixed(" pages"),
            ),
        )
    }
}
