package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.model.GalleryPaletteChoice

/**
 * One [GalleryPaletteChoice] per [ColorPalette], marking [selected] as the current override —
 * `null` when the gallery is following the app's own palette, in which case no chip is marked
 * selected. Invoked off the composition — in
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryPaletteSelectedAction] and as
 * [nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState]'s own default —
 * so the gallery's render never calls `toSpinePalette()` itself (`component-contract.md` R9).
 */
internal fun galleryPaletteChoicesFor(selected: ColorPalette?): ImmutableList<GalleryPaletteChoice> = ColorPalette.entries
    .map { palette ->
        GalleryPaletteChoice(
            palette = palette,
            label = palette.toSpinePalette().label,
            selected = palette == selected,
        )
    }
    .toImmutableList()
