package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.TabDestination
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenScreenModel
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

object ReadingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ReadingScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val appNavigator = koinInject<AppNavigator>()
        val bookDetailPresenter = LocalBookDetailPresenter.current

        ReadingScreenLayout(
            state = state,
            runAction = screenModel::runAction,
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
            onNavigateToSearch = {
                tabNavigator.current = appNavigator.tab(TabDestination.EXPLORE)
            },
        )
    }
}

/**
 * The Reading screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions wire up identically in [ReadingScreen.Content],
 * and only the rendered layout branches. `expect` cannot carry default argument values, so every
 * parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun ReadingScreenLayout(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onNavigateToSearch: () -> Unit,
)
