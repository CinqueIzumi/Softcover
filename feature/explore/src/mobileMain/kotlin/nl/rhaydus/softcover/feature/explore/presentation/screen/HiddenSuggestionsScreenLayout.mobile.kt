package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * Mobile Hidden-suggestions page — a pushed sub-screen of Settings. Uses the standard [SoftcoverTopBar]
 * carrying the "Hidden suggestions" title + back control, exactly like the sibling Appearance /
 * Library-tabs settings pages; the [HiddenSuggestionsContent] body opens with an intro line rather than
 * repeating the title.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            SoftcoverTopBar(
                title = "Hidden suggestions",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        HiddenSuggestionsContent(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
                ),
        )
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
