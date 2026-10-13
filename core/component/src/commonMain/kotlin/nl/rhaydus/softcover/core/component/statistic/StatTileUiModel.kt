package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
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
        override val previews: ImmutableMap<String, StatTileUiModel> = persistentMapOf(
            "Hero" to StatTileUiModel(
                eyebrow = "Total pages read",
                stat = StatNumberUiModel(value = 12_481.0),
                variant = StatTileVariant.Hero(caption = "All the pages you've turned so far."),
            ),
            "Standard" to StatTileUiModel(
                eyebrow = "Volumes",
                stat = StatNumberUiModel(value = 38.0),
                variant = StatTileVariant.Standard(caption = "books read"),
            ),
            "Compact" to StatTileUiModel(
                eyebrow = "Avg. rating",
                stat = StatNumberUiModel(
                    value = 4.2,
                    format = StatNumberFormat.Decimal,
                ),
                variant = StatTileVariant.Compact(unit = "★"),
            ),
            "Hero, loading" to StatTileUiModel(
                eyebrow = "Total pages read",
                stat = StatNumberUiModel(
                    value = 0.0,
                    isLoading = true,
                ),
                variant = StatTileVariant.Hero(caption = "All the pages you've turned so far."),
            ),
            "Standard, loading" to StatTileUiModel(
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
