package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.designsystem.presentation.component.ThemeTilePainting
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * One tile in the Appearance screen's theme picker, mapped off the composition
 * (`component-contract.md` R9) so [nl.rhaydus.softcover.feature.settings.presentation.screen.ThemeSection]
 * only forwards already-resolved values. [mode] is the tile's tap payload for
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnThemeModeSelectedAction].
 */
internal data class ThemeChoice(
    val label: String,
    val painting: ThemeTilePainting,
    val selected: Boolean,
    val mode: ThemeMode,
)
