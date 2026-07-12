package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsScreenModel
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * Lists every hidden "up next in your series" book and series (Explore's
 * `ContinueSeriesMenuSheet` dismiss path) with a per-item unblock. Reached only via
 * [nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination.HiddenSuggestions]
 * from Settings - never referenced directly by another feature.
 */
class HiddenSuggestionsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<HiddenSuggestionsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        HiddenSuggestionsScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }
}

/**
 * Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke `actual`; `expect` cannot
 * carry default argument values, so every parameter is supplied explicitly at the single call site
 * above.
 */
@Composable
internal expect fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
)
