package nl.rhaydus.softcover.orchestration.presentation

import cafe.adriel.voyager.navigator.tab.Tab

/**
 * The render-ready state of a single navigation destination, resolved once by [rememberNavItems]
 * and consumed identically by every chrome renderer (docked bar, floating bar, rail, sidebar) so
 * selection, the app-update badge, and the library pulse can never drift between them.
 *
 * [iconScale] is `1f` for every tab except the one currently playing the library pulse, so a
 * renderer applies it uniformly without knowing which tab is special.
 */
internal data class NavItemUi(
    val tab: Tab,
    val selected: Boolean,
    val showBadge: Boolean,
    val iconScale: Float,
)
