package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.domain.model.ColorPalette

/**
 * One tile in the Appearance screen's spine-colour picker, mapped off the composition
 * (`component-contract.md` R9) so [nl.rhaydus.softcover.feature.settings.presentation.screen.SpineColourSection]
 * only forwards [tile] as-is. [colorPalette] is the tile's tap payload for
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnColorPaletteSelectedAction] — it stays
 * on this feature-local model rather than on [tile] itself, since `:core:component` may not hold a
 * domain enum (`component-contract.md` § 5i / § 7.4).
 */
internal data class PaletteChoice(
    val tile: ColorPalettePreviewTileUiModel,
    val colorPalette: ColorPalette,
)
