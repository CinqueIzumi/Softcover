package nl.rhaydus.softcover.feature.widget.presentation.theme

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.domain.model.ThemeMode

internal data class WidgetSchemeSelection(
    val day: SchemeBrightness,
    val night: SchemeBrightness,
)

internal fun ThemeMode.toSchemeSelection(): WidgetSchemeSelection = when (this) {
    ThemeMode.LIGHT -> LIGHT_ONLY
    ThemeMode.DARK -> DARK_ONLY
    ThemeMode.DIM -> DIM_ONLY
    ThemeMode.SYSTEM -> FOLLOW_SYSTEM
}

private val LIGHT_ONLY = WidgetSchemeSelection(
    day = SchemeBrightness.LIGHT,
    night = SchemeBrightness.LIGHT,
)

private val DARK_ONLY = WidgetSchemeSelection(
    day = SchemeBrightness.DARK,
    night = SchemeBrightness.DARK,
)

private val DIM_ONLY = WidgetSchemeSelection(
    day = SchemeBrightness.DIM,
    night = SchemeBrightness.DIM,
)

private val FOLLOW_SYSTEM = WidgetSchemeSelection(
    day = SchemeBrightness.LIGHT,
    night = SchemeBrightness.DARK,
)
