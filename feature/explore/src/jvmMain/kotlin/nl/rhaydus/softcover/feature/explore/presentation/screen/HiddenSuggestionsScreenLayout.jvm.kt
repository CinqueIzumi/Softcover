package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * Standalone desktop Hidden-suggestions page, pushed from Settings; it has no master–detail pane.
 */
@Composable
internal actual fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val topBar: @Composable () -> Unit = {
        EditorialTopBar(
            model = state.topBar,
            onEvent = { event ->
                when (event) {
                    TopBarEvent.BackClicked -> onNavigateBack()
                }
            },
        )
    }

    if (state.isEmpty) {
        Column(modifier = Modifier.fillMaxSize()) {
            topBar()

            EmptyState(
                model = state.emptyState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .cappedContentWidth()
                    .padding(horizontal = Spacing.gutter),
            )
        }
    } else {
        DesktopScrollPage(topBar = topBar) {
            HiddenSuggestionsContent(
                state = state,
                runAction = runAction,
            )
        }
    }
}

@StandardPreview
@Composable
private fun HiddenSuggestionsScreenDesktopPreview() {
    SoftcoverTheme {
        HiddenSuggestionsScreenLayout(
            state = HiddenSuggestionsUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
