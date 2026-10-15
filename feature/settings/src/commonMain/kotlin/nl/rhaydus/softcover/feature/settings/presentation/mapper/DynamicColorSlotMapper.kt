package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.model.DynamicColorSlotUiModel

internal fun dynamicColorSlotFor(
    themeMode: ThemeMode,
    supportsDynamicColor: Boolean,
    useDynamicColor: Boolean,
): DynamicColorSlotUiModel = when {
    themeMode == ThemeMode.DIM && supportsDynamicColor -> DynamicColorSlotUiModel.Gloss(
        text = "Dynamic colour isn't available in Dim.",
    )

    supportsDynamicColor -> DynamicColorSlotUiModel.Toggle(row = dynamicColorToggleRowFor(checked = useDynamicColor))

    else -> DynamicColorSlotUiModel.Hidden
}
