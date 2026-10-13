package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * Mobile Hidden-suggestions page — a pushed sub-screen of Settings. Uses the standard [TopBar]
 * carrying the "Hidden suggestions" title + back control, exactly like the sibling Appearance /
 * Library-tabs settings pages; the [HiddenSuggestionsContent] body opens with an intro line rather than
 * repeating the title.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
internal actual fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
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
        key(state.isLoading) {
            Crossfade(
                targetState = state.isEmpty,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyState(
                        model = state.emptyState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .cappedContentWidth()
                            .padding(horizontal = Spacing.gutter),
                    )
                } else {
                    HiddenSuggestionsContent(
                        state = state,
                        runAction = runAction,
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .cappedContentWidth()
                            .padding(
                                horizontal = Spacing.gutter,
                                vertical = Spacing.md,
                            ),
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun HiddenSuggestionsScreenPreview() {
    SoftcoverTheme {
        HiddenSuggestionsScreenLayout(
            state = HiddenSuggestionsUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
