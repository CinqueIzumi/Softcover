package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState

/**
 * Standalone desktop Roadmap page — a fallback for a direct push (the primary desktop entry is the
 * Settings master–detail pane's `Roadmap` category). Static back bar over the shared [RoadmapContent],
 * capped to the reading measure, with a persistent desktop scrollbar. No pull-to-refresh here (a
 * touch-only gesture); the retry inside a [RoadmapUiState.roadmapError] banner is the desktop refresh
 * path.
 */
@Composable
internal actual fun RoadmapScreenLayout(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        DesktopSettingsBackBar(
            title = "Roadmap",
            onNavigateBack = onNavigateBack,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            RoadmapContent(
                state = state,
                runAction = runAction,
                openUrl = openUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .cappedContentWidth()
                    .padding(
                        horizontal = 32.dp,
                        vertical = 16.dp,
                    ),
            )

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}
