package nl.rhaydus.softcover.feature.widget.theme

import nl.rhaydus.softcover.core.domain.model.ThemeMode

internal data class WidgetSchemeSelection(
    val dayIsDark: Boolean,
    val nightIsDark: Boolean,
)

internal fun ThemeMode.toSchemeSelection(): WidgetSchemeSelection = when (this) {
    ThemeMode.LIGHT -> LIGHT_ONLY
    ThemeMode.DARK -> DARK_ONLY
    ThemeMode.SYSTEM -> FOLLOW_SYSTEM
}

private val LIGHT_ONLY = WidgetSchemeSelection(
    dayIsDark = false,
    nightIsDark = false,
)

private val DARK_ONLY = WidgetSchemeSelection(
    dayIsDark = true,
    nightIsDark = true,
)

private val FOLLOW_SYSTEM = WidgetSchemeSelection(
    dayIsDark = false,
    nightIsDark = true,
)
