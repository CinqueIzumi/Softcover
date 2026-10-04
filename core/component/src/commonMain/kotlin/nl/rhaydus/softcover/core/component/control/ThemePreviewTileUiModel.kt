package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

/**
 * Everything [ThemePreviewTile] renders: the tile's [key], label, which pairing of the light/dark scheme it
 * paints, whether it is the reader's current choice, and the [palette] / [dynamicColor] the tile
 * resolves its own miniature scheme from.
 *
 * @property palette The reader's chosen spine colour, so every tile in the row wears the same paper
 * and ink the app does.
 * @property dynamicColor Whether dynamic colour is on, so what the tile promises is what picking it
 * delivers.
 */
@Immutable
data class ThemePreviewTileUiModel<K>(
    val key: K,
    val label: String,
    val painting: ThemeTilePainting,
    val selected: Boolean,
    val palette: SpinePalette,
    val dynamicColor: Boolean,
) {
    companion object : UiModelPreviews<ThemePreviewTileUiModel<String>> {
        override val previews: ImmutableMap<String, ThemePreviewTileUiModel<String>> = persistentMapOf(
            "Light" to ThemePreviewTileUiModel(
                key = "light",
                label = "Light",
                painting = ThemeTilePainting.LIGHT,
                selected = true,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            ),
            "Dark" to ThemePreviewTileUiModel(
                key = "dark",
                label = "Dark",
                painting = ThemeTilePainting.DARK,
                selected = false,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            ),
            "System" to ThemePreviewTileUiModel(
                key = "system",
                label = "System",
                painting = ThemeTilePainting.SPLIT,
                selected = false,
                palette = SpinePalette.DEFAULT,
                dynamicColor = false,
            ),
        )
    }
}
