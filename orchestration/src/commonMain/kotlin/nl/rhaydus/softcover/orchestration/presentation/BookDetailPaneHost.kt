package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import cafe.adriel.voyager.navigator.Navigator
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailOverlayNavigator
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailPaneCloseHandler
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen

/**
 * Hosts the selected book's detail in the trailing pane of the expanded-width two-pane layout. The
 * existing [BookDetailScreen] is rendered in a **nested navigator** so its screen model is scoped
 * and disposed per selection — `key(destination.id)` recreates the navigator (and frees the previous
 * book's model) whenever the reader picks a different book.
 *
 * Two seams adapt the screen to a pane without touching its pushed-path behaviour:
 * - the cover-morph transition scopes are nulled, so the pane swaps instantly instead of animating a
 *   shared element (the morph is a property of the compact push, DS §2.5);
 * - [LocalBookDetailPaneCloseHandler] redirects the detail's back affordance to [onClose] (clear the
 *   pane) instead of popping the nav stack.
 */
@Composable
internal fun BookDetailPaneHost(
    destination: ScreenDestination.BookDetail,
    onClose: () -> Unit,
    overlayNavigator: Navigator,
) {
    val backState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = backState,
        isBackEnabled = true,
        onBackCancelled = {},
        onBackCompleted = onClose,
    )

    key(destination.id) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides null,
            LocalNavAnimatedVisibilityScope provides null,
            LocalBookDetailPaneCloseHandler provides onClose,
            LocalBookDetailOverlayNavigator provides overlayNavigator,
        ) {
            Navigator(
                screen = BookDetailScreen(
                    id = destination.id,
                    initialCover = destination.initialCover,
                    transitionSurface = destination.transitionSurface,
                ),
            )
        }
    }
}
