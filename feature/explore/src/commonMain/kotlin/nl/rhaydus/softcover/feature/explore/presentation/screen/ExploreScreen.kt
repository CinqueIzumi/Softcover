package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberIsOnline
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreScreenScreenModel
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

object ExploreScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val appNavigator = koinInject<AppNavigator>()
        val bookDetailPresenter = LocalBookDetailPresenter.current

        val screenModel = koinScreenModel<ExploreScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val isOnline = rememberIsOnline()

        val prefetcher = rememberBookDetailPrefetcher()

        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            ExploreScreenLayout(
                state = state,
                runAction = screenModel::runAction,
                onBookClick = { book, surface ->
                    val destination = ScreenDestination.BookDetail(
                        id = book.id,
                        initialCover = BookInitialCover.fromBook(book = book),
                        transitionSurface = surface,
                    )

                    if (bookDetailPresenter != null) {
                        bookDetailPresenter.open(destination)
                    } else {
                        navigator.parent?.push(item = appNavigator.screen(destination))
                    }
                },
                onScanClick = {
                    navigator.parent?.push(item = appNavigator.screen(ScreenDestination.BarcodeScanner))
                },
                isOnline = isOnline,
            )
        }
    }
}

/**
 * The Explore screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [ExploreScreen.Content],
 * and only the rendered layout branches. `expect` cannot carry default argument values, so every
 * parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun ExploreScreenLayout(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    onScanClick: () -> Unit,
    isOnline: Boolean,
)
