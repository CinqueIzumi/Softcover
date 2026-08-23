package nl.rhaydus.softcover.core.presentation.navigation

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.Tab

/**
 * Cross-feature navigation contract. Features inject this to open another feature's surface without
 * importing its `Screen`/`Tab` class — the implementation lives in the orchestration tier, which is
 * the only place allowed to depend on every feature.
 *
 * The call site keeps control of *how* it navigates (e.g. `navigator.push`, `navigator.parent?.push`,
 * `tabNavigator.current = ...`); this contract only resolves *what* to navigate to.
 */
interface AppNavigator {
    fun screen(destination: ScreenDestination): Screen

    fun tab(destination: TabDestination): Tab
}
