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
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.action.DocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.action.RefreshDocumentPageAction
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toInitialUiState
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun DocumentPageScreenLayout(
    state: DocumentPageUiState,
    runAction: (DocumentPageAction) -> Unit,
    openUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopBar(
                model = state.topBar,
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
            onRefresh = { runAction(RefreshDocumentPageAction()) },
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
                        horizontal = Spacing.gutter,
                        vertical = Spacing.md,
                    ),
            ) {
                PullToRefreshEyebrow(
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    baseText = state.refreshEyebrow,
                    refreshingText = "Checking for updates…",
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                DocumentPageContent(
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
private fun DocumentPageScreenPreview() {
    SoftcoverTheme {
        DocumentPageScreenLayout(
            state = DocumentPage.ROADMAP.toInitialUiState(),
            runAction = {},
            openUrl = {},
            onNavigateBack = {},
        )
    }
}
