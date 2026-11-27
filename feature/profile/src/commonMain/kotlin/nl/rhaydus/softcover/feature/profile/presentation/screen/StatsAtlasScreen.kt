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
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequests
import nl.rhaydus.softcover.core.presentation.navigation.LibraryFilterRequests
import nl.rhaydus.softcover.core.presentation.navigation.LibraryTagFilterRequest
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import org.koin.compose.koinInject

class StatsAtlasScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<StatsAtlasScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow

        val prefetcher = rememberBookDetailPrefetcher()

        val appNavigator = koinInject<AppNavigator>()

        val libraryFilterRequests = koinInject<LibraryFilterRequests>()

        val externalNavigationRequests = koinInject<ExternalNavigationRequests>()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is StatsAtlasEvent.OpenBookDetail -> {
                    val destination = ScreenDestination.BookDetail(
                        id = it.bookId,
                        initialCover = it.initialCover,
                    )

                    navigator.push(item = appNavigator.screen(destination))
                }

                is StatsAtlasEvent.OpenLibraryForTag -> {
                    libraryFilterRequests.request(
                        request = LibraryTagFilterRequest(
                            category = TagCategory.TAG,
                            name = it.tagName,
                        ),
                    )

                    externalNavigationRequests.request(
                        request = ExternalNavigationRequest.OpenTab(TabDestination.LIBRARY),
                    )
                }
            }
        }

        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            StatsAtlasScreenLayout(
                state = state,
                runAction = screenModel::runAction,
                onNavigateUp = navigator::pop,
                onBookCardEvent = rememberRecentlyLovedEventHandler(
                    state = state,
                    navigator = navigator,
                ),
                onRecordEvent = rememberReadingRecordEventHandler(
                    state = state,
                    navigator = navigator,
                ),
            )
        }
    }
}

/**
 * The Stats Atlas body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke `actual`;
 * `expect` cannot carry default argument values, so every parameter is supplied at the single call
 * site above.
 */
@Composable
internal expect fun StatsAtlasScreenLayout(
    state: StatsAtlasUiState,
    runAction: (StatsAtlasAction) -> Unit,
    onNavigateUp: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
    onRecordEvent: (BookCardEvent) -> Unit,
)

/**
 * Opens book detail for a tapped "Recently loved" card and warms the detail on finger-down. The
 * cached library book seeds the destination.
 */
@Composable
private fun rememberRecentlyLovedEventHandler(
    state: StatsAtlasUiState,
    navigator: Navigator,
): (BookCardEvent) -> Unit {
    val appNavigator = koinInject<AppNavigator>()
    val currentNavigator by rememberUpdatedState(navigator)

    return rememberPrefetchingBookCardEventHandler(
        set = state.recentlyLoved,
    ) { event, entry ->
        if (event is BookCardEvent.Clicked) {
            val destination = ScreenDestination.BookDetail(
                id = entry.loved.bookId,
                initialCover = BookInitialCover.fromBook(entry.libraryBook),
            )

            currentNavigator.push(item = appNavigator.screen(destination))
        }
    }
}

/**
 * Opens book detail for a tapped record card with the cover's shared-element transition, and warms the
 * detail on finger-down.
 */
@Composable
private fun rememberReadingRecordEventHandler(
    state: StatsAtlasUiState,
    navigator: Navigator,
): (BookCardEvent) -> Unit {
    val appNavigator = koinInject<AppNavigator>()
    val currentNavigator by rememberUpdatedState(navigator)

    return rememberPrefetchingBookCardEventHandler(
        set = state.readingRecords.cards,
    ) { event, entry ->
        if (event is BookCardEvent.Clicked) {
            val destination = ScreenDestination.BookDetail(
                id = entry.book.id,
                initialCover = BookInitialCover.fromBook(entry.book),
                transitionSurface = entry.surface,
            )

            currentNavigator.push(item = appNavigator.screen(destination))
        }
    }
}
