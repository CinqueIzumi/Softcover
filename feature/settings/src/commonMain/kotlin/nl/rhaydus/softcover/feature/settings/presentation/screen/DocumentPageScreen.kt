package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.settings.presentation.action.DocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState

class DocumentPageScreen(
    private val page: DocumentPage,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinDocumentPageScreenModel(page = page)

        val state by screenModel.state.collectAsStateWithLifecycle()

        val uriHandler = LocalUriHandler.current

        DocumentPageScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            openUrl = uriHandler::openUri,
            onNavigateBack = navigator::pop,
        )
    }
}

/**
 * The document page screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual` around the shared [DocumentPageContent]; only the chrome (top bar + pull-to-refresh vs a static
 * back bar) branches. On desktop this standalone page is a fallback: the primary entry is the Settings
 * master-detail pane's category for the page. `expect` cannot carry default argument values, so every
 * parameter is supplied explicitly at the single call site above.
 */
@Composable
internal expect fun DocumentPageScreenLayout(
    state: DocumentPageUiState,
    runAction: (DocumentPageAction) -> Unit,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
)
