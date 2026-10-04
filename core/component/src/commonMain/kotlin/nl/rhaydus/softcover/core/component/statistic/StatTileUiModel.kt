package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A labelled statistic on a tile.
 *
 * Loading rides on [stat]: only the number masks, while the container, eyebrow and caption stay real.
 */
@Immutable
data class StatTileUiModel(
    val eyebrow: String,
    val stat: StatNumberUiModel,
    val variant: StatTileVariant,
) {
    companion object : UiModelPreviews<StatTileUiModel> {
        override val previews: ImmutableList<StatTileUiModel> = persistentListOf(
            StatTileUiModel(
                eyebrow = "Total pages read",
                stat = StatNumberUiModel(value = 12_481.0),
                variant = StatTileVariant.Hero(caption = "Every page a step further into the story."),
            ),
            StatTileUiModel(
                eyebrow = "Volumes",
                stat = StatNumberUiModel(value = 38.0),
                variant = StatTileVariant.Standard(caption = "books read"),
            ),
            StatTileUiModel(
                eyebrow = "Avg. rating",
                stat = StatNumberUiModel(
                    value = 4.2,
                    format = StatNumberFormat.Decimal(fractionDigits = 1),
                ),
                variant = StatTileVariant.Compact(unit = "★"),
            ),
            StatTileUiModel(
                eyebrow = "Total pages read",
                stat = StatNumberUiModel(
                    value = 0.0,
                    isLoading = true,
                ),
                variant = StatTileVariant.Hero(caption = "Every page a step further into the story."),
            ),
            StatTileUiModel(
                eyebrow = "Volumes",
                stat = StatNumberUiModel(
                    value = 0.0,
                    isLoading = true,
                ),
                variant = StatTileVariant.Standard(caption = "books read"),
            ),
        )
    }
}
