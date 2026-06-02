package nl.rhaydus.softcover.core.designsystem.presentation.navigation

/**
 * A bottom-bar root reachable by switching the active [cafe.adriel.voyager.navigator.tab.Tab].
 * Resolved to the concrete tab through [AppNavigator.tab].
 */
enum class TabDestination {
    READING,
    LIBRARY,
    EXPLORE,
    SETTINGS,
}
