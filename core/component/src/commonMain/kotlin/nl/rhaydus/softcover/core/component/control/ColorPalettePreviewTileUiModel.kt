package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

/**
 * Everything [ColorPalettePreviewTile] renders: which [palette] it paints itself in, the [key] its event
 * carries, whether it is the reader's current choice and whether it can be tapped ([enabled]). Its label
 * comes straight from [SpinePalette.label] — the tile has nothing further to say about it.
 */
@Immutable
data class ColorPalettePreviewTileUiModel<K>(
    val key: K,
    val palette: SpinePalette,
    val selected: Boolean,
    val enabled: Boolean = true,
) {
    companion object : UiModelPreviews<ColorPalettePreviewTileUiModel<String>> {
        override val previews: ImmutableMap<String, ColorPalettePreviewTileUiModel<String>> = persistentMapOf(
            "Selected" to ColorPalettePreviewTileUiModel(
                key = "selected",
                palette = SpinePalette.DEFAULT,
                selected = true,
            ),
            "Unselected" to ColorPalettePreviewTileUiModel(
                key = "unselected",
                palette = SpinePalette.DEFAULT,
                selected = false,
            ),
            "Disabled" to ColorPalettePreviewTileUiModel(
                key = "disabled",
                palette = SpinePalette.DEFAULT,
                selected = false,
                enabled = false,
            ),
        )
    }
}
