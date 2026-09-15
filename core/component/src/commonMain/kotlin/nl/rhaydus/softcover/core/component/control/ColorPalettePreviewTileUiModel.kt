package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

/**
 * Everything [ColorPalettePreviewTile] renders: which [palette] it paints itself in, and whether it
 * is the reader's current choice. Its label comes straight from [SpinePalette.label] — the tile has
 * nothing further to say about it.
 */
@Immutable
data class ColorPalettePreviewTileUiModel(
    val palette: SpinePalette,
    val selected: Boolean,
) {
    companion object : UiModelPreviews<ColorPalettePreviewTileUiModel> {
        override val previews: ImmutableList<ColorPalettePreviewTileUiModel> = persistentListOf(
            ColorPalettePreviewTileUiModel(
                palette = SpinePalette.DEFAULT,
                selected = true,
            ),
            ColorPalettePreviewTileUiModel(
                palette = SpinePalette.DEFAULT,
                selected = false,
            ),
        )
    }
}
