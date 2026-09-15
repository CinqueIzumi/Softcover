package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.domain.model.ThemeMode

/**
 * One tile in the Appearance screen's theme picker, mapped off the composition
 * (`component-contract.md` R9) so [nl.rhaydus.softcover.feature.settings.presentation.screen.ThemeSection]
 * only forwards [tile] as-is. [mode] is the tile's tap payload for
 * [nl.rhaydus.softcover.feature.settings.presentation.action.OnThemeModeSelectedAction] — it stays on
 * this feature-local model rather than on [tile] itself, since `:core:component` may not hold a domain
 * enum (`component-contract.md` § 7.4). R4 governs the library, not a feature's own presentation
 * model, so carrying both the tile and its tap payload here is the intended shape.
 */
internal data class ThemeChoice(
    val tile: ThemePreviewTileUiModel,
    val mode: ThemeMode,
)
