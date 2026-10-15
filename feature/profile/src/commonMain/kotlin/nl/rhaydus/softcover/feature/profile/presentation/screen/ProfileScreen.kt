package nl.rhaydus.softcover.feature.profile.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.softcover.core.component.haptics.play
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.core.presentation.session.SessionAuthenticator
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import org.koin.compose.koinInject

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProfileScreenScreenModel>()
        val sessionAuthenticator = koinInject<SessionAuthenticator>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow

        val haptics = LocalHaptics.current

        ObserveAsEvents(flow = screenModel.events) {
            if (it is FeedbackEvent) haptics.play(it.feedback)

            when (it) {
                is ProfileEvent.LogOutUserEvent -> {
                    sessionAuthenticator.setUserAuthenticated(authenticated = false)
                }

                is ProfileEvent.LogOutFailedEvent -> Unit
            }
        }

        val prefetcher = rememberBookDetailPrefetcher()

        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            ProfileScreenLayout(
                state = state,
                runAction = screenModel::runAction,
                onNavigateUp = navigator::pop,
                onBookCardEvent = rememberRecentlyLovedEventHandler(
                    state = state,
                    navigator = navigator,
                ),
            )
        }
    }
}

/**
 * The Profile screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [ProfileScreen.Content],
 * and only the rendered layout branches. `expect` cannot carry default argument values, so every
 * parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun ProfileScreenLayout(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
)

/**
 * Opens book detail for a tapped "Recently loved" card and warms the detail on finger-down. A cached
 * library book seeds the destination in full; otherwise only the card's id, title and cover URL do.
 */
@Composable
private fun rememberRecentlyLovedEventHandler(
    state: ProfileUiState,
    navigator: Navigator,
): (BookCardEvent) -> Unit {
    val appNavigator = koinInject<AppNavigator>()
    val currentNavigator by rememberUpdatedState(navigator)

    return rememberPrefetchingBookCardEventHandler(
        set = state.recentlyLoved,
    ) { event, entry ->
        if (event is BookCardEvent.Clicked) {
            val loved = entry.loved
            val destination = ScreenDestination.BookDetail(
                id = loved.bookId,
                initialCover = entry.libraryBook?.let { BookInitialCover.fromBook(it) }
                    ?: BookInitialCover(
                        currentEdition = null,
                        defaultEdition = null,
                        fallbackCoverUrl = loved.coverUrl.takeIf { it.isNotBlank() },
                        title = loved.title,
                    ),
            )

            currentNavigator.push(item = appNavigator.screen(destination))
        }
    }
}
