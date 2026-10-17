package nl.rhaydus.softcover.core.uibinding.theme

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * The reader's stored appearance preference as the design system's brightness token.
 *
 * [ThemeMode.SYSTEM] defers to [systemInDark], the platform's own setting, so a caller reading it
 * from composition re-resolves whenever the device flips; the explicit modes ignore it. The `when`
 * is exhaustive on purpose, so a new [ThemeMode] is a compile error here.
 */
fun ThemeMode.toSchemeBrightness(systemInDark: Boolean): SchemeBrightness = when (this) {
    ThemeMode.LIGHT -> SchemeBrightness.LIGHT
    ThemeMode.DARK -> SchemeBrightness.DARK
    ThemeMode.DIM -> SchemeBrightness.DIM
    ThemeMode.SYSTEM -> if (systemInDark) SchemeBrightness.DARK else SchemeBrightness.LIGHT
}
