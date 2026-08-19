package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState

class RoadmapScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<RoadmapScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        RoadmapScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            openUrl = uriHandler::openUri,
            onNavigateBack = navigator::pop,
        )
    }
}

/**
 * The Roadmap screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual` around the shared [RoadmapContent]; only the chrome (top bar + pull-to-refresh vs a static
 * back bar) branches. On desktop this standalone page is a fallback — the primary entry is the
 * Settings master–detail pane's `Roadmap` category. `expect` cannot carry default argument values, so
 * every parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun RoadmapScreenLayout(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
)
