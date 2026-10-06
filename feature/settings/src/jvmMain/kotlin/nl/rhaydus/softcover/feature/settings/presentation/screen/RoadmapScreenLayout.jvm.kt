package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState

/**
 * Standalone desktop Roadmap page, a fallback for a direct push; the primary desktop entry is the
 * Settings master–detail pane's `Roadmap` category. There is no pull-to-refresh on desktop; the retry
 * inside a [RoadmapUiState.roadmapError] banner is the refresh path.
 */
@Composable
internal actual fun RoadmapScreenLayout(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
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
        RoadmapContent(
            state = state,
            runAction = runAction,
            openUrl = openUrl,
        )
    }
}
