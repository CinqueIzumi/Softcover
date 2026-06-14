package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

object LibraryScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val appNavigator = koinInject<AppNavigator>()
        val bookDetailPresenter = LocalBookDetailPresenter.current

        val screenModel = koinScreenModel<LibraryScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()
        val localState by screenModel.localState.collectAsStateWithLifecycle()

        val prefetcher = rememberBookDetailPrefetcher()

        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            LibraryScreenLayout(
                state = state,
                runAction = screenModel::runAction,
                gridStateFor = { id -> localState.gridStates[id] ?: LazyGridState() },
                topAppBarState = screenModel.headerScrollState,
                onBookClick = {
                    val destination = ScreenDestination.BookDetail(
                        id = it.id,
                        initialCover = BookInitialCover.fromBook(book = it),
                    )

                    if (bookDetailPresenter != null) {
                        bookDetailPresenter.open(destination)
                    } else {
                        navigator.parent?.push(item = appNavigator.screen(destination))
                    }
                },
                onEditionClick = {
                    val destination = ScreenDestination.BookDetail(
                        id = it.bookId,
                        initialCover = BookInitialCover.fromEdition(edition = it),
                        transitionSurface = "edition-${it.id}",
                    )

                    if (bookDetailPresenter != null) {
                        bookDetailPresenter.open(destination)
                    } else {
                        navigator.parent?.push(item = appNavigator.screen(destination))
                    }
                },
                onTabLongPress = {
                    navigator.parent?.push(
                        item = appNavigator.screen(ScreenDestination.LibraryVisibilitySettings),
                    )
                },
                onCreateNewListClick = {
                    screenModel.runAction(OnBulkAddToListSheetShownAction(shown = false))

                    navigator.parent?.push(item = appNavigator.screen(ScreenDestination.CreateList))
                },
            )
        }
    }
}

/**
 * The Library screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [LibraryScreen.Content],
 * and only the rendered layout branches. `expect` cannot carry default argument values, so every
 * parameter is supplied explicitly at the single call site above.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal expect fun LibraryScreenLayout(
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onEditionClick: (BookEdition) -> Unit,
    onTabLongPress: () -> Unit,
    onCreateNewListClick: () -> Unit,
    gridStateFor: (String) -> LazyGridState,
    topAppBarState: TopAppBarState,
)
