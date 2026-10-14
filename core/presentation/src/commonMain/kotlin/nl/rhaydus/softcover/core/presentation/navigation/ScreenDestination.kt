package nl.rhaydus.softcover.core.presentation.navigation

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover

/**
 * A cross-feature navigation target reachable as a pushed [cafe.adriel.voyager.core.screen.Screen].
 *
 * Features reference a destination — never another feature's `Screen` class — and resolve it through
 * [AppNavigator]. The concrete screen is constructed in the orchestration tier, keeping the feature
 * graph acyclic.
 */
sealed interface ScreenDestination {
    data class BookDetail(
        val id: Int,
        val initialCover: BookInitialCover? = null,
        val transitionSurface: String? = null,
    ) : ScreenDestination

    /**
     * @param onResolved receives the resolved book instead of the scanner pushing its detail screen.
     * Never persisted, so it is `null` after process death and the scanner falls back to pushing.
     */
    data class BarcodeScanner(
        val startInManualEntry: Boolean = false,
        val prefilledIsbn: String? = null,
        val onResolved: ((book: Book, editionId: Int?) -> Unit)? = null,
    ) : ScreenDestination

    data object LibraryVisibilitySettings : ScreenDestination

    data object FocusMode : ScreenDestination

    data object Profile : ScreenDestination

    data object HiddenSuggestions : ScreenDestination
}
