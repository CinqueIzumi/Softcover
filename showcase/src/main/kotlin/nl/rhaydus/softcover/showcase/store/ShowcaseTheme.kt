package nl.rhaydus.softcover.showcase.store

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

internal data class ShowcaseTheme(
    val palette: SpinePalette = SpinePalette.DEFAULT,
    val brightness: SchemeBrightness = SchemeBrightness.LIGHT,
)
