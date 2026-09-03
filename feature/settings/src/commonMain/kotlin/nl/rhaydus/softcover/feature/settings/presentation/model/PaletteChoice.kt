package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette

/**
 * One tile in the Appearance screen's spine-colour picker, mapped off the composition
 * (`component-contract.md` R9) so [nl.rhaydus.softcover.feature.settings.presentation.screen.SpineColourSection]
 * only forwards already-resolved values. [colorPalette] is the tile's tap payload for
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnColorPaletteSelectedAction].
 */
internal data class PaletteChoice(
    val palette: SpinePalette,
    val selected: Boolean,
    val colorPalette: ColorPalette,
)
