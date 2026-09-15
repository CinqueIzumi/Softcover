package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.model.PaletteChoice

/**
 * One [PaletteChoice] per [ColorPalette], marking [selected] as the reader's chosen palette and
 * building its [ColorPalettePreviewTileUiModel]. Invoked off the composition — in
 * [nl.rhaydus.softcover.feature.settings.presentation.collector.ThemeConfigurationCollector] and as
 * [nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState]'s own default —
 * so the Appearance screen's render never calls `toSpinePalette()` itself, nor assembles the tile
 * model itself (`component-contract.md` R9).
 */
internal fun paletteChoicesFor(selected: ColorPalette): ImmutableList<PaletteChoice> = ColorPalette.entries
    .map { palette ->
        PaletteChoice(
            tile = ColorPalettePreviewTileUiModel(
                palette = palette.toSpinePalette(),
                selected = palette == selected,
            ),
            colorPalette = palette,
        )
    }
    .toImmutableList()
