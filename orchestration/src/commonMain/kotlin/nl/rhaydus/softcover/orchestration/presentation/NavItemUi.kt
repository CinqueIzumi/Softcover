package nl.rhaydus.softcover.orchestration.presentation

import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.designsystem.icon.RhaydusIconResource

/**
 * The render-ready state of a single navigation destination, resolved once by [rememberNavItems]
 * and consumed identically by every chrome renderer (docked bar, floating bar, rail, sidebar) so
 * selection, the app-update badge, and the icon pulse can never drift between them.
 */
internal data class NavItemUi(
    val tab: Tab,
    val icon: RhaydusIconResource,
    val selected: Boolean,
    val showBadge: Boolean,
    val iconScale: Float,
)
