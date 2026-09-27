package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.RoadmapContent
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState

/**
 * The master–detail `Roadmap` category: a [PageMasthead] over the shared [RoadmapContent], following
 * [AboutPane]'s shape. No pull-to-refresh here (a touch-only gesture, not a desktop one) — the retry
 * inside a [RoadmapUiState.roadmapError] banner is the desktop refresh path.
 */
@Composable
internal fun RoadmapPane(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 24.dp,
                    bottom = 24.dp + rememberBottomBarPadding(),
                ),
        ) {
            Column(
                modifier = Modifier
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            ) {
                PageMasthead(model = state.masthead)

                Spacer(modifier = Modifier.height(28.dp))

                RoadmapContent(
                    state = state,
                    runAction = runAction,
                    openUrl = openUrl,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        DesktopVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}
