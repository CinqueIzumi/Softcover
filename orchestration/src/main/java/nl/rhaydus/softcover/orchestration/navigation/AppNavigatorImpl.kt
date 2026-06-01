package nl.rhaydus.softcover.orchestration.navigation

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.lists.presentation.screen.CreateListScreen
import nl.rhaydus.softcover.feature.profile.presentation.screen.ProfileScreen
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.scan.presentation.screen.BarcodeScannerScreen
import nl.rhaydus.softcover.feature.session.presentation.screen.FocusModeScreen
import nl.rhaydus.softcover.feature.settings.presentation.screen.LibraryVisibilitySettingsScreen
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

/**
 * Orchestration-tier resolution of [AppNavigator]. This is the single place that depends on every
 * feature's `Screen`/`Tab`, so the feature graph below it stays acyclic.
 */
class AppNavigatorImpl : AppNavigator {
    override fun screen(destination: ScreenDestination): Screen = when (destination) {
        is ScreenDestination.BookDetail -> BookDetailScreen(
            id = destination.id,
            initialCover = destination.initialCover,
            transitionSurface = destination.transitionSurface,
        )

        ScreenDestination.CreateList -> CreateListScreen()

        ScreenDestination.BarcodeScanner -> BarcodeScannerScreen()

        ScreenDestination.LibraryVisibilitySettings -> LibraryVisibilitySettingsScreen()

        ScreenDestination.FocusMode -> FocusModeScreen

        ScreenDestination.Profile -> ProfileScreen()
    }

    override fun tab(destination: TabDestination): Tab = when (destination) {
        TabDestination.READING -> ReadingTab
        TabDestination.LIBRARY -> LibraryTab
        TabDestination.EXPLORE -> ExploreTab
        TabDestination.SETTINGS -> SettingsTab
    }
}
