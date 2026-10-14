package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.DocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState

/**
 * Standalone desktop document page, a fallback for a direct push; the primary desktop entry is the
 * Settings master-detail pane's category for the page. There is no pull-to-refresh on desktop; the retry
 * inside a [DocumentPageUiState.error] banner is the refresh path.
 */
@Composable
internal actual fun DocumentPageScreenLayout(
    state: DocumentPageUiState,
    runAction: (DocumentPageAction) -> Unit,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    DesktopScrollPage(
        topBar = {
            EditorialTopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) {
        DocumentPageContent(
            state = state,
            runAction = runAction,
            openUrl = openUrl,
        )
    }
}
