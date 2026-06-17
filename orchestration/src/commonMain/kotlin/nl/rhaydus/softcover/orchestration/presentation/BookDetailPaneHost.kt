package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailOverlayNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailPaneCloseHandler
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope
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

/**
 * The trailing pane's resting state before a book is chosen — a quiet editorial prompt rather than a
 * blank surface.
 */
@Composable
internal fun EmptyDetailPane(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Select a book to see its details.",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
