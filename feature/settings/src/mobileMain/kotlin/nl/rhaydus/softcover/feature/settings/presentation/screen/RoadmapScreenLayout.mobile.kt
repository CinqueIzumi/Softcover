package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarNavigation
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.settings.presentation.action.RefreshRoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState

/** The bar is fixed for this screen, so the model is a constant rather than rebuilt per frame. */
private val ROADMAP_TOP_BAR = TopBarUiModel(
    title = "Roadmap",
    navigation = TopBarNavigation.Back,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun RoadmapScreenLayout(
    state: RoadmapUiState,
    runAction: (RoadmapAction) -> Unit,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopBar(
                model = ROADMAP_TOP_BAR,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { runAction(RefreshRoadmapAction()) },
            state = pullToRefreshState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .cappedContentWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp,
                    ),
            ) {
                PullToRefreshEyebrow(
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    baseText = "The plan",
                    refreshingText = "Checking for updates…",
                )

                Spacer(modifier = Modifier.height(16.dp))

                RoadmapContent(
                    state = state,
                    runAction = runAction,
                    openUrl = openUrl,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun RoadmapScreenPreview() {
    SoftcoverTheme {
        RoadmapScreenLayout(
            state = RoadmapUiState(),
            runAction = {},
            openUrl = {},
            onNavigateBack = {},
        )
    }
}
