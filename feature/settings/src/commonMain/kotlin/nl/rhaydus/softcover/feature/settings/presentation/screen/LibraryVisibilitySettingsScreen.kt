package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalCreateListPresenter
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class LibraryVisibilitySettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val createListPresenter = LocalCreateListPresenter.current

        val screenModel = koinScreenModel<LibraryVisibilitySettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        LibraryVisibilitySettingsScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
            onCreateListClick = { createListPresenter?.open(onListCreated = null) },
        )
    }
}

/**
 * The Library-tabs settings body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual` around the shared [LibraryVisibilityContent] + [LibraryVisibilitySaveBar]; only the chrome
 * branches. On desktop this standalone page is a fallback — the primary entry is the Settings
 * master–detail pane. `expect` cannot carry default argument values, so every parameter is supplied
 * explicitly at the single call site above.
 */
@Composable
internal expect fun LibraryVisibilitySettingsScreenLayout(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateListClick: () -> Unit,
)
