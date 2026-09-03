package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.model.PaletteChoice

/**
 * One [PaletteChoice] per [ColorPalette], marking [selected] as the reader's chosen palette.
 * Invoked off the composition — in
 * [nl.rhaydus.softcover.feature.settings.presentation.collector.ThemeConfigurationCollector] and as
 * [nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState]'s own default —
 * so the Appearance screen's render never calls `toSpinePalette()` itself (`component-contract.md`
 * R9).
 */
internal fun paletteChoicesFor(selected: ColorPalette): ImmutableList<PaletteChoice> = ColorPalette.entries
    .map { palette ->
        PaletteChoice(
            palette = palette.toSpinePalette(),
            selected = palette == selected,
            colorPalette = palette,
        )
    }
    .toImmutableList()
