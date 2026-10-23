package nl.rhaydus.softcover.showcase.appearance.event

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

internal sealed interface AppearanceSheetEvent {
    data class PaletteSelected(val palette: SpinePalette) : AppearanceSheetEvent

    data class BrightnessSelected(val brightness: SchemeBrightness) : AppearanceSheetEvent

    data object Dismissed : AppearanceSheetEvent
}
