package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.domain.model.ColorPalette

/**
 * One chip in the Component Gallery's spine-colour override row, mapped off the composition
 * (`component-contract.md` R9) so
 * [nl.rhaydus.softcover.feature.settings.presentation.screen.ComponentGalleryContent] never labels a
 * [ColorPalette] itself. [palette] is the chip's tap payload for
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryPaletteSelectedAction].
 */
internal data class GalleryPaletteChoice(
    val palette: ColorPalette,
    val label: String,
    val selected: Boolean,
)
