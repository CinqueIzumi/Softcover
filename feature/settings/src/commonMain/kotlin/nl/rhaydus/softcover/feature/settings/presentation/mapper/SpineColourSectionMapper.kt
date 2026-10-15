package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.model.SpineColourSectionUiModel

internal fun spineColourSectionFor(
    themeMode: ThemeMode,
    useDynamicColor: Boolean,
    paletteGloss: String,
): SpineColourSectionUiModel = when {
    themeMode == ThemeMode.DIM -> SpineColourSectionUiModel(
        gloss = "Dim has its own colours. Spine colours apply in Light, Dark and System.",
        interactive = false,
    )

    useDynamicColor -> SpineColourSectionUiModel(
        gloss = "Dynamic colour is on. Pick a spine colour to switch back.",
        interactive = true,
    )

    else -> SpineColourSectionUiModel(
        gloss = paletteGloss,
        interactive = true,
    )
}
